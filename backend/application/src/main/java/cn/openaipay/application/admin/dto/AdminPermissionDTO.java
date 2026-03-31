package cn.openaipay.application.admin.dto;
/**
 * 后台管理权限数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AdminPermissionDTO(
        /** 权限编码 */
        String permissionCode,
        /** 权限名称 */
        String permissionName,
        /** 业务编码 */
        String moduleCode,
        /** 业务类型 */
        String resourceType,
        /** HTTP方法 */
        String httpMethod,
        /** 路径匹配规则 */
        String pathPattern,
        /** 权限信息 */
        String permissionDesc
) {
}
