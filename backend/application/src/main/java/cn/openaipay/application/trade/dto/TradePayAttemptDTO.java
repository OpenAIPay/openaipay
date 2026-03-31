package cn.openaipay.application.trade.dto;

import org.joda.money.Money;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易视角下的支付尝试快照。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record TradePayAttemptDTO(
        /** 支付尝试序号 */
        Integer attemptNo,
        /** 支付单号 */
        String payOrderNo,
        /** 业务支付单号 */
        String bizOrderNo,
        /** 状态编码 */
        String status,
        /** 支付状态版本号 */
        Integer statusVersion,
        /** 支付结果码 */
        String resultCode,
        /** 支付结果描述 */
        String resultMessage,
        /** 失败原因 */
        String failureReason,
        /** 原始金额 */
        Money originalAmount,
        /** 优惠金额 */
        Money discountAmount,
        /** 应付金额 */
        Money payableAmount,
        /** 实付金额 */
        Money actualPaidAmount,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt,
        /** 参与方列表 */
        List<TradePayParticipantDTO> participants
) {
}
