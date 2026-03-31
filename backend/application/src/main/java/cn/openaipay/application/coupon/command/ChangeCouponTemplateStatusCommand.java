package cn.openaipay.application.coupon.command;
/**
 * 变更优惠券模板状态命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record ChangeCouponTemplateStatusCommand(
        /** 模板ID */
        Long templateId,
        /** 状态编码 */
        String status,
        /** 操作人 */
        String operator
) {
}
