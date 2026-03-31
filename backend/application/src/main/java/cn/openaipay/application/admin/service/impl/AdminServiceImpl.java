package cn.openaipay.application.admin.service.impl;

import cn.openaipay.application.admin.command.AdminLoginCommand;
import cn.openaipay.application.admin.dto.AdminLoginResultDTO;
import cn.openaipay.application.admin.service.AdminService;
import cn.openaipay.application.auth.exception.UnauthorizedException;
import cn.openaipay.application.auth.security.LoginAttemptGuard;
import cn.openaipay.domain.admin.model.AdminAccount;
import cn.openaipay.domain.admin.repository.AdminRepository;
import cn.openaipay.domain.shared.security.CredentialDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台管理应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class AdminServiceImpl implements AdminService {

    /** 默认过期秒数常量 */
    private static final long DEFAULT_EXPIRES_IN_SECONDS = 8L * 3600;
    /** 通用失败消息常量 */
    private static final String GENERIC_FAIL_MESSAGE = "账号或密码错误";

    /** AdminRepository组件 */
    private final AdminRepository adminRepository;
    /** 登录失败限流守卫。 */
    private final LoginAttemptGuard loginAttemptGuard;
    /** 域信息 */
    private final CredentialDomainService credentialDomainService;

    public AdminServiceImpl(AdminRepository adminRepository,
                            LoginAttemptGuard loginAttemptGuard,
                                       CredentialDomainService credentialDomainService) {
        this.adminRepository = adminRepository;
        this.loginAttemptGuard = loginAttemptGuard;
        this.credentialDomainService = credentialDomainService;
    }

    /**
     * 处理登录信息。
     */
    @Override
    @Transactional
    public AdminLoginResultDTO login(AdminLoginCommand command) {
        String username = credentialDomainService.normalizeOptional(command.username());
        String password = credentialDomainService.normalizeOptional(command.password());
        if (username == null || password == null) {
            throw new UnauthorizedException(GENERIC_FAIL_MESSAGE);
        }
        loginAttemptGuard.checkAllowed("ADMIN", username);

        AdminAccount account = adminRepository.findByUsername(username)
                .orElseThrow(() -> {
                    loginAttemptGuard.recordFailure("ADMIN", username);
                    return new UnauthorizedException(GENERIC_FAIL_MESSAGE);
                });

        if (!account.isActive()) {
            loginAttemptGuard.recordFailure("ADMIN", username);
            throw new UnauthorizedException("账号已被禁用，请联系管理员");
        }

        if (!credentialDomainService.matchesSha256(password, account.getPasswordSha256())) {
            loginAttemptGuard.recordFailure("ADMIN", username);
            throw new UnauthorizedException(GENERIC_FAIL_MESSAGE);
        }
        loginAttemptGuard.recordSuccess("ADMIN", username);

        LocalDateTime now = LocalDateTime.now();
        account.recordLogin(now);
        adminRepository.saveAccount(account);

        List<String> roleCodes = adminRepository.findRoleCodesByAdminId(account.getAdminId());
        List<String> permissionCodes = adminRepository.findPermissionCodesByAdminId(account.getAdminId());

        return new AdminLoginResultDTO(
                credentialDomainService.buildAccessToken(account.getAdminId(), command.deviceId(), DEFAULT_EXPIRES_IN_SECONDS),
                "Bearer",
                DEFAULT_EXPIRES_IN_SECONDS,
                String.valueOf(account.getAdminId()),
                account.getUsername(),
                account.getDisplayName(),
                roleCodes,
                permissionCodes
        );
    }
}
