package cn.openaipay.domain.trade.client;

import java.time.LocalDateTime;

/**
 * TradePaySubmitResult 记录模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record TradePaySubmitResult(
        /** 支付单号 */
        String payOrderNo,
        /** 订单单号 */
        String bizOrderNo,
        /** 来源业务类型 */
        String sourceBizType,
        /** 来源业务单号 */
        String sourceBizNo,
        /** 尝试单号 */
        Integer attemptNo,
        /** 状态编码 */
        String status,
        /** 状态版本号 */
        Integer statusVersion,
        /** 结果编码 */
        String resultCode,
        /** 结果说明 */
        String resultMessage,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
