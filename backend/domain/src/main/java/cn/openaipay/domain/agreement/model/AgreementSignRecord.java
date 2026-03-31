package cn.openaipay.domain.agreement.model;

import java.time.LocalDateTime;

/**
 * 协议签约记录。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public class AgreementSignRecord {

    /** 签约单号。 */
    private final String signNo;
    /** 用户ID。 */
    private final Long userId;
    /** 业务类型。 */
    private final AgreementBizType bizType;
    /** 基金编码。 */
    private final String fundCode;
    /** 币种。 */
    private final String currencyCode;
    /** 幂等键。 */
    private final String idempotencyKey;
    /** 签约状态。 */
    private AgreementSignStatus signStatus;
    /** 签约时间。 */
    private LocalDateTime signedAt;
    /** 开户完成时间。 */
    private LocalDateTime openedAt;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public AgreementSignRecord(String signNo,
                               Long userId,
                               AgreementBizType bizType,
                               String fundCode,
                               String currencyCode,
                               String idempotencyKey,
                               AgreementSignStatus signStatus,
                               LocalDateTime signedAt,
                               LocalDateTime openedAt,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt) {
        this.signNo = signNo;
        this.userId = userId;
        this.bizType = bizType;
        this.fundCode = fundCode;
        this.currencyCode = currencyCode;
        this.idempotencyKey = idempotencyKey;
        this.signStatus = signStatus;
        this.signedAt = signedAt;
        this.openedAt = openedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 处理业务数据。
     */
    public static AgreementSignRecord pending(String signNo,
                                              Long userId,
                                              AgreementBizType bizType,
                                              String fundCode,
                                              String currencyCode,
                                              String idempotencyKey,
                                              LocalDateTime now) {
        return new AgreementSignRecord(
                signNo,
                userId,
                bizType,
                fundCode,
                currencyCode,
                idempotencyKey,
                AgreementSignStatus.PENDING,
                null,
                null,
                now,
                now
        );
    }

    /**
     * 标记业务数据。
     */
    public void markSucceeded(LocalDateTime now) {
        this.signStatus = AgreementSignStatus.SUCCEEDED;
        this.signedAt = now;
        this.openedAt = now;
        this.updatedAt = now;
    }

    /**
     * 获取NO信息。
     */
    public String getSignNo() {
        return signNo;
    }

    /**
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取业务类型信息。
     */
    public AgreementBizType getBizType() {
        return bizType;
    }

    /**
     * 获取基金编码。
     */
    public String getFundCode() {
        return fundCode;
    }

    /**
     * 获取编码。
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * 获取KEY信息。
     */
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    /**
     * 获取状态。
     */
    public AgreementSignStatus getSignStatus() {
        return signStatus;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getOpenedAt() {
        return openedAt;
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
