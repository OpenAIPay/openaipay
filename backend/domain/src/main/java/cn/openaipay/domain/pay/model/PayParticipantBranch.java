package cn.openaipay.domain.pay.model;

import java.time.LocalDateTime;
/**
 * 支付参与方分支模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class PayParticipantBranch {

    /** 数据库主键ID */
    private final Long id;
    /** 支付单号 */
    private final String payOrderNo;
    /** 参与方类型 */
    private final PayParticipantType participantType;
    /** 分支标识 */
    private final String branchId;
    /** 参与方资源标识 */
    private final String participantResourceId;
    /** 请求载荷 */
    private String requestPayload;
    /** 业务状态值 */
    private PayParticipantStatus status;
    /** 响应消息 */
    private String responseMessage;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public PayParticipantBranch(Long id,
                                String payOrderNo,
                                PayParticipantType participantType,
                                String branchId,
                                String participantResourceId,
                                String requestPayload,
                                PayParticipantStatus status,
                                String responseMessage,
                                LocalDateTime createdAt,
                                LocalDateTime updatedAt) {
        this.id = id;
        this.payOrderNo = normalizeRequired(payOrderNo, "payOrderNo");
        this.participantType = participantType == null ? PayParticipantType.WALLET_ACCOUNT : participantType;
        this.branchId = normalizeRequired(branchId, "branchId");
        this.participantResourceId = normalizeRequired(participantResourceId, "participantResourceId");
        this.requestPayload = normalizeOptional(requestPayload);
        this.status = status == null ? PayParticipantStatus.INIT : status;
        this.responseMessage = normalizeOptional(responseMessage);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 创建业务数据。
     */
    public static PayParticipantBranch create(String payOrderNo,
                                              PayParticipantType participantType,
                                              String branchId,
                                              String participantResourceId,
                                              String requestPayload,
                                              LocalDateTime now) {
        return new PayParticipantBranch(
                null,
                payOrderNo,
                participantType,
                branchId,
                participantResourceId,
                requestPayload,
                PayParticipantStatus.INIT,
                null,
                now,
                now
        );
    }

    /**
     * 标记TRYOK信息。
     */
    public void markTryOk(String message, LocalDateTime now) {
        this.status = PayParticipantStatus.TRY_OK;
        this.responseMessage = normalizeOptional(message);
        touch(now);
    }

    /**
     * 更新请求。
     */
    public void updateRequestPayload(String requestPayload, LocalDateTime now) {
        this.requestPayload = normalizeOptional(requestPayload);
        touch(now);
    }

    /**
     * 标记TRY信息。
     */
    public void markTryFailed(String message, LocalDateTime now) {
        this.status = PayParticipantStatus.TRY_FAILED;
        this.responseMessage = normalizeOptional(message);
        touch(now);
    }

    /**
     * 标记OK信息。
     */
    public void markConfirmOk(String message, LocalDateTime now) {
        this.status = PayParticipantStatus.CONFIRM_OK;
        this.responseMessage = normalizeOptional(message);
        touch(now);
    }

    /**
     * 标记OK信息。
     */
    public void markCancelOk(String message, LocalDateTime now) {
        this.status = PayParticipantStatus.CANCEL_OK;
        this.responseMessage = normalizeOptional(message);
        touch(now);
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取支付订单NO信息。
     */
    public String getPayOrderNo() {
        return payOrderNo;
    }

    /**
     * 获取业务数据。
     */
    public PayParticipantType getParticipantType() {
        return participantType;
    }

    /**
     * 获取分支ID。
     */
    public String getBranchId() {
        return branchId;
    }

    /**
     * 获取ID。
     */
    public String getParticipantResourceId() {
        return participantResourceId;
    }

    /**
     * 获取请求。
     */
    public String getRequestPayload() {
        return requestPayload;
    }

    /**
     * 获取状态。
     */
    public PayParticipantStatus getStatus() {
        return status;
    }

    /**
     * 获取响应消息信息。
     */
    public String getResponseMessage() {
        return responseMessage;
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

    private void touch(LocalDateTime now) {
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    private String normalizeRequired(String raw, String fieldName) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
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
}
