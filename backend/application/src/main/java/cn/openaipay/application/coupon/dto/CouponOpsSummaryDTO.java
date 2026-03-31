package cn.openaipay.application.coupon.dto;
/**
 * 优惠券运营汇总数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record CouponOpsSummaryDTO(
        /** 模板次数 */
        long activeTemplateCount,
        /** 模板次数 */
        long pausedTemplateCount,
        /** 模板次数 */
        long draftTemplateCount,
        /** 模板次数 */
        long expiredTemplateCount,
        /** 总信息 */
        long totalIssuedCoupons
) {
}
