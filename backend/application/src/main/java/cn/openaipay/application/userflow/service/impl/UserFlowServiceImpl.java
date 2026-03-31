package cn.openaipay.application.userflow.service.impl;

import cn.openaipay.application.agreement.command.AgreementAcceptCommand;
import cn.openaipay.application.agreement.command.OpenAiCreditWithAgreementCommand;
import cn.openaipay.application.agreement.command.OpenAiLoanWithAgreementCommand;
import cn.openaipay.application.agreement.dto.AgreementTemplateDTO;
import cn.openaipay.application.agreement.dto.CreditProductOpenAgreementPackDTO;
import cn.openaipay.application.agreement.facade.AgreementFacade;
import cn.openaipay.application.auth.port.UserCredentialPort;
import cn.openaipay.application.contact.command.ApplyFriendRequestCommand;
import cn.openaipay.application.contact.command.HandleFriendRequestCommand;
import cn.openaipay.application.contact.dto.ContactRequestDTO;
import cn.openaipay.application.contact.facade.ContactFacade;
import cn.openaipay.application.kyc.command.SubmitKycCommand;
import cn.openaipay.application.kyc.facade.KycFacade;
import cn.openaipay.application.message.command.SendTextMessageCommand;
import cn.openaipay.application.message.facade.MessageFacade;
import cn.openaipay.application.asyncmessage.AsyncMessageTopics;
import cn.openaipay.application.outbox.OutboxPublisher;
import cn.openaipay.application.user.command.CreateUserCommand;
import cn.openaipay.application.user.service.UserService;
import cn.openaipay.application.userflow.command.RegisterUserCommand;
import cn.openaipay.application.userflow.async.RegisterInitialWalletTopUpPayload;
import cn.openaipay.application.userflow.dto.RegisterPhoneCheckDTO;
import cn.openaipay.application.userflow.dto.UserRegistrationDTO;
import cn.openaipay.application.userflow.port.UserFlowQueryPort;
import cn.openaipay.application.userflow.port.UserRegistrationView;
import cn.openaipay.application.userflow.security.UserFlowAttemptGuard;
import cn.openaipay.application.userflow.service.UserFlowService;
import cn.openaipay.domain.app.model.AppLoginDeviceAccountWhitelist;
import cn.openaipay.domain.app.repository.AppInfoRepository;
import cn.openaipay.domain.app.repository.AppLoginDeviceAccountWhitelistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 用户流程应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Service
public class UserFlowServiceImpl implements UserFlowService {

    /** 日志组件。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserFlowServiceImpl.class);

    /** 自动开通协议幂等键前缀。 */
    private static final String AUTO_REG_IDEMPOTENCY_PREFIX = "AUTO_REG";
    /** 自动好友申请文案。 */
    private static final String AUTO_CONTACT_APPLY_MESSAGE = "注册自动添加客服";
    /** 默认欢迎消息文案。 */
    private static final String AUTO_CONTACT_WELCOME_MESSAGE = "您好！有什么问题可以帮您？";
    /** 普通注册账号来源。 */
    private static final String ACCOUNT_SOURCE_REGISTER = "REGISTER";
    /** 演示账号来源。 */
    private static final String ACCOUNT_SOURCE_DEMO = "DEMO";
    /** 注册赠送余额消息 key 前缀。 */
    private static final String REGISTER_WALLET_TOPUP_MESSAGE_KEY_PREFIX = "REGISTER_WALLET_TOPUP";
    /** 注册赠送余额异步重试次数。 */
    private static final int REGISTER_WALLET_TOPUP_MAX_RETRY = 12;
    /** 注册默认应用编码。 */
    private static final String DEFAULT_APP_CODE = "OPENAIPAY_IOS";
    /** 单设备最大注册账号数量。 */
    private static final long MAX_REGISTER_ACCOUNT_PER_DEVICE = 3L;

