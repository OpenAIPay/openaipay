package cn.openaipay.application.auth.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.application.app.command.BindAppDeviceLoginUserCommand;
import cn.openaipay.application.app.dto.AppLoginDeviceAccountWhitelistDTO;
import cn.openaipay.application.app.facade.AppFacade;
import cn.openaipay.application.auth.command.LoginCommand;
import cn.openaipay.application.auth.dto.LoginPresetAccountDTO;
import cn.openaipay.application.auth.dto.LoginResultDTO;
import cn.openaipay.application.auth.exception.UnauthorizedException;
import cn.openaipay.application.auth.port.UserAuthQueryPort;
import cn.openaipay.application.auth.port.UserAuthView;
import cn.openaipay.application.auth.security.InMemoryAttemptGuardStateStore;
import cn.openaipay.application.auth.security.LoginAttemptGuard;
import cn.openaipay.application.auth.service.AuthService;
import cn.openaipay.application.auth.service.impl.AuthServiceImpl;
import cn.openaipay.domain.shared.security.impl.CredentialDomainServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * AuthFacadeImplTest 门面行为测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@ExtendWith(MockitoExtension.class)
class AuthFacadeImplTest {

    /** SHA-256 secret 摘要。 */
    private static final String SHA256_SECRET_HASH =
            "2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b";

    /** 用户查询端口。 */
    @Mock
    private UserAuthQueryPort userAuthQueryPort;
    /** app 门面。 */
    @Mock
    private AppFacade appFacade;

    /** 凭证领域服务。 */
    private CredentialDomainServiceImpl credentialDomainService;
    /** 认证门面。 */
    private AuthFacadeImpl facade;

    @BeforeEach
    void setUp() {
        credentialDomainService = new CredentialDomainServiceImpl("auth-facade-test-secret", 3600);
        AuthService authService = new AuthServiceImpl(
                userAuthQueryPort,
                new LoginAttemptGuard(new InMemoryAttemptGuardStateStore()),
                credentialDomainService,
                appFacade
        );
        facade = new AuthFacadeImpl(authService);
        lenient().when(appFacade.isLoginDeviceBindingCheckEnabled("OPENAIPAY_IOS")).thenReturn(true);
    }

