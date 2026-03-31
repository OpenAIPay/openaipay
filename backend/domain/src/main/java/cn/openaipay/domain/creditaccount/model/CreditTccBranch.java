package cn.openaipay.domain.creditaccount.model;

import org.joda.money.Money;
import org.joda.money.CurrencyUnit;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 信用TCC分支模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class CreditTccBranch {

    /** 分布式事务ID */
    private final String xid;
    /** 分支标识 */
    private final String branchId;
    /** 账户编号 */
    private final String accountNo;
    /** 操作类型 */
    private final CreditTccOperationType operationType;
    /** 资产类型 */
    private final CreditAssetCategory assetCategory;
    /** 金额 */
    private final Money amount;
    /** 分支状态 */
    private CreditTccBranchStatus branchStatus;
    /** 业务编号 */
    private final String businessNo;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public CreditTccBranch(String xid,
                           String branchId,
                           String accountNo,
                           CreditTccOperationType operationType,
                           CreditAssetCategory assetCategory,
                           Money amount,
                           CreditTccBranchStatus branchStatus,
                           String businessNo,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
        this.xid = xid;
        this.branchId = branchId;
        this.accountNo = accountNo;
        this.operationType = operationType;
        this.assetCategory = assetCategory;
        this.amount = normalizeAmount(amount);
        this.branchStatus = branchStatus;
        this.businessNo = businessNo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 处理NEWTRY信息。
     */
    public static CreditTccBranch newTry(String xid,
                                         String branchId,
                                         String accountNo,
                                         CreditTccOperationType operationType,
                                         CreditAssetCategory assetCategory,
                                         Money amount,
                                         String businessNo,
                                         LocalDateTime now) {
        return new CreditTccBranch(
                xid,
                branchId,
                accountNo,
                operationType,
                assetCategory,
                amount,
                CreditTccBranchStatus.TRIED,
                businessNo,
                now,
                now
        );
    }

    /**
     * 处理NEW信息。
     */
    public static CreditTccBranch newCancelFence(String xid,
                                                 String branchId,
                                                 String accountNo,
                                                 CreditTccOperationType operationType,
                                                 CreditAssetCategory assetCategory,
                                                 Money amount,
                                                 String businessNo,
                                                 LocalDateTime now) {
        return new CreditTccBranch(
                xid,
                branchId,
                accountNo,
                operationType,
                assetCategory,
                amount,
                CreditTccBranchStatus.CANCELED,
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
     * 获取账户NO信息。
     */
    public String getAccountNo() {
        return accountNo;
    }

    /**
     * 获取业务数据。
     */
    public CreditTccOperationType getOperationType() {
        return operationType;
    }

    /**
     * 获取资源信息。
     */
    public CreditAssetCategory getAssetCategory() {
        return assetCategory;
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
    public CreditTccBranchStatus getBranchStatus() {
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
        if (branchStatus == CreditTccBranchStatus.CANCELED) {
            throw new IllegalStateException("branch has already been canceled");
        }
        this.branchStatus = CreditTccBranchStatus.CONFIRMED;
        this.updatedAt = now;
    }

    /**
     * 标记业务数据。
     */
    public void markCanceled(LocalDateTime now) {
        if (branchStatus == CreditTccBranchStatus.CONFIRMED) {
            throw new IllegalStateException("branch has already been confirmed");
        }
        this.branchStatus = CreditTccBranchStatus.CANCELED;
        this.updatedAt = now;
    }

    private Money normalizeAmount(Money source) {
        if (source == null) {
            return Money.zero(CurrencyUnit.of("CNY")).rounded(2, RoundingMode.HALF_UP);
        }
        return source.rounded(2, RoundingMode.HALF_UP);
    }
}
