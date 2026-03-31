package cn.openaipay.application.pay.command;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.json.JsonWriter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付执行结算计划快照。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record SettlementPlanSnapshot(
        /** 原始金额 */
        Money originalAmount,
        /** 钱包金额 */
        Money walletDebitAmount,
        /** 资金金额 */
        Money fundDebitAmount,
        /** 信用金额 */
        Money creditDebitAmount,
        /** 入金金额 */
        Money inboundDebitAmount,
        /** 出金金额 */
        Money outboundAmount,
        /** 优惠券单号 */
        String couponNo,
        /** 资金编码 */
        String fundCode,
        /** 业务编码 */
        String paymentToolCode,
        /** 支付方式编码 */
        String paymentMethod
) {
    /**
     * 转换为业务数据。
     */
    public String toPayload() {
        CurrencyUnit currencyUnit = resolveCurrency();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("currencyCode", currencyUnit.getCode());
        payload.put("originalAmount", amountText(originalAmount));
        payload.put("walletDebitAmount", amountText(walletDebitAmount));
        payload.put("fundDebitAmount", amountText(fundDebitAmount));
        payload.put("creditDebitAmount", amountText(creditDebitAmount));
        payload.put("inboundDebitAmount", amountText(inboundDebitAmount));
        if (outboundAmount != null) {
            payload.put("outboundAmount", amountText(outboundAmount));
        }
        if (couponNo != null && !couponNo.isBlank()) {
            payload.put("couponNo", couponNo.trim());
        }
        if (fundCode != null && !fundCode.isBlank()) {
            payload.put("fundCode", fundCode.trim());
        }
        if (paymentToolCode != null && !paymentToolCode.isBlank()) {
            payload.put("paymentToolCode", paymentToolCode.trim());
        }
        if (paymentMethod != null && !paymentMethod.isBlank()) {
            payload.put("paymentMethod", paymentMethod.trim());
        }
        return JsonWriter.standard().writeToString(payload);
    }

    /**
     * 处理业务数据。
     */
    public static SettlementPlanSnapshot fromPayload(String payload) {
        Map<String, Object> raw = JsonParserFactory.getJsonParser().parseMap(payload);
        CurrencyUnit currencyUnit = CurrencyUnit.of(stringValue(raw.get("currencyCode"), "CNY"));
        return new SettlementPlanSnapshot(
                moneyValue(raw.get("originalAmount"), currencyUnit, false),
                moneyValue(raw.get("walletDebitAmount"), currencyUnit, true),
                moneyValue(raw.get("fundDebitAmount"), currencyUnit, true),
                moneyValue(raw.get("creditDebitAmount"), currencyUnit, true),
                moneyValue(raw.get("inboundDebitAmount"), currencyUnit, true),
                moneyValue(raw.get("outboundAmount"), currencyUnit, true),
                stringValue(raw.get("couponNo"), null),
                stringValue(raw.get("fundCode"), null),
                stringValue(raw.get("paymentToolCode"), null),
                stringValue(raw.get("paymentMethod"), null)
        );
    }

    private CurrencyUnit resolveCurrency() {
        if (originalAmount != null) {
            return originalAmount.getCurrencyUnit();
        }
        if (walletDebitAmount != null) {
            return walletDebitAmount.getCurrencyUnit();
        }
        if (fundDebitAmount != null) {
            return fundDebitAmount.getCurrencyUnit();
        }
        if (creditDebitAmount != null) {
            return creditDebitAmount.getCurrencyUnit();
        }
        if (inboundDebitAmount != null) {
            return inboundDebitAmount.getCurrencyUnit();
        }
        if (outboundAmount != null) {
            return outboundAmount.getCurrencyUnit();
        }
        return CurrencyUnit.of("CNY");
    }

    private static String amountText(Money amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String stringValue(Object rawValue, String defaultValue) {
        if (rawValue == null) {
            return defaultValue;
        }
        String text = String.valueOf(rawValue).trim();
        return text.isEmpty() ? defaultValue : text;
    }

    private static Money moneyValue(Object rawValue, CurrencyUnit currencyUnit, boolean optional) {
        if (rawValue == null) {
            return optional ? null : Money.zero(currencyUnit);
        }
        BigDecimal amount = new BigDecimal(String.valueOf(rawValue));
        return Money.of(currencyUnit, amount).rounded(2, RoundingMode.HALF_UP);
    }
}
