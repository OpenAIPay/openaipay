package cn.openaipay.domain.trade.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 基金业务交易扩展单模型。
 *
 * 业务场景：爱存申购、转出、收益结转等业务除了统一交易主单，还需要额外保存基金账户号、账单号、份额和净值日期。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public class TradeFundOrder {
    /** 扩展单主键ID。 */
    private final Long id;
    /** 基金业务交易单号。 */
    private final String bizOrderNo;
    /** 关联统一交易主单号。 */
    private final String tradeOrderNo;
    /** 基金产品类型。 */
    private final TradeFundProductType fundProductType;
    /** 基金账户号。 */
    private final String fundAccountNo;
    /** 基金账单号。 */
    private final String billNo;
    /** 基金账单月份。 */
    private final String billMonth;
    /** 基金业务交易类型。 */
    private final TradeFundTradeType fundTradeType;
    /** 份额变化量。 */
    private final BigDecimal shareAmount;
    /** 确认金额。 */
    private final Money confirmAmount;
    /** 净值日期。 */
    private final LocalDate navDate;
    /** 业务发生时间。 */
    private final LocalDateTime occurredAt;
    /** 记录创建时间。 */
    private final LocalDateTime createdAt;
    /** 记录更新时间。 */
    private final LocalDateTime updatedAt;

    public TradeFundOrder(Long id,
                          String bizOrderNo,
                          String tradeOrderNo,
                          TradeFundProductType fundProductType,
                          String fundAccountNo,
                          String billNo,
                          String billMonth,
                          TradeFundTradeType fundTradeType,
                          BigDecimal shareAmount,
                          Money confirmAmount,
                          LocalDate navDate,
                          LocalDateTime occurredAt,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.id = id;
        this.bizOrderNo = normalizeRequired(bizOrderNo, "bizOrderNo");
        this.tradeOrderNo = normalizeRequired(tradeOrderNo, "tradeOrderNo");
        this.fundProductType = fundProductType == null ? TradeFundProductType.AICASH : fundProductType;
        this.fundAccountNo = normalizeRequired(fundAccountNo, "fundAccountNo");
        this.billNo = normalizeOptional(billNo);
        this.billMonth = normalizeOptional(billMonth);
        this.fundTradeType = fundTradeType == null ? TradeFundTradeType.PURCHASE : fundTradeType;
        this.shareAmount = normalizeShareAmount(shareAmount);
        CurrencyUnit currency = resolveCurrencyUnit(confirmAmount);
        this.confirmAmount = normalizeConfirmAmount(confirmAmount, currency);
        this.navDate = navDate;
        this.occurredAt = occurredAt == null ? LocalDateTime.now() : occurredAt;
        this.createdAt = createdAt == null ? this.occurredAt : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
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

    private BigDecimal normalizeShareAmount(BigDecimal rawShareAmount) {
        BigDecimal resolved = rawShareAmount == null ? BigDecimal.ZERO : rawShareAmount;
        if (resolved.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("shareAmount must be greater than or equal to 0");
        }
        return resolved.setScale(8, RoundingMode.HALF_UP);
    }

    private CurrencyUnit resolveCurrencyUnit(Money amount) {
        if (amount != null) {
            return amount.getCurrencyUnit();
        }
        return CurrencyUnit.of("CNY");
    }

    private Money normalizeConfirmAmount(Money amount, CurrencyUnit currencyUnit) {
        Money resolved = amount == null ? Money.zero(currencyUnit) : amount;
        if (!resolved.getCurrencyUnit().equals(currencyUnit)) {
            throw new IllegalArgumentException("confirmAmount currency must be consistent");
        }
        if (resolved.isNegative()) {
            throw new IllegalArgumentException("confirmAmount must be greater than or equal to 0");
        }
        return resolved.rounded(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取业务订单NO信息。
     */
    public String getBizOrderNo() {
        return bizOrderNo;
    }

    /**
     * 获取交易订单NO信息。
     */
    public String getTradeOrderNo() {
        return tradeOrderNo;
    }

    /**
     * 获取基金信息。
     */
    public TradeFundProductType getFundProductType() {
        return fundProductType;
    }

    /**
     * 获取基金账户NO信息。
     */
    public String getFundAccountNo() {
        return fundAccountNo;
    }

    /**
     * 获取账单NO信息。
     */
    public String getBillNo() {
        return billNo;
    }

    /**
     * 获取业务数据。
     */
    public String getBillMonth() {
        return billMonth;
    }

    /**
     * 获取基金交易类型信息。
     */
    public TradeFundTradeType getFundTradeType() {
        return fundTradeType;
    }

    /**
     * 获取份额金额。
     */
    public BigDecimal getShareAmount() {
        return shareAmount;
    }

    /**
     * 获取金额。
     */
    public Money getConfirmAmount() {
        return confirmAmount;
    }

    /**
     * 获取NAV日期信息。
     */
    public LocalDate getNavDate() {
        return navDate;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getOccurredAt() {
        return occurredAt;
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
