package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
/**
 * 发放优惠券请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record IssueCouponRequest(
        /** 模板ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long templateId,
        /** 用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long userId,
        /** 渠道信息 */
        @NotBlank(message = "不能为空") String claimChannel,
        /** 业务单号 */
        String businessNo,
        /** 操作人 */
        String operator
) {
}
