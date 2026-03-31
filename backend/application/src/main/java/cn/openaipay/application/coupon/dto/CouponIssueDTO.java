package cn.openaipay.application.coupon.dto;

import org.joda.money.Money;
import java.time.LocalDateTime;
/**
 * 优惠券发放数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record CouponIssueDTO(
        /** 业务ID */
        Long issueId,
        /** 优惠券单号 */
        String couponNo,
        /** 模板ID */
        Long templateId,
        /** 用户ID */
        Long userId,
        /** 优惠券金额 */
        Money couponAmount,
        /** 状态编码 */
        String status,
        /** 渠道信息 */
        String claimChannel,
        /** 业务单号 */
        String businessNo,
        /** 订单号 */
        String orderNo,
        /** 全局业务单号 */
        String bizOrderNo,
        /** 交易单号 */
        String tradeOrderNo,
        /** 支付单号 */
        String payOrderNo,
        /** 领取时间 */
        LocalDateTime claimedAt,
        /** 业务时间 */
        LocalDateTime expireAt,
        /** 业务时间 */
        LocalDateTime usedAt,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
