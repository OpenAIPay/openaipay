package cn.openaipay.domain.walletaccount.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 钱包冻结明细模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public class WalletFreezeRecord {

    private final String xid;
    private final String branchId;
    private final Long userId;
    private final WalletFreezeType freezeType;
    private final TccOperationType operationType;
    private final Money amount;
    private WalletFreezeStatus freezeStatus;
    private final String businessNo;
    private String freezeReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WalletFreezeRecord(String xid,
                              String branchId,
                              Long userId,
                              WalletFreezeType freezeType,
                              TccOperationType operationType,
                              Money amount,
                              WalletFreezeStatus freezeStatus,
                              String businessNo,
                              String freezeReason,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
        this.xid = xid;
        this.branchId = branchId;
        this.userId = userId;
        this.freezeType = freezeType == null ? WalletFreezeType.DEFAULT : freezeType;
        this.operationType = operationType;
        this.amount = normalizeAmount(amount);
        this.freezeStatus = freezeStatus;
        this.businessNo = businessNo;
        this.freezeReason = freezeReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 处理NEW信息。
     */
    public static WalletFreezeRecord newFrozen(String xid,
                                               String branchId,
                                               Long userId,
                                               WalletFreezeType freezeType,
                                               TccOperationType operationType,
                                               Money amount,
                                               String businessNo,
                                               LocalDateTime now) {
        return new WalletFreezeRecord(
                xid,
                branchId,
                userId,
                freezeType,
                operationType,
                amount,
                WalletFreezeStatus.FROZEN,
                businessNo,
                null,
                now,
                now
        );
    }

    /**
     * 获取XID。
     */
    public String getXid() {
        return xid;
    }

    /**
     * 获取分支ID。
     */
    public String getBranchId() {
        return branchId;
    }

    /**
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取业务数据。
     */
    public WalletFreezeType getFreezeType() {
        return freezeType;
    }

    /**
     * 获取业务数据。
     */
    public TccOperationType getOperationType() {
        return operationType;
    }

    /**
     * 获取金额。
     */
    public Money getAmount() {
        return amount;
    }

    /**
     * 获取状态。
     */
    public WalletFreezeStatus getFreezeStatus() {
        return freezeStatus;
    }

    /**
     * 获取NO信息。
     */
    public String getBusinessNo() {
        return businessNo;
    }

    /**
     * 获取业务数据。
     */
    public String getFreezeReason() {
        return freezeReason;
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

    /**
     * 标记业务数据。
     */
    public void markDeducted(LocalDateTime now) {
        markDeducted("tcc confirm", now);
    }

    /**
     * 标记业务数据。
     */
    public void markDeducted(String reason, LocalDateTime now) {
        this.freezeStatus = WalletFreezeStatus.DEDUCTED;
        this.freezeReason = reason;
        this.updatedAt = now;
    }

    /**
     * 标记业务数据。
     */
    public void markReleased(String reason, LocalDateTime now) {
        this.freezeStatus = WalletFreezeStatus.RELEASED;
        this.freezeReason = reason;
        this.updatedAt = now;
    }

    private Money normalizeAmount(Money source) {
        if (source == null) {
            CurrencyUnit unit = CurrencyUnit.of("CNY");
            return Money.zero(unit).rounded(unit.getDecimalPlaces(), RoundingMode.HALF_UP);
        }
        return source.rounded(source.getCurrencyUnit().getDecimalPlaces(), RoundingMode.HALF_UP);
    }
}