    @Test
    void mobileVerifyLoginShouldReturnAccessTokenAndBindDeviceViaFacade() {
        UserAuthView user = activeUser("13900000000");
        when(userAuthQueryPort.findByLoginId("13900000000")).thenReturn(Optional.of(user));
        when(appFacade.isLoginWhitelistAllowed("OPENAIPAY_IOS", "ios-debug", "13900000000")).thenReturn(true);
        when(appFacade.queryBoundDeviceId(user.userId(), user.loginId())).thenReturn("ios-locked-device");

        LoginResultDTO result = facade.mobileVerifyLogin(new LoginCommand(" 13900000000 ", "ios-debug", List.of()));

        ArgumentCaptor<BindAppDeviceLoginUserCommand> commandCaptor =
                ArgumentCaptor.forClass(BindAppDeviceLoginUserCommand.class);
        verify(appFacade).bindLoginUser(commandCaptor.capture());

        BindAppDeviceLoginUserCommand bindCommand = commandCaptor.getValue();
        assertNotNull(result.accessToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(604800, result.expiresInSeconds());
        assertEquals(user.userId(), result.userId());
        assertEquals(user.aipayUid(), result.aipayUid());
        assertEquals(user.nickname(), result.nickname());
        assertEquals(user.userId(), credentialDomainService.resolveSubjectIdFromAccessToken(result.accessToken()));
        assertEquals("ios-debug", bindCommand.deviceId());
        assertEquals(user.userId(), bindCommand.userId());
        assertEquals(user.loginId(), bindCommand.loginId());
        assertEquals(user.nickname(), bindCommand.nickname());
    }

    @Test
    void mobileVerifyLoginShouldLockAfterFiveFailuresViaFacade() {
        when(userAuthQueryPort.findByLoginId("13920000002")).thenReturn(Optional.empty());
        LoginCommand command = new LoginCommand("13920000002", "ios-debug", List.of());

        for (int i = 0; i < 5; i++) {
            UnauthorizedException exception =
                    assertThrows(UnauthorizedException.class, () -> facade.mobileVerifyLogin(command));
            assertEquals("账号或密码错误", exception.getMessage());
        }

        UnauthorizedException lockedException =
                assertThrows(UnauthorizedException.class, () -> facade.mobileVerifyLogin(command));

        assertEquals("登录失败次数过多，请稍后再试", lockedException.getMessage());
        verify(userAuthQueryPort, times(5)).findByLoginId("13920000002");
    }

    @Test
    void mobileVerifyLoginShouldRejectWhenDeviceIdIsBlank() {
        UnauthorizedException exception =
                assertThrows(UnauthorizedException.class, () -> facade.mobileVerifyLogin(new LoginCommand("13920000002", "  ", List.of())));
        assertEquals("登录设备不能为空", exception.getMessage());
    }

    @Test
    void mobileVerifyLoginShouldRejectWhenDeviceMismatch() {
        UserAuthView user = activeUser("13900000000");
        when(userAuthQueryPort.findByLoginId("13900000000")).thenReturn(Optional.of(user));
        when(appFacade.isLoginWhitelistAllowed("OPENAIPAY_IOS", "ios-new-device", "13900000000")).thenReturn(false);
        when(appFacade.queryBoundDeviceId(user.userId(), user.loginId())).thenReturn("ios-locked-device");

        UnauthorizedException exception =
                assertThrows(UnauthorizedException.class, () -> facade.mobileVerifyLogin(new LoginCommand("13900000000", "ios-new-device", List.of())));

        assertEquals("该账号仅支持在注册的手机上登录", exception.getMessage());
    }

    @Test
    void mobileVerifyLoginShouldRejectFormerTrustedLoginIdAcrossDevicesWhenNotWhitelisted() {
        UserAuthView user = activeUser("13920000001");
        when(userAuthQueryPort.findByLoginId("13920000001")).thenReturn(Optional.of(user));
        when(appFacade.isLoginWhitelistConfigured("OPENAIPAY_IOS", "13920000001")).thenReturn(true);
        when(appFacade.isLoginWhitelistAllowed("OPENAIPAY_IOS", "ios-new-device", "13920000001")).thenReturn(false);

        UnauthorizedException exception =
                assertThrows(UnauthorizedException.class, () -> facade.mobileVerifyLogin(new LoginCommand("13920000001", "ios-new-device", List.of())));

        assertEquals("该账号仅支持在注册的手机上登录", exception.getMessage());
    }

    @Test
    void mobileVerifyLoginShouldAllowWhitelistedDeviceForFormerTrustedLoginId() {
        UserAuthView user = activeUser("+86 13920000001");
        when(userAuthQueryPort.findByLoginId("13920000001")).thenReturn(Optional.of(user));
        when(appFacade.isLoginWhitelistConfigured("OPENAIPAY_IOS", "+86 13920000001")).thenReturn(true);
        when(appFacade.isLoginWhitelistAllowed("OPENAIPAY_IOS", "ios-simulator-device", "+86 13920000001")).thenReturn(true);

        LoginResultDTO result = facade.mobileVerifyLogin(new LoginCommand("13920000001", "ios-simulator-device", List.of()));

        ArgumentCaptor<BindAppDeviceLoginUserCommand> commandCaptor =
                ArgumentCaptor.forClass(BindAppDeviceLoginUserCommand.class);
        verify(appFacade).bindLoginUser(commandCaptor.capture());
        assertNotNull(result.accessToken());
        assertEquals("ios-simulator-device", commandCaptor.getValue().deviceId());
    }

    @Test
    void mobileVerifyLoginShouldAllowLegacyWhitelistedDeviceAndBindCurrentStableDevice() {
        UserAuthView user = activeUser("13920000001");
        when(userAuthQueryPort.findByLoginId("13920000001")).thenReturn(Optional.of(user));
        when(appFacade.isLoginWhitelistConfigured("OPENAIPAY_IOS", "13920000001")).thenReturn(true);
        when(appFacade.isLoginWhitelistAllowed("OPENAIPAY_IOS", "ios-device-installation-001", "13920000001")).thenReturn(false);
        when(appFacade.isLoginWhitelistAllowed("OPENAIPAY_IOS", "ios-device-vendor-legacy", "13920000001")).thenReturn(true);

        LoginResultDTO result = facade.mobileVerifyLogin(new LoginCommand(
                "13920000001",
                "ios-device-installation-001",
                List.of("ios-device-vendor-legacy")
        ));

        assertNotNull(result.accessToken());
        verify(appFacade).bindLoginWhitelistAccount(
                "OPENAIPAY_IOS",
                "ios-device-installation-001",
                "13920000001",
                user.nickname()
        );
    }

    @Test
    void listPresetLoginAccountsShouldMergeLegacyDeviceWhitelist() {
        when(appFacade.listLoginWhitelistAccounts("OPENAIPAY_IOS", "ios-device-installation-001"))
                .thenReturn(List.of());
        when(appFacade.listLoginWhitelistAccounts("OPENAIPAY_IOS", "ios-device-vendor-legacy"))
                .thenReturn(List.of(new AppLoginDeviceAccountWhitelistDTO("13920000001", "顾郡")));

        List<?> result = facade.listPresetLoginAccounts(
                "ios-device-installation-001",
                List.of("ios-device-vendor-legacy")
        );

        assertEquals(1, result.size());
        LoginPresetAccountDTO account = (LoginPresetAccountDTO) result.get(0);
        assertEquals("13920000001", account.loginId());
        assertEquals("顾郡", account.nickname());
    }

    private UserAuthView activeUser(String loginId) {
        return new UserAuthView(
                880109000000000001L,
                "880109000000000001",
                loginId,
                "ACTIVE",
                "L2",
                SHA256_SECRET_HASH,
                "顾郡",
                "/api/media/gujun.png",
                loginId,
                "顾**",
                "4401**********0001",
                "86",
                "F",
                "广州"
        );
    }
}