    /** 用户主数据服务。 */
    private final UserService userService;
    /** 异步消息发布器。 */
    private final OutboxPublisher outboxPublisher;
    /** 注册查询端口。 */
    private final UserFlowQueryPort userFlowQueryPort;
    /** 用户凭证写入端口。 */
    private final UserCredentialPort userCredentialPort;
    /** 实名门面。 */
    private final KycFacade kycFacade;
    /** 协议门面。 */
    private final AgreementFacade agreementFacade;
    /** 联系人门面。 */
    private final ContactFacade contactFacade;
    /** 消息门面。 */
    private final MessageFacade messageFacade;
    /** 用户流程限流守卫。 */
    private final UserFlowAttemptGuard userFlowAttemptGuard;
    /** 登录设备白名单账号仓储。 */
    private final AppLoginDeviceAccountWhitelistRepository appLoginDeviceAccountWhitelistRepository;
    /** 应用配置仓储。 */
    private final AppInfoRepository appInfoRepository;
    /** 注册后置任务独立事务模板。 */
    private final TransactionTemplate postRegisterRequiresNewTransactionTemplate;

    public UserFlowServiceImpl(UserService userService,
                               OutboxPublisher outboxPublisher,
                               UserFlowQueryPort userFlowQueryPort,
                               UserCredentialPort userCredentialPort,
                               KycFacade kycFacade,
                               AgreementFacade agreementFacade,
                               ContactFacade contactFacade,
                               MessageFacade messageFacade,
                               UserFlowAttemptGuard userFlowAttemptGuard,
                               AppLoginDeviceAccountWhitelistRepository appLoginDeviceAccountWhitelistRepository,
                               AppInfoRepository appInfoRepository,
                               PlatformTransactionManager transactionManager) {
        this.userService = userService;
        this.outboxPublisher = outboxPublisher;
        this.userFlowQueryPort = userFlowQueryPort;
        this.userCredentialPort = userCredentialPort;
        this.kycFacade = kycFacade;
        this.agreementFacade = agreementFacade;
        this.contactFacade = contactFacade;
        this.messageFacade = messageFacade;
        this.userFlowAttemptGuard = userFlowAttemptGuard;
        this.appLoginDeviceAccountWhitelistRepository = appLoginDeviceAccountWhitelistRepository;
        this.appInfoRepository = appInfoRepository;
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.postRegisterRequiresNewTransactionTemplate = template;
    }

    /**
     * 校验注册手机号状态。
     */
    @Override
    public RegisterPhoneCheckDTO checkRegisterPhone(String loginId, String deviceId) {
        String normalizedLoginId = normalizeMainlandPhone(loginId);
        String normalizedDeviceId = normalizeOptional(deviceId);
        userFlowAttemptGuard.checkRegisterCheckAllowed(normalizedLoginId);
        userFlowAttemptGuard.recordRegisterCheckAttempt(normalizedLoginId);
        if (hasText(normalizedDeviceId)) {
            validateRegisterDeviceLimit(List.of(normalizedDeviceId));
        }
        // 注册校验统一返回不含存在性信息的结果，避免手机号枚举。
        return new RegisterPhoneCheckDTO(false, false, null);
    }

