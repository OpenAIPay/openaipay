package cn.openaipay.application.admin.service;

import cn.openaipay.application.admin.command.AssignAdminRolesCommand;
import cn.openaipay.application.admin.command.AssignRoleMenusCommand;
import cn.openaipay.application.admin.command.AssignRolePermissionsCommand;
import cn.openaipay.application.admin.dto.AdminAccountSummaryDTO;
import cn.openaipay.application.admin.dto.AdminAuthorizationDTO;
import cn.openaipay.application.admin.dto.AdminMenuDTO;
import cn.openaipay.application.admin.dto.AdminModuleDTO;
import cn.openaipay.application.admin.dto.AdminPermissionDTO;
import cn.openaipay.application.admin.dto.AdminRoleDTO;

import java.util.List;

/**
 * 后台管理RBAC应用服务接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface AdminRbacService {

    /**
     * 查询管理员列表。
     */
    List<AdminAccountSummaryDTO> listAdmins();

    /**
     * 查询菜单列表。
     */
    List<AdminMenuDTO> listMenus();

    /**
     * 查询模块信息列表。
     */
    List<AdminModuleDTO> listModules();

    /**
     * 查询角色信息列表。
     */
    List<AdminRoleDTO> listRoles();

    /**
     * 查询权限信息列表。
     */
    List<AdminPermissionDTO> listPermissions(String moduleCode);

    /**
     * 获取后台信息。
     */
    AdminAuthorizationDTO getAdminAuthorization(Long adminId);

    /**
     * 处理后台角色信息。
     */
    void assignAdminRoles(AssignAdminRolesCommand command);

    /**
     * 处理角色权限信息。
     */
    void assignRolePermissions(AssignRolePermissionsCommand command);

    /**
     * 处理角色菜单信息。
     */
    void assignRoleMenus(AssignRoleMenusCommand command);

    /**
     * 判断是否存在权限信息。
     */
    boolean hasPermission(Long adminId, String permissionCode);
}
