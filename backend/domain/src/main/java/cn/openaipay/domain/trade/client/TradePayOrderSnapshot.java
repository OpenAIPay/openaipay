package cn.openaipay.domain.trade.client;

import java.time.LocalDateTime;
import java.util.List;
import org.joda.money.Money;

/**
 * TradePayOrderSnapshot 记录模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record TradePayOrderSnapshot(
        /** 支付单号 */
        String payOrderNo,
        /** 交易单号 */
        String tradeOrderNo,
        /** 订单单号 */
        String bizOrderNo,
        /** 来源业务类型 */
        String sourceBizType,
        /** 来源业务单号 */
        String sourceBizNo,
        /** 尝试单号 */
        Integer attemptNo,
        /** 业务场景编码 */
        String businessSceneCode,
        /** 付款方用户ID */
        Long payerUserId,
        /** 收款方用户ID */
        Long payeeUserId,
        /** 原始金额 */
        Money originalAmount,
        /** 业务金额 */
        Money discountAmount,
        /** 应付金额 */
        Money payableAmount,
        /** 业务金额 */
        Money actualPaidAmount,
        /** 优惠券单号 */
        String couponNo,
        /** TXID */
        String globalTxId,
        /** 状态编码 */
        String status,
        /** 状态版本号 */
        Integer statusVersion,
        /** 结果编码 */
        String resultCode,
        /** 结果说明 */
        String resultMessage,
        /** 失败原因 */
        String failureReason,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt,
        /** 参与方列表 */
        List<TradePayParticipantSnapshot> participants,
        /** 资金明细列表 */
        List<TradePayFundDetailSnapshot> fundDetails
) {
}
