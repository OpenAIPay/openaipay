package cn.openaipay.application.coupon.command;
/**
 * 赎回优惠券命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record RedeemCouponCommand(
        /** 优惠券单号 */
        String couponNo,
        /** 订单号 */
        String orderNo
) {
}
