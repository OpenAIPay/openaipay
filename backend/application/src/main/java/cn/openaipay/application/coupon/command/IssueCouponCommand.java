package cn.openaipay.application.coupon.command;
/**
 * 发放优惠券命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record IssueCouponCommand(
        /** 模板ID */
        Long templateId,
        /** 用户ID */
        Long userId,
        /** 渠道信息 */
        String claimChannel,
        /** 业务单号 */
        String businessNo,
        /** 操作人 */
        String operator
) {
}
