package cn.openaipay.application.auth.service.impl;

import cn.openaipay.application.app.command.BindAppDeviceLoginUserCommand;
import cn.openaipay.application.app.facade.AppFacade;
import cn.openaipay.application.app.dto.AppLoginDeviceAccountWhitelistDTO;
import cn.openaipay.application.auth.command.LoginCommand;
import cn.openaipay.application.auth.dto.LoginPresetAccountDTO;
import cn.openaipay.application.auth.dto.LoginResultDTO;
import cn.openaipay.application.auth.exception.UnauthorizedException;
import cn.openaipay.application.auth.port.UserAuthQueryPort;
import cn.openaipay.application.auth.port.UserAuthView;
import cn.openaipay.application.auth.security.LoginAttemptGuard;
import cn.openaipay.application.auth.service.AuthService;
import cn.openaipay.domain.shared.security.CredentialDomainService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 认证应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class AuthServiceImpl implements AuthService {

    /** 默认过期秒数常量 */
    private static final long DEFAULT_EXPIRES_IN_SECONDS = 7L * 24 * 3600;
    /** 默认应用编码。 */
    private static final String DEFAULT_APP_CODE = "OPENAIPAY_IOS";
    /** 通用失败消息常量 */
    private static final String GENERIC_FAIL_MESSAGE = "账号或密码错误";
    /** 设备缺失提示 */
    private static final String DEVICE_REQUIRED_MESSAGE = "登录设备不能为空";
    /** 设备不匹配提示 */
    private static final String DEVICE_NOT_MATCHED_MESSAGE = "该账号仅支持在注册的手机上登录";

    /** UserAuthQueryPort组件 */
    private final UserAuthQueryPort userAuthQueryPort;
    /** 登录失败限流守卫。 */
    private final LoginAttemptGuard loginAttemptGuard;
    /** 域信息 */
    private final CredentialDomainService credentialDomainService;
    /** 应用信息 */
    private final AppFacade appFacade;

    public AuthServiceImpl(UserAuthQueryPort userAuthQueryPort,
                           LoginAttemptGuard loginAttemptGuard,
                           CredentialDomainService credentialDomainService,
                           AppFacade appFacade) {
        this.userAuthQueryPort = userAuthQueryPort;
        this.loginAttemptGuard = loginAttemptGuard;
        this.credentialDomainService = credentialDomainService;
        this.appFacade = appFacade;
    }

    /**
     * 本机号码验证登录（免密）。
     */
    @Override
    public LoginResultDTO mobileVerifyLogin(LoginCommand command) {
        String loginId = credentialDomainService.normalizeOptional(command.loginId());
        String normalizedDeviceId = credentialDomainService.normalizeOptional(command.deviceId());
        List<String> normalizedLegacyDeviceIds = normalizeLegacyDeviceIds(command.legacyDeviceIds(), normalizedDeviceId);
        if (loginId == null) {
            throw new UnauthorizedException(GENERIC_FAIL_MESSAGE);
        }
        if (normalizedDeviceId == null) {
            throw new UnauthorizedException(DEVICE_REQUIRED_MESSAGE);
        }
        loginAttemptGuard.checkAllowed("USER", loginId);

        UserAuthView user = userAuthQueryPort.findByLoginId(loginId)
                .orElseThrow(() -> {
                    loginAttemptGuard.recordFailure("USER", loginId);
                    return new UnauthorizedException(GENERIC_FAIL_MESSAGE);
                });

        if (!"ACTIVE".equalsIgnoreCase(user.accountStatus())) {
            loginAttemptGuard.recordFailure("USER", loginId);
            throw new UnauthorizedException("账号状态异常，请联系管理员");
        }

        String resolvedLoginId = credentialDomainService.normalizeOptional(user.loginId());
        if (resolvedLoginId == null) {
            resolvedLoginId = loginId;
        }
        if (appFacade.isLoginDeviceBindingCheckEnabled(DEFAULT_APP_CODE)) {
            boolean whitelistConfigured = appFacade.isLoginWhitelistConfigured(DEFAULT_APP_CODE, resolvedLoginId);
            boolean whitelistAllowed = appFacade.isLoginWhitelistAllowed(DEFAULT_APP_CODE, normalizedDeviceId, resolvedLoginId);
            boolean legacyWhitelistAllowed = !whitelistAllowed
                    && isAnyDeviceWhitelisted(normalizedLegacyDeviceIds, resolvedLoginId);
            if (whitelistConfigured && !whitelistAllowed && !legacyWhitelistAllowed) {
                loginAttemptGuard.recordFailure("USER", loginId);
                throw new UnauthorizedException(DEVICE_NOT_MATCHED_MESSAGE);
            }
            String boundDeviceId = credentialDomainService.normalizeOptional(
                    appFacade.queryBoundDeviceId(user.userId(), resolvedLoginId)
            );
            boolean currentDeviceMatchesBound = boundDeviceId != null && normalizedDeviceId.equals(boundDeviceId);
            boolean legacyDeviceMatchesBound = boundDeviceId != null && normalizedLegacyDeviceIds.contains(boundDeviceId);
            if (boundDeviceId != null
                    && !currentDeviceMatchesBound
                    && !legacyDeviceMatchesBound
                    && !whitelistAllowed
                    && !legacyWhitelistAllowed) {
                loginAttemptGuard.recordFailure("USER", loginId);
                throw new UnauthorizedException(DEVICE_NOT_MATCHED_MESSAGE);
            }
            if (whitelistConfigured && legacyWhitelistAllowed && !whitelistAllowed) {
                appFacade.bindLoginWhitelistAccount(
                        DEFAULT_APP_CODE,
                        normalizedDeviceId,
                        resolvedLoginId,
                        user.nickname()
                );
            }
        }
        loginAttemptGuard.recordSuccess("USER", loginId);

        return bindAndBuildLoginResult(user, normalizedDeviceId);
    }

    private LoginResultDTO bindAndBuildLoginResult(UserAuthView user, String normalizedDeviceId) {
        appFacade.bindLoginUser(new BindAppDeviceLoginUserCommand(
                normalizedDeviceId,
                user.userId(),
                user.aipayUid(),
                user.loginId(),
                user.accountStatus(),
                user.kycLevel(),
                user.nickname(),
                user.avatarUrl(),
                user.mobile(),
                user.maskedRealName(),
                user.idCardNoMasked(),
                user.countryCode(),
                user.gender(),
                user.region()
        ));

        return new LoginResultDTO(
                credentialDomainService.buildAccessToken(user.userId(), normalizedDeviceId, DEFAULT_EXPIRES_IN_SECONDS),
                "Bearer",
                DEFAULT_EXPIRES_IN_SECONDS,
                user.userId(),
                user.aipayUid(),
                user.nickname()
        );
    }

    /**
     * 查询登录下拉账号。
     */
    @Override
    public List<LoginPresetAccountDTO> listPresetLoginAccounts(String deviceId, List<String> legacyDeviceIds) {
        String normalizedDeviceId = credentialDomainService.normalizeOptional(deviceId);
        if (normalizedDeviceId == null) {
            throw new IllegalArgumentException("deviceId must not be blank");
        }
        List<String> candidateDeviceIds = normalizeLegacyDeviceIds(legacyDeviceIds, normalizedDeviceId);
        candidateDeviceIds.add(0, normalizedDeviceId);
        Map<String, LoginPresetAccountDTO> accountByLoginId = new LinkedHashMap<>();
        for (String candidateDeviceId : candidateDeviceIds) {
            appFacade.listLoginWhitelistAccounts(DEFAULT_APP_CODE, candidateDeviceId)
                    .stream()
                    .filter(account -> credentialDomainService.normalizeOptional(account.loginId()) != null)
                    .map(this::toLoginPresetAccountDTO)
                    .forEach(account -> accountByLoginId.putIfAbsent(account.loginId(), account));
        }
        return List.copyOf(accountByLoginId.values());
    }

    private LoginPresetAccountDTO toLoginPresetAccountDTO(AppLoginDeviceAccountWhitelistDTO account) {
        String normalizedLoginId = credentialDomainService.normalizeOptional(account.loginId());
        String normalizedNickname = credentialDomainService.normalizeOptional(account.nickname());
        return new LoginPresetAccountDTO(
                normalizedLoginId,
                normalizedNickname == null ? normalizedLoginId : normalizedNickname
        );
    }

    private boolean isAnyDeviceWhitelisted(List<String> legacyDeviceIds, String loginId) {
        for (String legacyDeviceId : legacyDeviceIds) {
            if (appFacade.isLoginWhitelistAllowed(DEFAULT_APP_CODE, legacyDeviceId, loginId)) {
                return true;
            }
        }
        return false;
    }

    private List<String> normalizeLegacyDeviceIds(List<String> legacyDeviceIds, String primaryDeviceId) {
        List<String> normalized = new ArrayList<>();
        if (legacyDeviceIds == null || legacyDeviceIds.isEmpty()) {
            return normalized;
        }
        for (String legacyDeviceId : legacyDeviceIds) {
            String candidate = credentialDomainService.normalizeOptional(legacyDeviceId);
            if (candidate == null || candidate.equals(primaryDeviceId) || normalized.contains(candidate)) {
                continue;
            }
            normalized.add(candidate);
        }
        return normalized;
    }
}
