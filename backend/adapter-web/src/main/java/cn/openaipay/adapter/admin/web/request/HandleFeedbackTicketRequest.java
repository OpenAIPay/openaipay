package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 后台处理反馈单请求参数。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
public record HandleFeedbackTicketRequest(
        /** 状态编码 */
        @NotBlank(message = "不能为空") @Size(max = 32, message = "长度不能超过32") String status,
        /** 处理备注 */
        @Size(max = 1000, message = "长度不能超过1000") String handleNote
) {
}
