package cn.openaipay.application.trade.dto;

import java.time.LocalDateTime;

/**
 * 交易流程步骤数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record TradeFlowStepDTO(
        /** 业务编码 */
        String stepCode,
        /** 业务状态 */
        String stepStatus,
        /** 请求载荷 */
        String requestPayload,
        /** 响应载荷 */
        String responsePayload,
        /** 业务说明 */
        String errorMessage,
        /** 开始时间 */
        LocalDateTime startedAt,
        /** 业务时间 */
        LocalDateTime finishedAt,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
