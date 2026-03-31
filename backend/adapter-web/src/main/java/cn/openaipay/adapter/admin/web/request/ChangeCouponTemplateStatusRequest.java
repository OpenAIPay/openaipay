package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.NotBlank;
/**
 * 变更优惠券模板状态请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record ChangeCouponTemplateStatusRequest(
        /** 状态编码 */
        @NotBlank(message = "不能为空") String status,
        /** 操作人 */
        String operator
) {
}