    /**
     * 执行注册流程。
     */
    @Override
    @Transactional
    public UserRegistrationDTO register(RegisterUserCommand command) {
        String normalizedDeviceId = normalizeOptional(command.deviceId());
        if (!hasText(normalizedDeviceId)) {
            throw new IllegalArgumentException("deviceId不能为空");
        }
        List<String> normalizedLegacyDeviceIds = normalizeLegacyDeviceIds(command.legacyDeviceIds(), normalizedDeviceId);
        List<String> registerDeviceIds = resolveRegisterDeviceIds(normalizedDeviceId, normalizedLegacyDeviceIds);
        String normalizedLoginId = normalizeMainlandPhone(command.loginId());
        userFlowAttemptGuard.checkRegisterSubmitAllowed(normalizedLoginId);
        userFlowAttemptGuard.recordRegisterSubmitAttempt(normalizedLoginId);
        if (userFlowQueryPort.findByLoginId(normalizedLoginId).isPresent()) {
            throw new IllegalArgumentException("注册失败，请检查信息后重试或直接登录");
        }
        String normalizedUserTypeCode = normalizeUserTypeCode(command.userTypeCode());
        String normalizedAccountSource = normalizeAccountSource(command.accountSource());
        String normalizedRealName = normalizeOptional(command.realName());
        String normalizedIdCardNo = normalizeIdCardNo(command.idCardNo());
        String normalizedNickname = resolveRegisterNickname(
                command.nickname(),
                normalizedRealName,
                normalizedAccountSource
        );
        String normalizedCountryCode = normalizeCountryCode(command.countryCode());
        String normalizedMobile = normalizeMobile(command.mobile(), normalizedLoginId);
        if (!hasText(normalizedRealName) || !hasText(normalizedIdCardNo)) {
            throw new IllegalArgumentException("注册必须包含实名信息");
        }
        if (ACCOUNT_SOURCE_REGISTER.equals(normalizedAccountSource)
                && hasText(normalizedIdCardNo)
                && userFlowQueryPort.existsRegisterAccountByIdCardNo(normalizedIdCardNo)) {
            throw new IllegalArgumentException("该身份证号已被注册");
        }
        validateRegisterDeviceLimit(registerDeviceIds);
        Long userId = userService.createCoreUser(new CreateUserCommand(
                null,
                normalizedLoginId,
                normalizedUserTypeCode,
                normalizedAccountSource,
                normalizedNickname,
                command.avatarUrl(),
                normalizedCountryCode,
                normalizedMobile
        ));
        userCredentialPort.initializeLoginPassword(userId, normalizePassword(command.loginPassword()));

        boolean kycSubmitted = hasText(normalizedRealName) && hasText(normalizedIdCardNo);
        if (kycSubmitted) {
            kycFacade.submit(new SubmitKycCommand(userId, normalizedRealName, normalizedIdCardNo));
        }
        bindRegisterDeviceAccount(registerDeviceIds, normalizedLoginId, normalizedNickname);
        schedulePostRegisterTasks(userId, normalizedLoginId, normalizedAccountSource);
        return new UserRegistrationDTO(userId, String.valueOf(userId), normalizedLoginId, kycSubmitted);
    }

    private void bindRegisterDeviceAccount(List<String> registerDeviceIds, String normalizedLoginId, String normalizedNickname) {
        if (registerDeviceIds == null || registerDeviceIds.isEmpty()) {
            return;
        }
        String bindDeviceId = registerDeviceIds.getFirst();
        LocalDateTime now = LocalDateTime.now();
        AppLoginDeviceAccountWhitelist whitelist = new AppLoginDeviceAccountWhitelist(
                null,
                DEFAULT_APP_CODE,
                bindDeviceId,
                normalizedLoginId,
                normalizedNickname,
                true,
                now,
                now
        );
        appLoginDeviceAccountWhitelistRepository.save(whitelist);
    }

    private void validateRegisterDeviceLimit(List<String> registerDeviceIds) {
        if (registerDeviceIds == null || registerDeviceIds.isEmpty()) {
            return;
        }
        Set<String> exemptLoginIds = resolveRegisterLimitExemptLoginIds();
        for (String registerDeviceId : registerDeviceIds) {
            if (isRegisterLimitExemptDevice(registerDeviceId, exemptLoginIds)) {
                return;
            }
        }
        Set<String> boundLoginIds = new LinkedHashSet<>();
        for (String registerDeviceId : registerDeviceIds) {
            appLoginDeviceAccountWhitelistRepository
                    .listEnabledByAppCodeAndDeviceId(DEFAULT_APP_CODE, registerDeviceId)
                    .stream()
                    .map(AppLoginDeviceAccountWhitelist::getLoginId)
                    .map(this::normalizeOptional)
                    .filter(this::hasText)
                    .forEach(boundLoginIds::add);
        }
        if (boundLoginIds.size() >= MAX_REGISTER_ACCOUNT_PER_DEVICE) {
            throw new IllegalArgumentException("同一设备最多注册3个账号");
        }
    }

