package cn.openaipay.domain.trade.model;

import java.time.LocalDateTime;

/**
 * 交易流程步骤模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class TradeFlowStep {
    /** 主键ID。 */
    private final Long id;
    /** 统一交易主单号。 */
    private final String tradeOrderNo;
    /** 流程步骤编码。 */
    private final TradeFlowStepCode stepCode;
    /** 流程步骤状态。 */
    private TradeFlowStepStatus stepStatus;
    /** 步骤请求报文。 */
    private final String requestPayload;
    /** 步骤响应报文。 */
    private String responsePayload;
    /** 步骤错误信息。 */
    private String errorMessage;
    /** 步骤开始时间。 */
    private final LocalDateTime startedAt;
    /** 步骤结束时间。 */
    private LocalDateTime finishedAt;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public TradeFlowStep(Long id,
                         String tradeOrderNo,
                         TradeFlowStepCode stepCode,
                         TradeFlowStepStatus stepStatus,
                         String requestPayload,
                         String responsePayload,
                         String errorMessage,
                         LocalDateTime startedAt,
                         LocalDateTime finishedAt,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.id = id;
        this.tradeOrderNo = normalizeRequired(tradeOrderNo, "tradeOrderNo");
        this.stepCode = stepCode == null ? TradeFlowStepCode.PRICING_QUOTE : stepCode;
        this.stepStatus = stepStatus == null ? TradeFlowStepStatus.RUNNING : stepStatus;
        this.requestPayload = normalizeOptional(requestPayload);
        this.responsePayload = normalizeOptional(responsePayload);
        this.errorMessage = normalizeOptional(errorMessage);
        this.startedAt = startedAt == null ? LocalDateTime.now() : startedAt;
        this.finishedAt = finishedAt;
        this.createdAt = createdAt == null ? this.startedAt : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 启动业务数据。
     */
    public static TradeFlowStep start(String tradeOrderNo,
                                      TradeFlowStepCode stepCode,
                                      String requestPayload,
                                      LocalDateTime now) {
        return new TradeFlowStep(
                null,
                tradeOrderNo,
                stepCode,
                TradeFlowStepStatus.RUNNING,
                requestPayload,
                null,
                null,
                now,
                null,
                now,
                now
        );
    }

    /**
     * 标记业务数据。
     */
    public void markSuccess(String responsePayload, LocalDateTime now) {
        this.stepStatus = TradeFlowStepStatus.SUCCESS;
        this.responsePayload = normalizeOptional(responsePayload);
        this.errorMessage = null;
        this.finishedAt = now == null ? LocalDateTime.now() : now;
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markFailed(String errorMessage, LocalDateTime now) {
        this.stepStatus = TradeFlowStepStatus.FAILED;
        this.errorMessage = normalizeOptional(errorMessage);
        this.finishedAt = now == null ? LocalDateTime.now() : now;
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markSkipped(String message, LocalDateTime now) {
        this.stepStatus = TradeFlowStepStatus.SKIPPED;
        this.responsePayload = normalizeOptional(message);
        this.errorMessage = null;
        this.finishedAt = now == null ? LocalDateTime.now() : now;
        touch(now);
    }

    private void touch(LocalDateTime now) {
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    private String normalizeRequired(String raw, String field) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取交易订单NO信息。
     */
    public String getTradeOrderNo() {
        return tradeOrderNo;
    }

    /**
     * 获取编码。
     */
    public TradeFlowStepCode getStepCode() {
        return stepCode;
    }

    /**
     * 获取状态。
     */
    public TradeFlowStepStatus getStepStatus() {
        return stepStatus;
    }

    /**
     * 获取请求。
     */
    public String getRequestPayload() {
        return requestPayload;
    }

    /**
     * 获取响应信息。
     */
    public String getResponsePayload() {
        return responsePayload;
    }

    /**
     * 获取错误消息信息。
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
