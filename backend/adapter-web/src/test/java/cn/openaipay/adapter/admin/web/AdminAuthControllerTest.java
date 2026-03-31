package cn.openaipay.adapter.admin.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.adapter.admin.web.request.AdminLoginRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.admin.command.AdminLoginCommand;
import cn.openaipay.application.admin.dto.AdminLoginResultDTO;
import cn.openaipay.application.admin.facade.AdminFacade;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 后台认证控制器测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@ExtendWith(MockitoExtension.class)
class AdminAuthControllerTest {

    /** 后台认证门面。 */
    @Mock
    private AdminFacade adminFacade;

    /** 控制器。 */
    private AdminAuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminAuthController(adminFacade);
    }

    @Test
    void loginShouldMapRequestIntoCommandAndReturnFacadeResult() {
        when(adminFacade.login(any())).thenReturn(new AdminLoginResultDTO(
                "admin-token-001",
                "Bearer",
                7200,
                "9001",
                "admin",
                "运营管理员",
                List.of("SUPER_ADMIN"),
                List.of("admin.page_init", "user.account.list")
        ));

        ApiResponse<AdminLoginResultDTO> response = controller.login(new AdminLoginRequest("admin", "secret", "ios-debug"));

        ArgumentCaptor<AdminLoginCommand> commandCaptor = ArgumentCaptor.forClass(AdminLoginCommand.class);
        verify(adminFacade).login(commandCaptor.capture());
        AdminLoginCommand command = commandCaptor.getValue();

        assertEquals("admin", command.username());
        assertEquals("secret", command.password());
        assertEquals("ios-debug", command.deviceId());
        assertEquals(true, response.success());
        assertEquals("9001", response.data().adminId());
        assertEquals("运营管理员", response.data().displayName());
    }
}