    private Set<String> resolveRegisterLimitExemptLoginIds() {
        return appInfoRepository.findByAppCode(DEFAULT_APP_CODE)
                .map(appInfo -> {
                    LinkedHashSet<String> loginIds = new LinkedHashSet<>();
                    String templateLoginId = normalizeOptional(appInfo.getDemoTemplateLoginId());
                    if (hasText(templateLoginId)) {
                        loginIds.add(templateLoginId);
                    }
                    String contactLoginId = normalizeOptional(appInfo.getDemoContactLoginId());
                    if (hasText(contactLoginId)) {
                        loginIds.add(contactLoginId);
                    }
                    return Set.copyOf(loginIds);
                })
                .orElse(Set.of());
    }

    private boolean isRegisterLimitExemptDevice(String deviceId, Set<String> exemptLoginIds) {
        if (!hasText(deviceId) || exemptLoginIds == null || exemptLoginIds.isEmpty()) {
            return false;
        }
        for (String exemptLoginId : exemptLoginIds) {
            if (appLoginDeviceAccountWhitelistRepository.existsEnabledByAppCodeAndDeviceIdAndLoginId(
                    DEFAULT_APP_CODE,
                    deviceId,
                    exemptLoginId
            )) {
                return true;
            }
        }
        return false;
    }

    private List<String> normalizeLegacyDeviceIds(List<String> legacyDeviceIds, String primaryDeviceId) {
        if (legacyDeviceIds == null || legacyDeviceIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String legacyDeviceId : legacyDeviceIds) {
            String candidate = normalizeOptional(legacyDeviceId);
            if (!hasText(candidate) || candidate.equals(primaryDeviceId)) {
                continue;
            }
            normalized.add(candidate);
        }
        return List.copyOf(normalized);
    }

    private List<String> resolveRegisterDeviceIds(String primaryDeviceId, List<String> legacyDeviceIds) {
        LinkedHashSet<String> deviceIds = new LinkedHashSet<>();
        if (hasText(primaryDeviceId)) {
            deviceIds.add(primaryDeviceId);
        }
        if (legacyDeviceIds != null && !legacyDeviceIds.isEmpty()) {
            deviceIds.addAll(legacyDeviceIds);
        }
        return List.copyOf(deviceIds);
    }

