package cn.openaipay.domain.admin.repository;

import cn.openaipay.domain.admin.model.AdminAccount;
import cn.openaipay.domain.admin.model.AdminModule;
import cn.openaipay.domain.admin.model.AdminMenu;
import cn.openaipay.domain.admin.model.AdminPermission;
import cn.openaipay.domain.admin.model.AdminRole;

import java.util.List;
import java.util.Optional;

/**
 * 后台管理仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface AdminRepository {

    /**
     * 按用户名查询后台账号。
     */
    Optional<AdminAccount> findByUsername(String username);

    /**
     * 按管理员ID查询后台账号。
     */
    Optional<AdminAccount> findByAdminId(Long adminId);

    /**
     * 查询全部后台账号。
     */
    List<AdminAccount> findAllAccounts();

    /**
     * 保存后台账号信息。
     */
    AdminAccount saveAccount(AdminAccount account);

    /**
     * 查询所有可见菜单。
     */
    List<AdminMenu> findAllVisibleMenus();

    /**
     * 按管理员ID查询可见菜单。
     */
    List<AdminMenu> findVisibleMenusByAdminId(Long adminId);

    /**
     * 按管理员ID查询角色编码列表。
     */
    List<String> findRoleCodesByAdminId(Long adminId);

    /**
     * 按管理员ID查询权限编码列表。
     */
    List<String> findPermissionCodesByAdminId(Long adminId);

    /**
     * 查询全部RBAC模块。
     */
    List<AdminModule> findModules();

    /**
     * 查询全部角色。
     */
    List<AdminRole> findRoles();

    /**
     * 按模块编码查询权限列表。
     */
    List<AdminPermission> findPermissions(String moduleCode);

    /**
     * 按角色编码查询菜单编码列表。
     */
    List<String> findMenuCodesByRoleCode(String roleCode);

    /**
     * 按角色编码查询权限编码列表。
     */
    List<String> findPermissionCodesByRoleCode(String roleCode);

    /**
     * 覆盖管理员与角色关系。
     */
    void replaceAdminRoles(Long adminId, List<String> roleCodes, String operator);

    /**
     * 覆盖角色与权限关系。
     */
    void replaceRolePermissions(String roleCode, List<String> permissionCodes, String operator);

    /**
     * 覆盖角色与菜单关系。
     */
    void replaceRoleMenus(String roleCode, List<String> menuCodes, String operator);
}
