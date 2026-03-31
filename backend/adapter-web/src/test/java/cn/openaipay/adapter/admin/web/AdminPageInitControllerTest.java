package cn.openaipay.adapter.admin.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.adapter.admin.security.AdminRequestContext;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.admin.dto.AdminMenuDTO;
import cn.openaipay.application.admin.dto.AdminPageInitDTO;
import cn.openaipay.application.admin.dto.AdminProfileDTO;
import cn.openaipay.application.admin.pageinit.facade.AdminPageInitFacade;
import cn.openaipay.application.coupon.dto.CouponOpsSummaryDTO;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * AdminPageInitControllerTest 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@ExtendWith(MockitoExtension.class)
class AdminPageInitControllerTest {

    /** 后台信息 */
    @Mock
    private AdminPageInitFacade adminPageInitFacade;
    /** 后台请求信息 */
    @Mock
    private AdminRequestContext adminRequestContext;

    /** controller信息 */
    private AdminPageInitController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminPageInitController(adminPageInitFacade, adminRequestContext);
    }

    @Test
    void pageInitShouldPreferCurrentAdminIdFromContext() {
        AdminPageInitDTO dto = pageInit();
        when(adminRequestContext.currentAdminId()).thenReturn(9001L);
        when(adminPageInitFacade.pageInit(9001L)).thenReturn(dto);

        ApiResponse<AdminPageInitDTO> response = controller.pageInit(1000L);

        verify(adminPageInitFacade).pageInit(9001L);
        assertEquals(true, response.success());
        assertEquals("9001", response.data().admin().adminId());
    }

    @Test
    void menusShouldFallbackToRequestAdminIdWhenContextIsEmpty() {
        when(adminRequestContext.currentAdminId()).thenReturn(null);
        when(adminPageInitFacade.listVisibleMenus(1000L)).thenReturn(List.of(
                new AdminMenuDTO("message-center", null, "消息中心", "/admin/messages", "message", 2)
        ));

        ApiResponse<List<AdminMenuDTO>> response = controller.menus(1000L);

        verify(adminPageInitFacade).listVisibleMenus(1000L);
        assertEquals(true, response.success());
        assertEquals(1, response.data().size());
        assertEquals("/admin/messages", response.data().get(0).path());
    }

    private AdminPageInitDTO pageInit() {
        return new AdminPageInitDTO(
                new AdminProfileDTO(
                        "9001",
                        "admin",
                        "运营管理员",
                        "ACTIVE",
                        LocalDateTime.of(2026, 3, 15, 9, 0)
                ),
                List.of(new AdminMenuDTO("dashboard", null, "工作台", "/admin/dashboard", "dashboard", 1)),
                new CouponOpsSummaryDTO(8, 2, 1, 0, 128),
                List.of("SUPER_ADMIN"),
                List.of("admin.page_init")
        );
    }
}
