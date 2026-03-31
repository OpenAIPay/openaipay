package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.NotBlank;
/**
 * 赎回优惠券请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record RedeemCouponRequest(
        /** 优惠券单号 */
        @NotBlank(message = "不能为空") String couponNo,
        /** 订单号 */
        @NotBlank(message = "不能为空") String orderNo
) {
}
