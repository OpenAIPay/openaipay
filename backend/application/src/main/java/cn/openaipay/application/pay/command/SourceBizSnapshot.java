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
 * 来源业务执行快照。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record SourceBizSnapshot(
        /** 来源交易类型 */
        String sourceTradeType,
        /** 结算金额 */
        Money settleAmount,
        /** 收款方信用信息 */
        Boolean requiresPayeeCredit
) {
    /**
     * 转换为业务数据。
     */
    public String toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sourceTradeType", sourceTradeType);
        if (settleAmount != null) {
            payload.put("settleAmount", settleAmount.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
            payload.put("currencyCode", settleAmount.getCurrencyUnit().getCode());
        }
        if (requiresPayeeCredit != null) {
            payload.put("requiresPayeeCredit", requiresPayeeCredit);
        }
        return JsonWriter.standard().writeToString(payload);
    }

    /**
     * 处理业务数据。
     */
    public static SourceBizSnapshot fromPayload(String payload) {
        Map<String, Object> raw = JsonParserFactory.getJsonParser().parseMap(payload);
        String sourceTradeType = stringValue(raw.get("sourceTradeType"));
        String currencyCode = stringValue(raw.get("currencyCode"));
        String settleAmountText = stringValue(raw.get("settleAmount"));
        Money settleAmount = null;
        if (settleAmountText != null) {
            CurrencyUnit currencyUnit = CurrencyUnit.of(currencyCode == null ? "CNY" : currencyCode);
            settleAmount = Money.of(currencyUnit, new BigDecimal(settleAmountText)).rounded(2, RoundingMode.HALF_UP);
        }
        Boolean requiresPayeeCredit = booleanValue(raw.get("requiresPayeeCredit"));
        return new SourceBizSnapshot(sourceTradeType, settleAmount, requiresPayeeCredit);
    }

    private static String stringValue(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        String value = String.valueOf(rawValue).trim();
        return value.isEmpty() ? null : value;
    }

    private static Boolean booleanValue(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return Boolean.parseBoolean(String.valueOf(rawValue));
    }
}
