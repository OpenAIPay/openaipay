package cn.openaipay.application.settle.async;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.json.JsonWriter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 结算成功后触发会计过账的异步消息载荷。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record SettleAccountingEventRequestedPayload(
        /** 交易类型 */
        String tradeType,
        /** 付款方用户ID */
        Long payerUserId,
        /** 收款方用户ID */
        Long payeeUserId,
        /** 支付单号 */
        String payOrderNo,
        /** 请求幂等号 */
        String requestNo,
        /** 交易主单号 */
        String tradeOrderNo,
        /** 结算业务单号 */
        String settleBizNo,
        /** 结算金额 */
        Money settleAmount,
        /** 原始金额 */
        Money originalAmount,
        /** 应付金额 */
        Money payableAmount,
        /** 信用收款方信息 */
        boolean shouldCreditPayee
) {
    /**
     * 转换为业务数据。
     */
    public String toPayload() {
        CurrencyUnit currencyUnit = resolveCurrencyUnit();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tradeType", tradeType);
        payload.put("payerUserId", payerUserId);
        payload.put("payeeUserId", payeeUserId);
        payload.put("payOrderNo", payOrderNo);
        payload.put("requestNo", requestNo);
        payload.put("tradeOrderNo", tradeOrderNo);
        payload.put("settleBizNo", settleBizNo);
        payload.put("currencyCode", currencyUnit.getCode());
        payload.put("settleAmount", amountText(settleAmount));
        payload.put("originalAmount", amountText(originalAmount));
        payload.put("payableAmount", amountText(payableAmount));
        payload.put("shouldCreditPayee", shouldCreditPayee);
        return JsonWriter.standard().writeToString(payload);
    }

    /**
     * 处理业务数据。
     */
    public static SettleAccountingEventRequestedPayload fromPayload(String payload) {
        Map<String, Object> raw = JsonParserFactory.getJsonParser().parseMap(payload);
        CurrencyUnit currencyUnit = CurrencyUnit.of(stringValue(raw.get("currencyCode"), "CNY"));
        return new SettleAccountingEventRequestedPayload(
                stringValue(raw.get("tradeType"), null),
                longValue(raw.get("payerUserId")),
                longValue(raw.get("payeeUserId")),
                stringValue(raw.get("payOrderNo"), null),
                stringValue(raw.get("requestNo"), null),
                stringValue(raw.get("tradeOrderNo"), null),
                stringValue(raw.get("settleBizNo"), null),
                moneyValue(raw.get("settleAmount"), currencyUnit),
                moneyValue(raw.get("originalAmount"), currencyUnit),
                moneyValue(raw.get("payableAmount"), currencyUnit),
                booleanValue(raw.get("shouldCreditPayee"))
        );
    }

    private CurrencyUnit resolveCurrencyUnit() {
        if (settleAmount != null) {
            return settleAmount.getCurrencyUnit();
        }
        if (originalAmount != null) {
            return originalAmount.getCurrencyUnit();
        }
        if (payableAmount != null) {
            return payableAmount.getCurrencyUnit();
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
        String value = String.valueOf(rawValue).trim();
        return value.isEmpty() ? defaultValue : value;
    }

    private static Long longValue(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(rawValue));
    }

    private static Money moneyValue(Object rawValue, CurrencyUnit currencyUnit) {
        if (rawValue == null) {
            return Money.zero(currencyUnit).rounded(2, RoundingMode.HALF_UP);
        }
        return Money.of(currencyUnit, new BigDecimal(String.valueOf(rawValue))).rounded(2, RoundingMode.HALF_UP);
    }

    private static boolean booleanValue(Object rawValue) {
        if (rawValue == null) {
            return false;
        }
        if (rawValue instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return Boolean.parseBoolean(String.valueOf(rawValue));
    }
}
