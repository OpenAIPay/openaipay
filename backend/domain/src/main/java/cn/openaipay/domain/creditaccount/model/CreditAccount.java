package cn.openaipay.domain.creditaccount.model;

import org.joda.money.Money;
import org.joda.money.CurrencyUnit;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 信用账户模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class CreditAccount {

    /** 账户编号 */
    private final String accountNo;
    /** 用户ID */
    private final Long userId;
    /** 总上限 */
    private Money totalLimit;
    /** 本金余额 */
    private Money principalBalance;
    /** 未入账本金金额 */
    private Money principalUnreachAmount;
    /** 逾期本金余额 */
    private Money overduePrincipalBalance;
    /** 逾期未入账本金金额 */
    private Money overduePrincipalUnreachAmount;
    /** 利息余额 */
    private Money interestBalance;
    /** 罚息余额 */
    private Money fineBalance;
    /** 账户状态 */
    private CreditAccountStatus accountStatus;
    /** 支付状态 */
    private CreditAccountPayStatus payStatus;
    /** 每月还款日 */
    private int repayDayOfMonth;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public CreditAccount(String accountNo,
                         Long userId,
                         Money totalLimit,
                         Money principalBalance,
                         Money principalUnreachAmount,
                         Money overduePrincipalBalance,
                         Money overduePrincipalUnreachAmount,
                         Money interestBalance,
                         Money fineBalance,
                         CreditAccountStatus accountStatus,
                         CreditAccountPayStatus payStatus,
                         int repayDayOfMonth,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.accountNo = accountNo;
        this.userId = userId;
        this.totalLimit = normalizeAmount(totalLimit);
        this.principalBalance = normalizeAmount(principalBalance);
        this.principalUnreachAmount = normalizeAmount(principalUnreachAmount);
        this.overduePrincipalBalance = normalizeAmount(overduePrincipalBalance);
        this.overduePrincipalUnreachAmount = normalizeAmount(overduePrincipalUnreachAmount);
        this.interestBalance = normalizeAmount(interestBalance);
        this.fineBalance = normalizeAmount(fineBalance);
        this.accountStatus = accountStatus;
        this.payStatus = payStatus;
        this.repayDayOfMonth = normalizeRepayDayOfMonth(repayDayOfMonth);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        assertNonNegativeState();
    }

    /**
     * 开通业务数据。
     */
    public static CreditAccount open(String accountNo, Long userId, Money totalLimit, LocalDateTime now) {
        return open(accountNo, userId, totalLimit, 10, now);
    }

    /**
     * 开通业务数据。
     */
    public static CreditAccount open(String accountNo,
                                     Long userId,
                                     Money totalLimit,
                                     int repayDayOfMonth,
                                     LocalDateTime now) {
        Money zero = zeroMoney();
        return new CreditAccount(
                accountNo,
                userId,
                totalLimit,
                zero,
                zero,
                zero,
                zero,
                zero,
                zero,
                CreditAccountStatus.NORMAL,
                CreditAccountPayStatus.NORMAL,
                repayDayOfMonth,
                now,
                now
        );
    }

    /**
     * 获取账户NO信息。
     */
    public String getAccountNo() {
        return accountNo;
    }

    /**
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取限额信息。
     */
    public Money getTotalLimit() {
        return totalLimit;
    }

    /**
     * 获取业务数据。
     */
    public Money getPrincipalBalance() {
        return principalBalance;
    }

    /**
     * 获取金额。
     */
    public Money getPrincipalUnreachAmount() {
        return principalUnreachAmount;
    }

    /**
     * 获取业务数据。
     */
    public Money getOverduePrincipalBalance() {
        return overduePrincipalBalance;
    }

    /**
     * 获取金额。
     */
    public Money getOverduePrincipalUnreachAmount() {
        return overduePrincipalUnreachAmount;
    }

    /**
     * 获取业务数据。
     */
    public Money getInterestBalance() {
        return interestBalance;
    }

    /**
     * 获取业务数据。
     */
    public Money getFineBalance() {
        return fineBalance;
    }

    /**
     * 获取账户状态。
     */
    public CreditAccountStatus getAccountStatus() {
        return accountStatus;
    }

    /**
     * 获取支付状态。
     */
    public CreditAccountPayStatus getPayStatus() {
        return payStatus;
    }

    /**
     * 获取DAYOF信息。
     */
    public int getRepayDayOfMonth() {
        return repayDayOfMonth;
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
     * 处理限额信息。
     */
    public Money calculateAvailableLimit() {
        return normalizeAmount(
                totalLimit
                        .minus(principalBalance)
                        .minus(principalUnreachAmount)
                        .minus(overduePrincipalBalance)
                        .minus(overduePrincipalUnreachAmount)
        );
    }

    /**
     * 处理业务数据。
     */
    public void hold(CreditTccOperationType operationType,
                     CreditAssetCategory assetCategory,
                     Money amount,
                     LocalDateTime now) {
        Money normalizedAmount = requirePositiveAmount(amount);
        operationType.hold(this, assetCategory, normalizedAmount, now);
    }

    /**
     * 确认业务数据。
     */
    public void confirm(CreditTccOperationType operationType,
                        CreditAssetCategory assetCategory,
                        Money amount,
                        LocalDateTime now) {
        Money normalizedAmount = requirePositiveAmount(amount);
        operationType.confirm(this, assetCategory, normalizedAmount, now);
    }

    /**
     * 取消业务数据。
     */
    public void cancel(CreditTccOperationType operationType,
                       CreditAssetCategory assetCategory,
                       Money amount,
                       LocalDateTime now) {
        Money normalizedAmount = requirePositiveAmount(amount);
        operationType.cancel(this, assetCategory, normalizedAmount, now);
    }

    /**
     * 处理业务数据。
     */
    public void lend(Money amount, LocalDateTime now) {
        ensureStatus(CreditAccountStatus.NORMAL, "credit account status is not NORMAL");
        Money normalizedAmount = requirePositiveAmount(amount);
        if (calculateAvailableLimit().compareTo(normalizedAmount) < 0) {
            throw new IllegalArgumentException("insufficient available credit limit");
        }
        principalBalance = normalizeAmount(principalBalance.plus(normalizedAmount));
        touch(now);
    }

    /**
     * 处理业务数据。
     */
    public void repayPrincipal(Money amount, LocalDateTime now) {
        Money normalizedAmount = requirePositiveAmount(amount);
        if (principalBalance.compareTo(normalizedAmount) < 0) {
            throw new IllegalArgumentException("principal balance is not enough");
        }
        principalBalance = normalizeAmount(principalBalance.minus(normalizedAmount));
        touch(now);
    }

    /**
     * 处理转账TO信息。
     */
    public void transferPrincipalToOverdue(Money amount, LocalDateTime now) {
        Money normalizedAmount = requirePositiveAmount(amount);
        if (principalBalance.compareTo(normalizedAmount) < 0) {
            throw new IllegalArgumentException("principal balance is not enough for overdue transfer");
        }
        principalBalance = normalizeAmount(principalBalance.minus(normalizedAmount));
        overduePrincipalBalance = normalizeAmount(overduePrincipalBalance.plus(normalizedAmount));
        touch(now);
    }

    /**
     * 处理业务数据。
     */
    public void repayOverduePrincipal(Money amount, LocalDateTime now) {
        Money normalizedAmount = requirePositiveAmount(amount);
        if (overduePrincipalBalance.compareTo(normalizedAmount) < 0) {
            throw new IllegalArgumentException("overdue principal balance is not enough");
        }
        overduePrincipalBalance = normalizeAmount(overduePrincipalBalance.minus(normalizedAmount));
        touch(now);
    }

    /**
     * 处理业务数据。
     */
    public void accrueInterest(Money amount, LocalDateTime now) {
        Money normalizedAmount = requirePositiveAmount(amount);
        interestBalance = normalizeAmount(interestBalance.plus(normalizedAmount));
        touch(now);
    }

    /**
     * 处理业务数据。
     */
    public void repayInterest(Money amount, LocalDateTime now) {
        Money normalizedAmount = requirePositiveAmount(amount);
        if (interestBalance.compareTo(normalizedAmount) < 0) {
            throw new IllegalArgumentException("interest balance is not enough");
        }
        interestBalance = normalizeAmount(interestBalance.minus(normalizedAmount));
        touch(now);
    }

    /**
     * 处理业务数据。
     */
    public void accrueFine(Money amount, LocalDateTime now) {
        Money normalizedAmount = requirePositiveAmount(amount);
        fineBalance = normalizeAmount(fineBalance.plus(normalizedAmount));
        touch(now);
    }

    /**
     * 处理业务数据。
     */
    public void repayFine(Money amount, LocalDateTime now) {
        Money normalizedAmount = requirePositiveAmount(amount);
        if (fineBalance.compareTo(normalizedAmount) < 0) {
            throw new IllegalArgumentException("fine balance is not enough");
        }
        fineBalance = normalizeAmount(fineBalance.minus(normalizedAmount));
        touch(now);
    }

    /**
     * 处理状态。
     */
    public void changeStatus(CreditAccountStatus targetStatus, LocalDateTime now) {
        if (targetStatus == null) {
            throw new IllegalArgumentException("targetStatus must not be null");
        }
        if (accountStatus == CreditAccountStatus.CLOSE) {
            throw new IllegalStateException("closed account cannot change status");
        }
        if (targetStatus == CreditAccountStatus.CLOSE && !isClear()) {
            throw new IllegalStateException("credit account is not clear, close is not allowed");
        }
        accountStatus = targetStatus;
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markCompensated(LocalDateTime now) {
        payStatus = CreditAccountPayStatus.PAID;
        accountStatus = CreditAccountStatus.FREEZE_LEND;
        touch(now);
    }

    /**
     * 更新限额信息。
     */
    public void updateTotalLimit(Money newTotalLimit, LocalDateTime now) {
        Money normalizedLimit = requirePositiveAmount(newTotalLimit);
        if (normalizedLimit.compareTo(outstandingExposure()) < 0) {
            throw new IllegalArgumentException("new total limit is less than outstanding exposure");
        }
        totalLimit = normalizedLimit;
        touch(now);
    }

    void holdPrincipalForLend(Money amount, LocalDateTime now) {
        ensureStatus(CreditAccountStatus.NORMAL, "credit account status is not NORMAL");
        if (calculateAvailableLimit().compareTo(amount) < 0) {
            throw new IllegalArgumentException("insufficient available credit limit");
        }
        principalUnreachAmount = normalizeAmount(principalUnreachAmount.plus(amount));
        touch(now);
    }

    void confirmPrincipalForLend(Money amount, LocalDateTime now) {
        ensureUnreachEnough(principalUnreachAmount, amount, "principal unreach amount is not enough");
        principalUnreachAmount = normalizeAmount(principalUnreachAmount.minus(amount));
        principalBalance = normalizeAmount(principalBalance.plus(amount));
        touch(now);
    }

    void cancelPrincipalForLend(Money amount, LocalDateTime now) {
        ensureUnreachEnough(principalUnreachAmount, amount, "principal unreach amount is not enough");
        principalUnreachAmount = normalizeAmount(principalUnreachAmount.minus(amount));
        touch(now);
    }

    void holdPrincipalForRepay(Money amount, LocalDateTime now) {
        ensureNotClosed();
        if (principalBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("principal balance is not enough");
        }
        principalBalance = normalizeAmount(principalBalance.minus(amount));
        principalUnreachAmount = normalizeAmount(principalUnreachAmount.plus(amount));
        touch(now);
    }

    void confirmPrincipalForRepay(Money amount, LocalDateTime now) {
        ensureUnreachEnough(principalUnreachAmount, amount, "principal unreach amount is not enough");
        principalUnreachAmount = normalizeAmount(principalUnreachAmount.minus(amount));
        touch(now);
    }

    void cancelPrincipalForRepay(Money amount, LocalDateTime now) {
        ensureUnreachEnough(principalUnreachAmount, amount, "principal unreach amount is not enough");
        principalUnreachAmount = normalizeAmount(principalUnreachAmount.minus(amount));
        principalBalance = normalizeAmount(principalBalance.plus(amount));
        touch(now);
    }

    void holdOverduePrincipalForRepay(Money amount, LocalDateTime now) {
        ensureNotClosed();
        if (overduePrincipalBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("overdue principal balance is not enough");
        }
        overduePrincipalBalance = normalizeAmount(overduePrincipalBalance.minus(amount));
        overduePrincipalUnreachAmount = normalizeAmount(overduePrincipalUnreachAmount.plus(amount));
        touch(now);
    }

    void confirmOverduePrincipalForRepay(Money amount, LocalDateTime now) {
        ensureUnreachEnough(overduePrincipalUnreachAmount, amount, "overdue principal unreach amount is not enough");
        overduePrincipalUnreachAmount = normalizeAmount(overduePrincipalUnreachAmount.minus(amount));
        touch(now);
    }

    void cancelOverduePrincipalForRepay(Money amount, LocalDateTime now) {
        ensureUnreachEnough(overduePrincipalUnreachAmount, amount, "overdue principal unreach amount is not enough");
        overduePrincipalUnreachAmount = normalizeAmount(overduePrincipalUnreachAmount.minus(amount));
        overduePrincipalBalance = normalizeAmount(overduePrincipalBalance.plus(amount));
        touch(now);
    }

    void holdInterestForRepay(Money amount, LocalDateTime now) {
        ensureNotClosed();
        if (interestBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("interest balance is not enough");
        }
        interestBalance = normalizeAmount(interestBalance.minus(amount));
        touch(now);
    }

    void confirmInterestForRepay(Money amount, LocalDateTime now) {
        requirePositiveAmount(amount);
        touch(now);
    }

    void cancelInterestForRepay(Money amount, LocalDateTime now) {
        ensureNotClosed();
        Money normalizedAmount = requirePositiveAmount(amount);
        interestBalance = normalizeAmount(interestBalance.plus(normalizedAmount));
        touch(now);
    }

    void holdFineForRepay(Money amount, LocalDateTime now) {
        ensureNotClosed();
        if (fineBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("fine balance is not enough");
        }
        fineBalance = normalizeAmount(fineBalance.minus(amount));
        touch(now);
    }

    void confirmFineForRepay(Money amount, LocalDateTime now) {
        requirePositiveAmount(amount);
        touch(now);
    }

    void cancelFineForRepay(Money amount, LocalDateTime now) {
        ensureNotClosed();
        Money normalizedAmount = requirePositiveAmount(amount);
        fineBalance = normalizeAmount(fineBalance.plus(normalizedAmount));
        touch(now);
    }

    private Money outstandingExposure() {
        return normalizeAmount(principalBalance
                .plus(principalUnreachAmount)
                .plus(overduePrincipalBalance)
                .plus(overduePrincipalUnreachAmount));
    }

    private boolean isClear() {
        Money zero = zeroMoney();
        return principalBalance.compareTo(zero) == 0
                && principalUnreachAmount.compareTo(zero) == 0
                && overduePrincipalBalance.compareTo(zero) == 0
                && overduePrincipalUnreachAmount.compareTo(zero) == 0
                && interestBalance.compareTo(zero) == 0
                && fineBalance.compareTo(zero) == 0;
    }

    private void ensureStatus(CreditAccountStatus expectedStatus, String message) {
        if (accountStatus != expectedStatus) {
            throw new IllegalStateException(message);
        }
    }

    private void ensureNotClosed() {
        if (accountStatus == CreditAccountStatus.CLOSE) {
            throw new IllegalStateException("closed account is not allowed");
        }
    }

    private void ensureUnreachEnough(Money unreachAmount, Money amount, String message) {
        if (unreachAmount.compareTo(amount) < 0) {
            throw new IllegalStateException(message);
        }
    }

    private Money requirePositiveAmount(Money amount) {
        Money normalizedAmount = normalizeAmount(amount);
        if (normalizedAmount.compareTo(zeroMoney()) <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        return normalizedAmount;
    }

    private Money normalizeAmount(Money amount) {
        if (amount == null) {
            return zeroMoney().rounded(2, RoundingMode.HALF_UP);
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
    }

    private void touch(LocalDateTime now) {
        assertNonNegativeState();
        this.updatedAt = now;
    }

    private void assertNonNegativeState() {
        ensureNonNegative(totalLimit, "totalLimit");
        ensureNonNegative(principalBalance, "principalBalance");
        ensureNonNegative(principalUnreachAmount, "principalUnreachAmount");
        ensureNonNegative(overduePrincipalBalance, "overduePrincipalBalance");
        ensureNonNegative(overduePrincipalUnreachAmount, "overduePrincipalUnreachAmount");
        ensureNonNegative(interestBalance, "interestBalance");
        ensureNonNegative(fineBalance, "fineBalance");
    }

    private void ensureNonNegative(Money amount, String fieldName) {
        if (amount == null) {
            return;
        }
        if (amount.compareTo(Money.zero(amount.getCurrencyUnit())) < 0) {
            throw new IllegalStateException(fieldName + " must not be less than 0");
        }
    }

    private int normalizeRepayDayOfMonth(int dayOfMonth) {
        if (dayOfMonth <= 0) {
            return 10;
        }
        if (dayOfMonth > 28) {
            throw new IllegalArgumentException("repayDayOfMonth must be between 1 and 28");
        }
        return dayOfMonth;
    }

    private static Money zeroMoney() {
        return Money.zero(CurrencyUnit.of("CNY"));
    }
}
