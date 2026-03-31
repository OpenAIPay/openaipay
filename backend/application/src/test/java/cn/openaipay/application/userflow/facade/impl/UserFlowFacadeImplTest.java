package cn.openaipay.application.userflow.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.application.agreement.command.OpenAiCreditWithAgreementCommand;
import cn.openaipay.application.agreement.command.OpenAiLoanWithAgreementCommand;
import cn.openaipay.application.agreement.dto.AgreementTemplateDTO;
import cn.openaipay.application.agreement.dto.CreditProductOpenAgreementPackDTO;
import cn.openaipay.application.agreement.facade.AgreementFacade;
import cn.openaipay.application.auth.security.InMemoryAttemptGuardStateStore;
import cn.openaipay.application.auth.exception.TooManyRequestsException;
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
import cn.openaipay.application.userflow.dto.RegisterPhoneCheckDTO;
import cn.openaipay.application.userflow.dto.UserRegistrationDTO;
import cn.openaipay.application.userflow.port.UserFlowQueryPort;
import cn.openaipay.application.userflow.port.UserRegistrationView;
import cn.openaipay.application.userflow.security.UserFlowAttemptGuard;
import cn.openaipay.application.userflow.service.UserFlowService;
import cn.openaipay.application.userflow.service.impl.UserFlowServiceImpl;
import cn.openaipay.domain.app.model.AppInfo;
import cn.openaipay.domain.app.model.AppLoginDeviceAccountWhitelist;
import cn.openaipay.domain.app.model.AppStatus;
import cn.openaipay.domain.app.repository.AppInfoRepository;
import cn.openaipay.domain.app.repository.AppLoginDeviceAccountWhitelistRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

