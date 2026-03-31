package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新会计科目启停状态请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record UpdateAccountingSubjectStatusRequest(
        /** 启用标记 */
        @NotNull(message = "不能为空") Boolean enabled
) {
}
