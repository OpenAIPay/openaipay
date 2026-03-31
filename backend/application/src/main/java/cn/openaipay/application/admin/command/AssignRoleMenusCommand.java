package cn.openaipay.application.admin.command;

import java.util.List;
/**
 * 分配角色Menus命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AssignRoleMenusCommand(
        /** 角色编码 */
        String roleCode,
        /** 菜单信息 */
        List<String> menuCodes,
        /** 操作人 */
        String operator
) {
}
