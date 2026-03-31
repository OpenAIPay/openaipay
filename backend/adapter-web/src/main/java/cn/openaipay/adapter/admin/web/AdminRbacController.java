package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.AdminRequestContext;
import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.admin.web.request.AssignAdminRolesRequest;
import cn.openaipay.adapter.admin.web.request.AssignRoleMenusRequest;
import cn.openaipay.adapter.admin.web.request.AssignRolePermissionsRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.admin.command.AssignAdminRolesCommand;
import cn.openaipay.application.admin.command.AssignRoleMenusCommand;
import cn.openaipay.application.admin.command.AssignRolePermissionsCommand;
import cn.openaipay.application.admin.dto.AdminAccountSummaryDTO;
import cn.openaipay.application.admin.facade.AdminRbacFacade;
import cn.openaipay.application.admin.dto.AdminAuthorizationDTO;
import cn.openaipay.application.admin.dto.AdminMenuDTO;
import cn.openaipay.application.admin.dto.AdminModuleDTO;
import cn.openaipay.application.admin.dto.AdminPermissionDTO;
import cn.openaipay.application.admin.dto.AdminRoleDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台管理RBAC控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/admin/rbac")
public class AdminRbacController {

    /** AdminRbacFacade组件 */
    private final AdminRbacFacade adminRbacFacade;
    /** 管理请求上下文 */
    private final AdminRequestContext adminRequestContext;

    public AdminRbacController(AdminRbacFacade adminRbacFacade,
                               AdminRequestContext adminRequestContext) {
        this.adminRbacFacade = adminRbacFacade;
        this.adminRequestContext = adminRequestContext;
    }

    /**
     * 查询后台信息列表。
     */
    @GetMapping("/admins")
    @RequireAdminPermission("rbac.view")
    public ApiResponse<List<AdminAccountSummaryDTO>> listAdmins() {
        return ApiResponse.success(adminRbacFacade.listAdmins());
    }

    /**
     * 查询模块信息列表。
     */
    @GetMapping("/modules")
    @RequireAdminPermission("rbac.view")
    public ApiResponse<List<AdminModuleDTO>> listModules() {
        return ApiResponse.success(adminRbacFacade.listModules());
    }

    /**
     * 查询菜单信息列表。
     */
    @GetMapping("/menus")
    @RequireAdminPermission("rbac.view")
    public ApiResponse<List<AdminMenuDTO>> listMenus() {
        return ApiResponse.success(adminRbacFacade.listMenus());
    }

    /**
     * 查询角色信息列表。
     */
    @GetMapping("/roles")
    @RequireAdminPermission("rbac.view")
    public ApiResponse<List<AdminRoleDTO>> listRoles() {
        return ApiResponse.success(adminRbacFacade.listRoles());
    }

    /**
     * 查询权限信息列表。
     */
    @GetMapping("/permissions")
    @RequireAdminPermission("rbac.view")
    public ApiResponse<List<AdminPermissionDTO>> listPermissions(
            @RequestParam(value = "moduleCode", required = false) String moduleCode) {
        return ApiResponse.success(adminRbacFacade.listPermissions(moduleCode));
    }

    /**
     * 处理MY信息。
     */
    @GetMapping("/me/authorizations")
    @RequireAdminPermission("rbac.view")
    public ApiResponse<AdminAuthorizationDTO> myAuthorization() {
        Long adminId = adminRequestContext.requiredAdminId();
        return ApiResponse.success(adminRbacFacade.getAdminAuthorization(adminId));
    }

    /**
     * 获取后台信息。
     */
    @GetMapping("/admins/{adminId}/authorizations")
    @RequireAdminPermission("rbac.view")
    public ApiResponse<AdminAuthorizationDTO> getAdminAuthorization(@PathVariable("adminId") Long adminId) {
        return ApiResponse.success(adminRbacFacade.getAdminAuthorization(adminId));
    }

    /**
     * 处理后台角色信息。
     */
    @PutMapping("/admins/{adminId}/roles")
    @RequireAdminPermission("rbac.admin_role.assign")
    public ApiResponse<Void> assignAdminRoles(@PathVariable("adminId") Long adminId,
                                              @Valid @RequestBody AssignAdminRolesRequest request) {
        adminRbacFacade.assignAdminRoles(new AssignAdminRolesCommand(
                adminId,
                request.roleCodes(),
                resolveOperator(request.operator())
        ));
        return ApiResponse.success(null);
    }

    /**
     * 处理角色权限信息。
     */
    @PutMapping("/roles/{roleCode}/permissions")
    @RequireAdminPermission("rbac.role_permission.assign")
    public ApiResponse<Void> assignRolePermissions(@PathVariable("roleCode") String roleCode,
                                                   @Valid @RequestBody AssignRolePermissionsRequest request) {
        adminRbacFacade.assignRolePermissions(new AssignRolePermissionsCommand(
                roleCode,
                request.permissionCodes(),
                resolveOperator(request.operator())
        ));
        return ApiResponse.success(null);
    }

    /**
     * 处理角色菜单信息。
     */
    @PutMapping("/roles/{roleCode}/menus")
    @RequireAdminPermission("rbac.role_menu.assign")
    public ApiResponse<Void> assignRoleMenus(@PathVariable("roleCode") String roleCode,
                                             @Valid @RequestBody AssignRoleMenusRequest request) {
        adminRbacFacade.assignRoleMenus(new AssignRoleMenusCommand(
                roleCode,
                request.menuCodes(),
                resolveOperator(request.operator())
        ));
        return ApiResponse.success(null);
    }

    private String resolveOperator(String requestOperator) {
        if (requestOperator != null && !requestOperator.isBlank()) {
            return requestOperator.trim();
        }
        String currentUsername = adminRequestContext.currentAdminUsername();
        if (currentUsername != null && !currentUsername.isBlank()) {
            return currentUsername;
        }
        return "system";
    }
}
