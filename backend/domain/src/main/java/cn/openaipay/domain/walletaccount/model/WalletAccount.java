package cn.openaipay.domain.walletaccount.model;

import org.joda.money.Money;
import org.joda.money.CurrencyUnit;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 钱包账户模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class WalletAccount {

    /** 用户ID */
    private final Long userId;
    /** 币种编码 */
    private final String currencyCode;
    /** 可用余额 */
    private Money availableBalance;
    /** 冻结余额 */
    private Money reservedBalance;
    /** 账户状态 */
    private WalletAccountStatus accountStatus;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public WalletAccount(Long userId,
                         String currencyCode,
                         Money availableBalance,
                         Money reservedBalance,
                         WalletAccountStatus accountStatus,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.userId = userId;
        this.currencyCode = normalizeCurrencyCode(currencyCode);
        this.availableBalance = normalizeAmount(availableBalance);
        this.reservedBalance = normalizeAmount(reservedBalance);
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        assertNonNegativeState();
    }

    /**
     * 开通业务数据。
     */
    public static WalletAccount open(Long userId, String currencyCode, LocalDateTime now) {
        CurrencyUnit currencyUnit = resolveCurrencyUnit(currencyCode);
        return new WalletAccount(
                userId,
                currencyCode,
                Money.zero(currencyUnit),
                Money.zero(currencyUnit),
                WalletAccountStatus.ACTIVE,
                now,
                now
        );
    }

    /**
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取编码。
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * 获取业务数据。
     */
    public Money getAvailableBalance() {
        return availableBalance;
    }

    /**
     * 获取业务数据。
     */
    public Money getReservedBalance() {
        return reservedBalance;
    }

    /**
     * 获取账户状态。
     */
    public WalletAccountStatus getAccountStatus() {
        return accountStatus;
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
     * 处理业务数据。
     */
    public void hold(TccOperationType operationType, Money amount, LocalDateTime now) {
        ensureActive();
        Money normalizedAmount = requirePositiveAmount(amount);
        if (operationType == TccOperationType.DEBIT) {
            if (availableBalance.compareTo(normalizedAmount) < 0) {
                throw new IllegalArgumentException("insufficient available balance");
            }
            availableBalance = normalizeAmount(availableBalance.minus(normalizedAmount));
        }
        reservedBalance = normalizeAmount(reservedBalance.plus(normalizedAmount));
        touch(now);
    }

    /**
     * 确认业务数据。
     */
    public void confirm(TccOperationType operationType, Money amount, LocalDateTime now) {
        ensureActive();
        Money normalizedAmount = requirePositiveAmount(amount);
        ensureReservedEnough(normalizedAmount);
        reservedBalance = normalizeAmount(reservedBalance.minus(normalizedAmount));
        if (operationType == TccOperationType.CREDIT) {
            availableBalance = normalizeAmount(availableBalance.plus(normalizedAmount));
        }
        touch(now);
    }

    /**
     * 取消业务数据。
     */
    public void cancel(TccOperationType operationType, Money amount, LocalDateTime now) {
        ensureActive();
        Money normalizedAmount = requirePositiveAmount(amount);
        ensureReservedEnough(normalizedAmount);
        reservedBalance = normalizeAmount(reservedBalance.minus(normalizedAmount));
        if (operationType == TccOperationType.DEBIT) {
            availableBalance = normalizeAmount(availableBalance.plus(normalizedAmount));
        }
        touch(now);
    }

    private void ensureActive() {
        if (accountStatus != WalletAccountStatus.ACTIVE) {
            throw new IllegalStateException("wallet account status is not ACTIVE");
        }
    }

    private void ensureReservedEnough(Money amount) {
        if (reservedBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("reserved balance is not enough");
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
        int scale = resolveCurrencyUnit(this.currencyCode).getDecimalPlaces();
        if (amount == null) {
            return zeroMoney().rounded(scale, RoundingMode.HALF_UP);
        }
        return amount.rounded(amount.getCurrencyUnit().getDecimalPlaces(), RoundingMode.HALF_UP);
    }

    private void touch(LocalDateTime now) {
        assertNonNegativeState();
        this.updatedAt = now;
    }

    private void assertNonNegativeState() {
        ensureNonNegative(availableBalance, "availableBalance");
        ensureNonNegative(reservedBalance, "reservedBalance");
    }

    private void ensureNonNegative(Money amount, String fieldName) {
        if (amount == null) {
            return;
        }
        if (amount.compareTo(Money.zero(amount.getCurrencyUnit())) < 0) {
            throw new IllegalStateException(fieldName + " must not be less than 0");
        }
    }

    private Money zeroMoney() {
        return Money.zero(resolveCurrencyUnit(this.currencyCode));
    }

    private static String normalizeCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return "CNY";
        }
        return currencyCode.trim().toUpperCase();
    }

    private static CurrencyUnit resolveCurrencyUnit(String currencyCode) {
        return CurrencyUnit.of(normalizeCurrencyCode(currencyCode));
    }
}
