package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.AdminRequestContext;
import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.admin.dto.AdminMenuDTO;
import cn.openaipay.application.admin.dto.AdminPageInitDTO;
import cn.openaipay.application.admin.pageinit.facade.AdminPageInitFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台管理页面初始化控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/admin")
public class AdminPageInitController {

    /** AdminPageInitFacade组件 */
    private final AdminPageInitFacade adminPageInitFacade;
    /** 管理请求上下文 */
    private final AdminRequestContext adminRequestContext;

    public AdminPageInitController(AdminPageInitFacade adminPageInitFacade,
                                   AdminRequestContext adminRequestContext) {
        this.adminPageInitFacade = adminPageInitFacade;
        this.adminRequestContext = adminRequestContext;
    }

    /**
     * 处理页面初始化信息。
     */
    @GetMapping("/page-init")
    @RequireAdminPermission("admin.page_init")
    public ApiResponse<AdminPageInitDTO> pageInit(@RequestParam(value = "adminId", required = false) Long adminId) {
        return ApiResponse.success(adminPageInitFacade.pageInit(resolveAdminId(adminId)));
    }

    /**
     * 处理菜单信息。
     */
    @GetMapping("/menus")
    @RequireAdminPermission("admin.menu.list")
    public ApiResponse<List<AdminMenuDTO>> menus(@RequestParam(value = "adminId", required = false) Long adminId) {
        return ApiResponse.success(adminPageInitFacade.listVisibleMenus(resolveAdminId(adminId)));
    }

    private Long resolveAdminId(Long requestAdminId) {
        Long currentAdminId = adminRequestContext.currentAdminId();
        if (currentAdminId != null) {
            return currentAdminId;
        }
        return requestAdminId;
    }
}
