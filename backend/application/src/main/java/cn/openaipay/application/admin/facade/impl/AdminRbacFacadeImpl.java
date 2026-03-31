package cn.openaipay.application.admin.facade.impl;

import cn.openaipay.application.admin.command.AssignAdminRolesCommand;
import cn.openaipay.application.admin.command.AssignRoleMenusCommand;
import cn.openaipay.application.admin.command.AssignRolePermissionsCommand;
import cn.openaipay.application.admin.dto.AdminAccountSummaryDTO;
import cn.openaipay.application.admin.dto.AdminAuthorizationDTO;
import cn.openaipay.application.admin.dto.AdminMenuDTO;
import cn.openaipay.application.admin.dto.AdminModuleDTO;
import cn.openaipay.application.admin.dto.AdminPermissionDTO;
import cn.openaipay.application.admin.dto.AdminRoleDTO;
import cn.openaipay.application.admin.facade.AdminRbacFacade;
import cn.openaipay.application.admin.service.AdminRbacService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 后台 RBAC 门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Service
public class AdminRbacFacadeImpl implements AdminRbacFacade {

    /** 后台信息 */
    private final AdminRbacService adminRbacService;

    public AdminRbacFacadeImpl(AdminRbacService adminRbacService) {
        this.adminRbacService = adminRbacService;
    }

    /**
     * 查询管理员列表。
     */
    @Override
    public List<AdminAccountSummaryDTO> listAdmins() {
        return adminRbacService.listAdmins();
    }

    /**
     * 查询菜单列表。
     */
    @Override
    public List<AdminMenuDTO> listMenus() {
        return adminRbacService.listMenus();
    }

    /**
     * 查询模块信息列表。
     */
    @Override
    public List<AdminModuleDTO> listModules() {
        return adminRbacService.listModules();
    }

    /**
     * 查询角色信息列表。
     */
    @Override
    public List<AdminRoleDTO> listRoles() {
        return adminRbacService.listRoles();
    }

    /**
     * 查询权限信息列表。
     */
    @Override
    public List<AdminPermissionDTO> listPermissions(String moduleCode) {
        return adminRbacService.listPermissions(moduleCode);
    }

    /**
     * 获取后台信息。
     */
    @Override
    public AdminAuthorizationDTO getAdminAuthorization(Long adminId) {
        return adminRbacService.getAdminAuthorization(adminId);
    }

    /**
     * 处理后台角色信息。
     */
    @Override
    public void assignAdminRoles(AssignAdminRolesCommand command) {
        adminRbacService.assignAdminRoles(command);
    }

    /**
     * 处理角色权限信息。
     */
    @Override
    public void assignRolePermissions(AssignRolePermissionsCommand command) {
        adminRbacService.assignRolePermissions(command);
    }

    /**
     * 处理角色菜单信息。
     */
    @Override
    public void assignRoleMenus(AssignRoleMenusCommand command) {
        adminRbacService.assignRoleMenus(command);
    }

    /**
     * 判断是否存在权限信息。
     */
    @Override
    public boolean hasPermission(Long adminId, String permissionCode) {
        return adminRbacService.hasPermission(adminId, permissionCode);
    }
}