/**
 * UserFlowFacadeImplTest 门面行为测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@ExtendWith(MockitoExtension.class)
class UserFlowFacadeImplTest {

    /** 新注册用户 ID。 */
    private static final Long NEW_USER_ID = 880109000000000019L;

    /** 用户服务。 */
    @Mock
    private UserService userService;
    /** 注册查询端口。 */
    @Mock
    private UserFlowQueryPort userFlowQueryPort;
    /** 用户凭证写入端口。 */
    @Mock
    private UserCredentialPort userCredentialPort;
    /** 实名门面。 */
    @Mock
    private KycFacade kycFacade;
    /** 协议门面。 */
    @Mock
    private AgreementFacade agreementFacade;
    /** 联系人门面。 */
    @Mock
    private ContactFacade contactFacade;
    /** 消息门面。 */
    @Mock
    private MessageFacade messageFacade;
    /** 异步消息发布器。 */
    @Mock
    private OutboxPublisher outboxPublisher;
    /** 登录设备白名单账号仓储。 */
    @Mock
    private AppLoginDeviceAccountWhitelistRepository appLoginDeviceAccountWhitelistRepository;
    /** 应用信息仓储。 */
    @Mock
    private AppInfoRepository appInfoRepository;
    /** 事务管理器。 */
    @Mock
    private PlatformTransactionManager transactionManager;

    /** 用户流程门面。 */
    private UserFlowFacadeImpl facade;

    @BeforeEach
    void setUp() {
        lenient().when(appInfoRepository.findByAppCode("OPENAIPAY_IOS")).thenReturn(Optional.of(new AppInfo(
                1L,
                "OPENAIPAY_IOS",
                "AiPay iOS",
                AppStatus.ENABLED,
                true,
                true,
                true,
                "13920000004",
                "13800000000",
                "888888",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        )));
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        UserFlowService userFlowService = new UserFlowServiceImpl(
                userService,
                outboxPublisher,
                userFlowQueryPort,
                userCredentialPort,
                kycFacade,
                agreementFacade,
                contactFacade,
                messageFacade,
                new UserFlowAttemptGuard(new InMemoryAttemptGuardStateStore()),
                appLoginDeviceAccountWhitelistRepository,
                appInfoRepository,
                transactionManager
        );
        facade = new UserFlowFacadeImpl(userFlowService);
    }

    @Test
    void checkRegisterPhoneShouldReturnMaskedResultAndApplyRateLimitViaFacade() {
        for (int i = 0; i < 24; i++) {
            RegisterPhoneCheckDTO result = facade.checkRegisterPhone(" 13920000002 ", "ios-device-installation-001");
            assertFalse(result.userExists());
            assertFalse(result.realNameVerified());
            assertNull(result.kycLevel());
        }

        TooManyRequestsException exception =
                assertThrows(TooManyRequestsException.class, () -> facade.checkRegisterPhone("13920000002", "ios-device-installation-001"));

        assertEquals("请求过于频繁，请稍后再试", exception.getMessage());
    }

    @Test
    void registerShouldCreateUserInitializeCredentialAndOpenProductsViaFacade() {
        when(userFlowQueryPort.findByLoginId("13920000002")).thenReturn(Optional.empty());
        when(userService.createCoreUser(any(CreateUserCommand.class))).thenReturn(NEW_USER_ID);
        when(agreementFacade.getAiCreditOpenAgreementPack(NEW_USER_ID))
                .thenReturn(agreementPack(NEW_USER_ID, "AICREDIT"));
        when(agreementFacade.getAiLoanOpenAgreementPack(NEW_USER_ID))
                .thenReturn(agreementPack(NEW_USER_ID, "AILOAN"));

        UserRegistrationDTO result = facade.register(new RegisterUserCommand(
                "ios-device-installation-001",
                List.of(),
                "+86 13920000002",
                null,
                null,
                null,
                "/api/media/gujun.png",
                null,
                null,
                "顾郡",
                "440101199001010011",
                " secret "
        ));

        ArgumentCaptor<CreateUserCommand> createUserCaptor = ArgumentCaptor.forClass(CreateUserCommand.class);
        ArgumentCaptor<SubmitKycCommand> submitKycCaptor = ArgumentCaptor.forClass(SubmitKycCommand.class);
        ArgumentCaptor<OpenAiCreditWithAgreementCommand> creditCaptor =
                ArgumentCaptor.forClass(OpenAiCreditWithAgreementCommand.class);
        ArgumentCaptor<OpenAiLoanWithAgreementCommand> loanCaptor =
                ArgumentCaptor.forClass(OpenAiLoanWithAgreementCommand.class);
        ArgumentCaptor<AppLoginDeviceAccountWhitelist> whitelistCaptor =
                ArgumentCaptor.forClass(AppLoginDeviceAccountWhitelist.class);

        verify(userService).createCoreUser(createUserCaptor.capture());
        verify(userCredentialPort).initializeLoginPassword(NEW_USER_ID, "secret");
        verify(kycFacade).submit(submitKycCaptor.capture());
        verify(agreementFacade).openAiCreditWithAgreement(creditCaptor.capture());
        verify(agreementFacade).openAiLoanWithAgreement(loanCaptor.capture());
        verify(appLoginDeviceAccountWhitelistRepository).save(whitelistCaptor.capture());
        verify(outboxPublisher).publishIfAbsent(
                org.mockito.ArgumentMatchers.eq(AsyncMessageTopics.USER_REGISTER_INITIAL_WALLET_TOPUP_REQUESTED),
                org.mockito.ArgumentMatchers.eq("REGISTER_WALLET_TOPUP_" + NEW_USER_ID),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(12)
        );

        CreateUserCommand createUserCommand = createUserCaptor.getValue();
        assertEquals("13920000002", createUserCommand.loginId());
        assertEquals("01", createUserCommand.userTypeCode());
        assertEquals("REGISTER", createUserCommand.accountSource());
        assertEquals("顾郡", createUserCommand.nickname());
        assertEquals("/api/media/gujun.png", createUserCommand.avatarUrl());
        assertEquals("86", createUserCommand.countryCode());
        assertEquals("13920000002", createUserCommand.mobile());

        SubmitKycCommand submitKycCommand = submitKycCaptor.getValue();
        assertEquals(NEW_USER_ID, submitKycCommand.userId());
        assertEquals("顾郡", submitKycCommand.realName());
        assertEquals("440101199001010011", submitKycCommand.idCardNo());

        OpenAiCreditWithAgreementCommand creditCommand = creditCaptor.getValue();
        OpenAiLoanWithAgreementCommand loanCommand = loanCaptor.getValue();
        AppLoginDeviceAccountWhitelist whitelist = whitelistCaptor.getValue();
        assertEquals(NEW_USER_ID, creditCommand.userId());
        assertEquals(NEW_USER_ID, loanCommand.userId());
        assertEquals(1, creditCommand.agreementAccepts().size());
        assertEquals(1, loanCommand.agreementAccepts().size());
        assertEquals("AUTO_REG_AICREDIT_" + NEW_USER_ID, creditCommand.idempotencyKey());
        assertEquals("AUTO_REG_AILOAN_" + NEW_USER_ID, loanCommand.idempotencyKey());
        assertEquals("OPENAIPAY_IOS", whitelist.getAppCode());
        assertEquals("ios-device-installation-001", whitelist.getDeviceId());
        assertEquals("13920000002", whitelist.getLoginId());

        assertEquals(NEW_USER_ID, result.userId());
        assertEquals(String.valueOf(NEW_USER_ID), result.aipayUid());
        assertEquals("13920000002", result.loginId());
        assertEquals(true, result.kycSubmitted());
    }

    @Test
    void registerShouldAutoAddDefaultContactAndSendWelcomeMessageViaFacade() {
        Long defaultContactUserId = 880102069981881102L;
        when(userFlowQueryPort.findByLoginId("13920000004")).thenReturn(Optional.empty());
        when(userFlowQueryPort.findByLoginId("13800000000"))
                .thenReturn(Optional.of(new UserRegistrationView(defaultContactUserId, "13800000000", "L2")));
        when(userService.createCoreUser(any(CreateUserCommand.class))).thenReturn(NEW_USER_ID);
        when(agreementFacade.getAiCreditOpenAgreementPack(NEW_USER_ID))
                .thenReturn(agreementPack(NEW_USER_ID, "AICREDIT"));
        when(agreementFacade.getAiLoanOpenAgreementPack(NEW_USER_ID))
                .thenReturn(agreementPack(NEW_USER_ID, "AILOAN"));
        when(contactFacade.isFriend(NEW_USER_ID, defaultContactUserId)).thenReturn(false);
        when(contactFacade.applyFriendRequest(any(ApplyFriendRequestCommand.class)))
                .thenReturn(new ContactRequestDTO(
                        "REQ_AUTO_001",
                        NEW_USER_ID,
                        defaultContactUserId,
                        null,
                        null,
                        null,
                        null,
                        "注册自动添加客服",
                        "PENDING",
                        null,
                        null,
                        LocalDateTime.now()
                ));

        UserRegistrationDTO result = facade.register(new RegisterUserCommand(
                "ios-device-installation-001",
                List.of(),
                "13920000004",
                "01",
                null,
                "测试用户",
                null,
                "86",
                "13920000004",
                "测试用户",
                "440101199001010011",
                "secret"
        ));

        ArgumentCaptor<ApplyFriendRequestCommand> applyCaptor = ArgumentCaptor.forClass(ApplyFriendRequestCommand.class);
        ArgumentCaptor<HandleFriendRequestCommand> handleCaptor = ArgumentCaptor.forClass(HandleFriendRequestCommand.class);
        ArgumentCaptor<SendTextMessageCommand> textCaptor = ArgumentCaptor.forClass(SendTextMessageCommand.class);

        verify(contactFacade).applyFriendRequest(applyCaptor.capture());
        verify(contactFacade).handleFriendRequest(handleCaptor.capture());
        verify(messageFacade).sendTextMessage(textCaptor.capture());
        verify(transactionManager, org.mockito.Mockito.atLeast(4)).getTransaction(any());
        verify(transactionManager, org.mockito.Mockito.atLeast(4)).commit(any());

        ApplyFriendRequestCommand applyCommand = applyCaptor.getValue();
        assertEquals(NEW_USER_ID, applyCommand.requesterUserId());
        assertEquals(defaultContactUserId, applyCommand.targetUserId());
        assertEquals("注册自动添加客服", applyCommand.applyMessage());

        HandleFriendRequestCommand handleCommand = handleCaptor.getValue();
        assertEquals(defaultContactUserId, handleCommand.operatorUserId());
        assertEquals("REQ_AUTO_001", handleCommand.requestNo());
        assertEquals("ACCEPT", handleCommand.action());

        SendTextMessageCommand textCommand = textCaptor.getValue();
        assertEquals(defaultContactUserId, textCommand.senderUserId());
        assertEquals(NEW_USER_ID, textCommand.receiverUserId());
        assertEquals("您好！有什么问题可以帮您？", textCommand.contentText());
        assertNull(textCommand.extPayload());

        assertEquals(NEW_USER_ID, result.userId());
        assertEquals("13920000004", result.loginId());
    }

    @Test
    void registerShouldThrottleAfterRepeatedFailuresAndClearAfterSuccessViaFacade() {
        AtomicInteger lookupCounter = new AtomicInteger();
        when(userFlowQueryPort.findByLoginId("13920000002")).thenAnswer(invocation -> {
            int current = lookupCounter.getAndIncrement();
            if (current < 7) {
                return Optional.of(new UserRegistrationView(1L, "13920000002", "L0"));
            }
            if (current == 7) {
                return Optional.empty();
            }
            return Optional.of(new UserRegistrationView(2L, "13920000002", "L0"));
        });
        when(userService.createCoreUser(any(CreateUserCommand.class))).thenReturn(NEW_USER_ID);
        when(agreementFacade.getAiCreditOpenAgreementPack(NEW_USER_ID))
                .thenReturn(agreementPack(NEW_USER_ID, "AICREDIT"));
        when(agreementFacade.getAiLoanOpenAgreementPack(NEW_USER_ID))
                .thenReturn(agreementPack(NEW_USER_ID, "AILOAN"));

        RegisterUserCommand command = new RegisterUserCommand(
                "ios-device-installation-001",
                List.of(),
                "13920000002",
                "01",
                null,
                "顾郡",
                null,
                "86",
                "13920000002",
                "顾郡",
                "440101199001010011",
                "secret"
        );

        for (int i = 0; i < 7; i++) {
            IllegalArgumentException exception =
                    assertThrows(IllegalArgumentException.class, () -> facade.register(command));
            assertEquals("注册失败，请检查信息后重试或直接登录", exception.getMessage());
        }

        UserRegistrationDTO success = facade.register(command);
        IllegalArgumentException afterSuccess =
                assertThrows(IllegalArgumentException.class, () -> facade.register(command));

        assertEquals(NEW_USER_ID, success.userId());
        assertEquals("注册失败，请检查信息后重试或直接登录", afterSuccess.getMessage());
    }

    @Test
    void registerShouldRejectWhenSubmitAttemptsExceededLimitViaFacade() {
        when(userFlowQueryPort.findByLoginId("13920000002"))
                .thenReturn(Optional.of(new UserRegistrationView(1L, "13920000002", "L0")));

        RegisterUserCommand command = new RegisterUserCommand(
                "ios-device-installation-001",
                List.of(),
                "13920000002",
                "01",
                null,
                "顾郡",
                null,
                "86",
                "13920000002",
                "顾郡",
                "440101199001010011",
                "secret"
        );

        for (int i = 0; i < 8; i++) {
            IllegalArgumentException exception =
                    assertThrows(IllegalArgumentException.class, () -> facade.register(command));
            assertEquals("注册失败，请检查信息后重试或直接登录", exception.getMessage());
        }

        TooManyRequestsException exception =
                assertThrows(TooManyRequestsException.class, () -> facade.register(command));

        assertEquals("请求过于频繁，请稍后再试", exception.getMessage());
    }

    @Test
    void registerShouldRejectWhenRegisterIdCardAlreadyUsedViaFacade() {
        when(userFlowQueryPort.findByLoginId("13920000002")).thenReturn(Optional.empty());
        when(userFlowQueryPort.existsRegisterAccountByIdCardNo("440101199001010011")).thenReturn(true);

        RegisterUserCommand command = new RegisterUserCommand(
                "ios-device-installation-001",
                List.of(),
                "13920000002",
                "01",
                "REGISTER",
                "顾郡",
                null,
                "86",
                "13920000002",
                "顾郡",
                "440101199001010011",
                "secret"
        );

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> facade.register(command));

        assertEquals("该身份证号已被注册", exception.getMessage());
        verify(userService, never()).createCoreUser(any(CreateUserCommand.class));
    }

    @Test
    void registerDemoShouldUseGeneratedNicknameAndKeepRealNameForKycViaFacade() {
        Long defaultContactUserId = 880102069981881102L;
        when(userFlowQueryPort.findByLoginId("13920000004")).thenReturn(Optional.empty());
        when(userFlowQueryPort.findByLoginId("13800000000"))
                .thenReturn(Optional.of(new UserRegistrationView(defaultContactUserId, "13800000000", "L2")));
        when(contactFacade.isFriend(NEW_USER_ID, defaultContactUserId)).thenReturn(true);
        when(userService.createCoreUser(any(CreateUserCommand.class))).thenReturn(NEW_USER_ID);
        when(agreementFacade.getAiCreditOpenAgreementPack(NEW_USER_ID))
                .thenReturn(agreementPack(NEW_USER_ID, "AICREDIT"));
        when(agreementFacade.getAiLoanOpenAgreementPack(NEW_USER_ID))
                .thenReturn(agreementPack(NEW_USER_ID, "AILOAN"));

        facade.register(new RegisterUserCommand(
                "ios-device-installation-001",
                List.of(),
                "13920000004",
                "01",
                "DEMO",
                "演示6666",
                null,
                "86",
                "13920000004",
                "顾郡",
                "440101199001010011",
                "secret"
        ));

        ArgumentCaptor<CreateUserCommand> createUserCaptor = ArgumentCaptor.forClass(CreateUserCommand.class);
        ArgumentCaptor<SubmitKycCommand> submitKycCaptor = ArgumentCaptor.forClass(SubmitKycCommand.class);
        verify(userService).createCoreUser(createUserCaptor.capture());
        verify(kycFacade).submit(submitKycCaptor.capture());

        CreateUserCommand createUserCommand = createUserCaptor.getValue();
        SubmitKycCommand submitKycCommand = submitKycCaptor.getValue();
        assertEquals("DEMO", createUserCommand.accountSource());
        assertEquals("演示6666", createUserCommand.nickname());
        assertEquals("顾郡", submitKycCommand.realName());
    }

    @Test
    void registerShouldRejectDemoWhenMissingKycViaFacade() {
        when(userFlowQueryPort.findByLoginId("13920000002")).thenReturn(Optional.empty());

        RegisterUserCommand command = new RegisterUserCommand(
                "ios-device-installation-001",
                List.of(),
                "13920000002",
                "01",
                "DEMO",
                "顾郡",
                null,
                "86",
                "13920000002",
                null,
                null,
                "secret"
        );

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> facade.register(command));

        assertEquals("注册必须包含实名信息", exception.getMessage());
        verify(userService, never()).createCoreUser(any(CreateUserCommand.class));
    }

    @Test
    void registerShouldRejectWhenDeviceRegisteredAccountsReachedLimitViaFacade() {
        when(userFlowQueryPort.findByLoginId("13920000002")).thenReturn(Optional.empty());
        when(appLoginDeviceAccountWhitelistRepository.listEnabledByAppCodeAndDeviceId("OPENAIPAY_IOS", "ios-demo-001"))
                .thenReturn(List.of(
                        new AppLoginDeviceAccountWhitelist(1L, "OPENAIPAY_IOS", "ios-demo-001", "13800000001", "用户1", true, LocalDateTime.now(), LocalDateTime.now()),
                        new AppLoginDeviceAccountWhitelist(2L, "OPENAIPAY_IOS", "ios-demo-001", "13800000002", "用户2", true, LocalDateTime.now(), LocalDateTime.now()),
                        new AppLoginDeviceAccountWhitelist(3L, "OPENAIPAY_IOS", "ios-demo-001", "13800000003", "用户3", true, LocalDateTime.now(), LocalDateTime.now())
                ));

        RegisterUserCommand command = new RegisterUserCommand(
                "ios-demo-001",
                List.of(),
                "13920000002",
                "01",
                "REGISTER",
                "顾郡",
                null,
                "86",
                "13920000002",
                "顾郡",
                "440101199001010011",
                "secret"
        );

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> facade.register(command));

        assertEquals("同一设备最多注册3个账号", exception.getMessage());
        verify(userService, never()).createCoreUser(any(CreateUserCommand.class));
    }

    @Test
    void registerShouldBypassDeviceLimitForExemptDeviceViaFacade() {
        when(userFlowQueryPort.findByLoginId("13920000002")).thenReturn(Optional.empty());
        when(appLoginDeviceAccountWhitelistRepository.existsEnabledByAppCodeAndDeviceIdAndLoginId(
                eq("OPENAIPAY_IOS"), eq("ios-demo-001"), anyString()
        )).thenReturn(true);
        when(userService.createCoreUser(any(CreateUserCommand.class))).thenReturn(NEW_USER_ID);
        when(agreementFacade.getAiCreditOpenAgreementPack(NEW_USER_ID))
                .thenReturn(agreementPack(NEW_USER_ID, "AICREDIT"));
        when(agreementFacade.getAiLoanOpenAgreementPack(NEW_USER_ID))
                .thenReturn(agreementPack(NEW_USER_ID, "AILOAN"));

        UserRegistrationDTO result = facade.register(new RegisterUserCommand(
                "ios-demo-001",
                List.of(),
                "13920000002",
                "01",
                "REGISTER",
                "顾郡",
                null,
                "86",
                "13920000002",
                "顾郡",
                "440101199001010011",
                "secret"
        ));

        assertEquals(NEW_USER_ID, result.userId());
        verify(userService).createCoreUser(any(CreateUserCommand.class));
    }

    private CreditProductOpenAgreementPackDTO agreementPack(Long userId, String productCode) {
        return new CreditProductOpenAgreementPackDTO(
                userId,
                "OPEN",
                productCode,
                List.of(new AgreementTemplateDTO(
                        productCode + "_MAIN",
                        "1.0.0",
                        "OPEN",
                        productCode + "主协议",
                        "https://example.com/" + productCode.toLowerCase(),
                        "hash-" + productCode,
                        true
                ))
        );
    }
}
