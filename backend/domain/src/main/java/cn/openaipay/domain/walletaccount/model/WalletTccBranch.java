package cn.openaipay.domain.walletaccount.model;

import org.joda.money.Money;
import org.joda.money.CurrencyUnit;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 钱包TCC分支模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class WalletTccBranch {

    /** 分布式事务ID */
    private final String xid;
    /** 分支标识 */
    private final String branchId;
    /** 用户ID */
    private final Long userId;
    /** 操作类型 */
    private final TccOperationType operationType;
    /** 冻结类型 */
    private final WalletFreezeType freezeType;
    /** 金额 */
    private final Money amount;
    /** 分支状态 */
    private WalletTccBranchStatus branchStatus;
    /** 业务编号 */
    private final String businessNo;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public WalletTccBranch(String xid,
                           String branchId,
                           Long userId,
                           TccOperationType operationType,
                           WalletFreezeType freezeType,
                           Money amount,
                           WalletTccBranchStatus branchStatus,
                           String businessNo,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
        this.xid = xid;
        this.branchId = branchId;
        this.userId = userId;
        this.operationType = operationType;
        this.freezeType = freezeType == null ? WalletFreezeType.DEFAULT : freezeType;
        this.amount = normalizeAmount(amount);
        this.branchStatus = branchStatus;
        this.businessNo = businessNo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 处理NEWTRY信息。
     */
    public static WalletTccBranch newTry(String xid,
                                         String branchId,
                                         Long userId,
                                         TccOperationType operationType,
                                         WalletFreezeType freezeType,
                                         Money amount,
                                         String businessNo,
                                         LocalDateTime now) {
        return new WalletTccBranch(
                xid,
                branchId,
                userId,
                operationType,
                freezeType,
                amount,
                WalletTccBranchStatus.TRIED,
                businessNo,
                now,
                now
        );
    }

    /**
     * 处理NEW信息。
     */
    public static WalletTccBranch newCancelFence(String xid,
                                                 String branchId,
                                                 Long userId,
                                                 TccOperationType operationType,
                                                 WalletFreezeType freezeType,
                                                 Money amount,
                                                 String businessNo,
                                                 LocalDateTime now) {
        return new WalletTccBranch(
                xid,
                branchId,
                userId,
                operationType,
                freezeType,
                amount,
                WalletTccBranchStatus.CANCELED,
                businessNo,
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
    public TccOperationType getOperationType() {
        return operationType;
    }

    /**
     * 获取业务数据。
     */
    public WalletFreezeType getFreezeType() {
        return freezeType;
    }

    /**
     * 获取金额。
     */
    public Money getAmount() {
        return amount;
    }

    /**
     * 获取分支状态。
     */
    public WalletTccBranchStatus getBranchStatus() {
        return branchStatus;
    }

    /**
     * 获取NO信息。
     */
    public String getBusinessNo() {
        return businessNo;
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
    public void markConfirmed(LocalDateTime now) {
        if (branchStatus == WalletTccBranchStatus.CANCELED) {
            throw new IllegalStateException("branch has already been canceled");
        }
        this.branchStatus = WalletTccBranchStatus.CONFIRMED;
        this.updatedAt = now;
    }

    /**
     * 标记业务数据。
     */
    public void markCanceled(LocalDateTime now) {
        if (branchStatus == WalletTccBranchStatus.CONFIRMED) {
            throw new IllegalStateException("branch has already been confirmed");
        }
        this.branchStatus = WalletTccBranchStatus.CANCELED;
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
