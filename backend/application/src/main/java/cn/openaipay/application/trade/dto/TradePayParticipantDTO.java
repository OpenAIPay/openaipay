package cn.openaipay.application.trade.dto;

import java.time.LocalDateTime;

/**
 * 交易视角下的支付参与方分支明细。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record TradePayParticipantDTO(
        /** 参与方类型 */
        String participantType,
        /** 分支号 */
        String branchId,
        /** 参与方资源标识 */
        String participantResourceId,
        /** 状态编码 */
        String status,
        /** 分支请求快照 */
        String requestPayload,
        /** 分支响应结果 */
        String responseMessage,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
