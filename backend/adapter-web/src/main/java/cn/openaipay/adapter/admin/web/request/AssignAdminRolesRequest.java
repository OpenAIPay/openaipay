package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
/**
 * 分配后台管理Roles请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AssignAdminRolesRequest(
        /** 角色信息 */
        @NotNull(message = "不能为空") List<String> roleCodes,
        /** 操作人 */
        String operator
) {
}
