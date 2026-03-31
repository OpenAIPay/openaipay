package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
/**
 * 分配角色Permissions请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AssignRolePermissionsRequest(
        /** 权限信息 */
        @NotNull(message = "不能为空") List<String> permissionCodes,
        /** 操作人 */
        String operator
) {
}
