package cn.openaipay.domain.admin.model;

/**
 * 后台管理权限模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class AdminPermission {

    /** 权限编码 */
    private final String permissionCode;
    /** 权限名称 */
    private final String permissionName;
    /** 模块编码 */
    private final String moduleCode;
    /** 资源类型 */
    private final String resourceType;
    /** HTTP方式 */
    private final String httpMethod;
    /** 路径模式 */
    private final String pathPattern;
    /** 权限描述 */
    private final String permissionDesc;

    public AdminPermission(String permissionCode,
                           String permissionName,
                           String moduleCode,
                           String resourceType,
                           String httpMethod,
                           String pathPattern,
                           String permissionDesc) {
        this.permissionCode = permissionCode;
        this.permissionName = permissionName;
        this.moduleCode = moduleCode;
        this.resourceType = resourceType;
        this.httpMethod = httpMethod;
        this.pathPattern = pathPattern;
        this.permissionDesc = permissionDesc;
    }

    /**
     * 获取权限编码。
     */
    public String getPermissionCode() {
        return permissionCode;
    }

    /**
     * 获取权限信息。
     */
    public String getPermissionName() {
        return permissionName;
    }

    /**
     * 获取模块编码。
     */
    public String getModuleCode() {
        return moduleCode;
    }

    /**
     * 获取业务数据。
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * 获取业务数据。
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * 获取业务数据。
     */
    public String getPathPattern() {
        return pathPattern;
    }

    /**
     * 获取权限信息。
     */
    public String getPermissionDesc() {
        return permissionDesc;
    }
}
