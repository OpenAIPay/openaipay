package cn.openaipay.application.admin.command;

import java.util.List;
/**
 * 分配后台管理Roles命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AssignAdminRolesCommand(
        /** 后台ID */
        Long adminId,
        /** 角色信息 */
        List<String> roleCodes,
        /** 操作人 */
        String operator
) {
}
