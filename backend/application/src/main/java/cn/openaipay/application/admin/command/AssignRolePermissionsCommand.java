package cn.openaipay.application.admin.command;

import java.util.List;
/**
 * 分配角色Permissions命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AssignRolePermissionsCommand(
        /** 角色编码 */
        String roleCode,
        /** 权限信息 */
        List<String> permissionCodes,
        /** 操作人 */
        String operator
) {
}
