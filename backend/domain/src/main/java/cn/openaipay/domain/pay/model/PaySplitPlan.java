package cn.openaipay.domain.pay.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付参与方扣款拆分计划。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public class PaySplitPlan {

    /** 钱包金额 */
    private final Money walletDebitAmount;
    /** 资金金额 */
    private final Money fundDebitAmount;
    /** 信用金额 */
    private final Money creditDebitAmount;
    /** 入金金额 */
    private final Money inboundDebitAmount;

    private PaySplitPlan(CurrencyUnit currencyUnit,
                         Money walletDebitAmount,
                         Money fundDebitAmount,
                         Money creditDebitAmount,
                         Money inboundDebitAmount) {
        CurrencyUnit normalizedCurrency = requireCurrencyUnit(currencyUnit);
        this.walletDebitAmount = normalizeNonNegative(walletDebitAmount, "walletDebitAmount", normalizedCurrency);
        this.fundDebitAmount = normalizeNonNegative(fundDebitAmount, "fundDebitAmount", normalizedCurrency);
        this.creditDebitAmount = normalizeNonNegative(creditDebitAmount, "creditDebitAmount", normalizedCurrency);
        this.inboundDebitAmount = normalizeNonNegative(inboundDebitAmount, "inboundDebitAmount", normalizedCurrency);
    }

    /**
     * 处理OF信息。
     */
    public static PaySplitPlan of(CurrencyUnit currencyUnit,
                                  Money walletDebitAmount,
                                  Money fundDebitAmount,
                                  Money creditDebitAmount,
                                  Money inboundDebitAmount) {
        return new PaySplitPlan(currencyUnit, walletDebitAmount, fundDebitAmount, creditDebitAmount, inboundDebitAmount);
    }

    /**
     * 处理业务数据。
     */
    public static PaySplitPlan empty(CurrencyUnit currencyUnit) {
        return new PaySplitPlan(currencyUnit, null, null, null, null);
    }

    /**
     * 处理金额。
     */
    public Money totalDebitAmount() {
        return walletDebitAmount
                .plus(fundDebitAmount)
                .plus(creditDebitAmount)
                .plus(inboundDebitAmount)
                .rounded(2, RoundingMode.HALF_UP);
    }

    /**
     * 转换为业务数据。
     */
    public String toPayload() {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("currencyCode", walletDebitAmount.getCurrencyUnit().getCode());
        payload.put("walletDebitAmount", amountText(walletDebitAmount));
        payload.put("fundDebitAmount", amountText(fundDebitAmount));
        payload.put("creditDebitAmount", amountText(creditDebitAmount));
        payload.put("inboundDebitAmount", amountText(inboundDebitAmount));
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            if (builder.length() > 0) {
                builder.append(';');
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return builder.toString();
    }

    /**
     * 处理业务数据。
     */
    public static PaySplitPlan fromPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("split plan payload must not be blank");
        }
        Map<String, String> values = parsePayload(payload);
        CurrencyUnit currencyUnit = CurrencyUnit.of(values.getOrDefault("currencyCode", "CNY"));
        return PaySplitPlan.of(
                currencyUnit,
                moneyValue(values.get("walletDebitAmount"), currencyUnit),
                moneyValue(values.get("fundDebitAmount"), currencyUnit),
                moneyValue(values.get("creditDebitAmount"), currencyUnit),
                moneyValue(values.get("inboundDebitAmount"), currencyUnit)
        );
    }

    /**
     * 获取钱包金额。
     */
    public Money getWalletDebitAmount() {
        return walletDebitAmount;
    }

    /**
     * 获取基金金额。
     */
    public Money getFundDebitAmount() {
        return fundDebitAmount;
    }

    /**
     * 获取信用金额。
     */
    public Money getCreditDebitAmount() {
        return creditDebitAmount;
    }

    /**
     * 获取入金金额。
     */
    public Money getInboundDebitAmount() {
        return inboundDebitAmount;
    }

    private static CurrencyUnit requireCurrencyUnit(CurrencyUnit currencyUnit) {
        if (currencyUnit == null) {
            throw new IllegalArgumentException("currencyUnit must not be null");
        }
        return currencyUnit;
    }

    private static String amountText(Money amount) {
        return amount.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static Map<String, String> parsePayload(String payload) {
        Map<String, String> values = new LinkedHashMap<>();
        String[] parts = payload.split(";");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && !kv[0].isBlank()) {
                values.put(kv[0].trim(), kv[1].trim());
            }
        }
        return values;
    }

    private static Money moneyValue(String raw, CurrencyUnit currencyUnit) {
        if (raw == null || raw.isBlank()) {
            return Money.zero(currencyUnit).rounded(2, RoundingMode.HALF_UP);
        }
        return Money.of(currencyUnit, new BigDecimal(raw)).rounded(2, RoundingMode.HALF_UP);
    }

    private static Money normalizeNonNegative(Money amount, String fieldName, CurrencyUnit currencyUnit) {
        if (amount == null) {
            return Money.zero(currencyUnit).rounded(2, RoundingMode.HALF_UP);
        }
        if (!currencyUnit.equals(amount.getCurrencyUnit())) {
            throw new IllegalArgumentException(fieldName + " currency must equal split plan currency");
        }
        if (amount.isLessThan(Money.zero(currencyUnit))) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
    }
}
