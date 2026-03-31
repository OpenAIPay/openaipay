package cn.openaipay.domain.admin.model;

/**
 * 后台管理角色模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class AdminRole {

    /** 角色编码 */
    private final String roleCode;
    /** 角色名称 */
    private final String roleName;
    /** 角色范围 */
    private final String roleScope;
    /** 角色状态 */
    private final String roleStatus;
    /** 是否内置角色 */
    private final boolean builtin;
    /** 角色描述 */
    private final String roleDesc;

    public AdminRole(String roleCode,
                     String roleName,
                     String roleScope,
                     String roleStatus,
                     boolean builtin,
                     String roleDesc) {
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.roleScope = roleScope;
        this.roleStatus = roleStatus;
        this.builtin = builtin;
        this.roleDesc = roleDesc;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(roleStatus);
    }

    /**
     * 获取角色编码。
     */
    public String getRoleCode() {
        return roleCode;
    }

    /**
     * 获取角色信息。
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * 获取角色信息。
     */
    public String getRoleScope() {
        return roleScope;
    }

    /**
     * 获取角色状态。
     */
    public String getRoleStatus() {
        return roleStatus;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isBuiltin() {
        return builtin;
    }

    /**
     * 获取角色信息。
     */
    public String getRoleDesc() {
        return roleDesc;
    }
}
