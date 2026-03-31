package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
/**
 * 分配角色Menus请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AssignRoleMenusRequest(
        /** 菜单信息 */
        @NotNull(message = "不能为空") List<String> menuCodes,
        /** 操作人 */
        String operator
) {
}