    private void schedulePostRegisterTasks(Long userId, String normalizedLoginId, String normalizedAccountSource) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            runPostRegisterTasks(userId, normalizedLoginId, normalizedAccountSource);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runPostRegisterTasks(userId, normalizedLoginId, normalizedAccountSource);
            }
        });
    }

    private void runPostRegisterTasks(Long userId, String normalizedLoginId, String normalizedAccountSource) {
        runPostRegisterTaskSafely("autoOpenCreditProducts", () -> autoOpenCreditProductsWithAgreement(userId));
        runPostRegisterTaskSafely("autoAddDefaultContact", () -> ensureAutoAddDefaultContactAndWelcome(userId, normalizedLoginId));
        runPostRegisterTaskSafely("publishRegisterWalletTopUp", () ->
                publishRegisterWalletTopUpRequested(userId, normalizedLoginId, normalizedAccountSource));
        runPostRegisterTaskSafely("clearRegisterSubmitAttempt", () ->
                userFlowAttemptGuard.clearRegisterSubmitAttempt(normalizedLoginId));
    }

    private void runPostRegisterTaskSafely(String taskName, Runnable task) {
        try {
            postRegisterRequiresNewTransactionTemplate.executeWithoutResult(status -> task.run());
        } catch (RuntimeException exception) {
            LOGGER.warn("register post task failed, taskName={}, message={}", taskName, exception.getMessage(), exception);
        }
    }

    private void publishRegisterWalletTopUpRequested(Long userId,
                                                     String loginId,
                                                     String accountSource) {
        if (userId == null || userId <= 0) {
            return;
        }
        String messageKey = REGISTER_WALLET_TOPUP_MESSAGE_KEY_PREFIX + "_" + userId;
        String payload = new RegisterInitialWalletTopUpPayload(
                String.valueOf(userId),
                loginId,
                accountSource
        ).toPayload();
        outboxPublisher.publishIfAbsent(
                AsyncMessageTopics.USER_REGISTER_INITIAL_WALLET_TOPUP_REQUESTED,
                messageKey,
                payload,
                REGISTER_WALLET_TOPUP_MAX_RETRY
        );
    }

    private void ensureAutoAddDefaultContactAndWelcome(Long userId, String normalizedLoginId) {
        String defaultContactLoginId = resolveAutoContactLoginId();
        if (defaultContactLoginId.equals(normalizedLoginId)) {
            return;
        }
        Long contactUserId = userFlowQueryPort.findByLoginId(defaultContactLoginId)
                .map(UserRegistrationView::userId)
                .orElseThrow(() -> new IllegalStateException("默认客服账号未配置: " + defaultContactLoginId));
        if (contactUserId.equals(userId)) {
            return;
        }
        if (contactFacade.isFriend(userId, contactUserId)) {
            return;
        }
        ContactRequestDTO request = contactFacade.applyFriendRequest(new ApplyFriendRequestCommand(
                userId,
                contactUserId,
                AUTO_CONTACT_APPLY_MESSAGE
        ));
        contactFacade.handleFriendRequest(new HandleFriendRequestCommand(
                contactUserId,
                request.requestNo(),
                "ACCEPT"
        ));
        messageFacade.sendTextMessage(new SendTextMessageCommand(
                contactUserId,
                userId,
                AUTO_CONTACT_WELCOME_MESSAGE,
                null
        ));
    }

    private String resolveAutoContactLoginId() {
        return appInfoRepository.findByAppCode(DEFAULT_APP_CODE)
                .map(appInfo -> normalizeOptional(appInfo.getDemoContactLoginId()))
                .filter(loginId -> loginId != null && !loginId.isBlank())
                .orElseThrow(() -> new IllegalStateException(
                        "应用配置缺少默认联系人登录号，请在 app_info.demo_contact_login_id 或后台管理-应用设置中配置"));
    }

    private void autoOpenCreditProductsWithAgreement(Long userId) {
        openAiCreditWithAgreement(userId);
        openAiLoanWithAgreement(userId);
    }

    private void openAiCreditWithAgreement(Long userId) {
        CreditProductOpenAgreementPackDTO pack = agreementFacade.getAiCreditOpenAgreementPack(userId);
        agreementFacade.openAiCreditWithAgreement(new OpenAiCreditWithAgreementCommand(
                userId,
                buildAutoOpenIdempotencyKey("AICREDIT", userId),
                toAgreementAcceptCommands(pack)
        ));
    }

    private void openAiLoanWithAgreement(Long userId) {
        CreditProductOpenAgreementPackDTO pack = agreementFacade.getAiLoanOpenAgreementPack(userId);
        agreementFacade.openAiLoanWithAgreement(new OpenAiLoanWithAgreementCommand(
                userId,
                buildAutoOpenIdempotencyKey("AILOAN", userId),
                toAgreementAcceptCommands(pack)
        ));
    }

    private List<AgreementAcceptCommand> toAgreementAcceptCommands(CreditProductOpenAgreementPackDTO pack) {
        if (pack == null || pack.agreements() == null || pack.agreements().isEmpty()) {
            throw new IllegalArgumentException("agreement templates not configured for credit product open");
        }
        return pack.agreements().stream()
                .map(this::toAgreementAcceptCommand)
                .toList();
    }

    private AgreementAcceptCommand toAgreementAcceptCommand(AgreementTemplateDTO template) {
        if (template == null || template.templateCode() == null || template.templateVersion() == null) {
            throw new IllegalArgumentException("agreement template is invalid");
        }
        return new AgreementAcceptCommand(template.templateCode(), template.templateVersion());
    }

    private String buildAutoOpenIdempotencyKey(String productCode, Long userId) {
        String normalizedProductCode = productCode == null ? "UNKNOWN" : productCode.trim().toUpperCase(Locale.ROOT);
        String key = AUTO_REG_IDEMPOTENCY_PREFIX + "_" + normalizedProductCode + "_" + userId;
        return key.length() > 64 ? key.substring(0, 64) : key;
    }

    private String normalizeUserTypeCode(String userTypeCode) {
        String normalized = normalizeOptional(userTypeCode);
        if (normalized == null) {
            return "01";
        }
        if (!normalized.matches("^(01|02|03|09)$")) {
            throw new IllegalArgumentException("userTypeCode仅支持01/02/03/09");
        }
        return normalized;
    }

    private String normalizeNickname(String nickname) {
        String normalized = normalizeOptional(nickname);
        return normalized == null ? "新用户" : normalized;
    }

    private String resolveRegisterNickname(String nickname,
                                           String normalizedRealName,
                                           String normalizedAccountSource) {
        if (ACCOUNT_SOURCE_DEMO.equals(normalizedAccountSource)) {
            String normalizedNickname = normalizeOptional(nickname);
            if (normalizedNickname != null) {
                return normalizedNickname;
            }
            if (hasText(normalizedRealName)) {
                return normalizedRealName;
            }
            return normalizeNickname(nickname);
        }
        if (hasText(normalizedRealName)) {
            return normalizedRealName;
        }
        return normalizeNickname(nickname);
    }

    private String normalizeCountryCode(String countryCode) {
        String normalized = normalizeOptional(countryCode);
        return normalized == null ? "86" : normalized;
    }

    private String normalizeIdCardNo(String idCardNo) {
        String normalized = normalizeOptional(idCardNo);
        if (normalized == null) {
            return null;
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeMobile(String mobile, String fallbackLoginId) {
        String normalized = normalizeOptional(mobile);
        return normalized == null ? fallbackLoginId : normalizeMainlandPhone(normalized);
    }

    private String normalizePassword(String password) {
        String normalized = normalizeOptional(password);
        if (normalized == null) {
            throw new IllegalArgumentException("loginPassword不能为空");
        }
        return normalized;
    }

    private String normalizeMainlandPhone(String rawLoginId) {
        if (rawLoginId == null) {
            throw new IllegalArgumentException("loginId不能为空");
        }
        String normalized = extractAsciiDigits(rawLoginId.trim());
        if (normalized.length() == 13 && normalized.startsWith("86")) {
            normalized = normalized.substring(2);
        } else if (normalized.length() == 15 && normalized.startsWith("0086")) {
            normalized = normalized.substring(4);
        }
        if (!normalized.matches("^1[3-9][0-9]{9}$")) {
            throw new IllegalArgumentException("loginId格式不正确");
        }
        return normalized;
    }

    private String extractAsciiDigits(String raw) {
        StringBuilder digits = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch >= '0' && ch <= '9') {
                digits.append(ch);
            } else if (ch >= '０' && ch <= '９') {
                digits.append((char) ('0' + (ch - '０')));
            }
        }
        return digits.toString();
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean hasText(String raw) {
        return normalizeOptional(raw) != null;
    }

    private String normalizeAccountSource(String accountSource) {
        String normalized = normalizeOptional(accountSource);
        if (normalized == null) {
            return ACCOUNT_SOURCE_REGISTER;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        if (ACCOUNT_SOURCE_DEMO.equals(upper)) {
            return ACCOUNT_SOURCE_DEMO;
        }
        return ACCOUNT_SOURCE_REGISTER;
    }
}
