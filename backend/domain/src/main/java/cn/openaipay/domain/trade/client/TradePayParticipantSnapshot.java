package cn.openaipay.domain.trade.client;

import java.time.LocalDateTime;

/**
 * TradePayParticipantSnapshot 记录模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record TradePayParticipantSnapshot(
        /** 支付单号 */
        String payOrderNo,
        /** 业务类型 */
        String participantType,
        /** 分支ID */
        String branchId,
        /** 业务ID */
        String participantResourceId,
        /** 请求载荷 */
        String requestPayload,
        /** 状态编码 */
        String status,
        /** 响应说明 */
        String responseMessage,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
