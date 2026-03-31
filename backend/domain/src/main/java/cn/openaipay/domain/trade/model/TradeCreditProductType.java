package cn.openaipay.domain.trade.model;

import cn.openaipay.domain.creditaccount.model.CreditProductCodes;

import java.util.Locale;

/**
 * 信用业务产品类型。
 *
 * 业务场景：信用交易扩展单需要明确区分爱花与爱借，便于后台按产品线查询业务交易。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public enum TradeCreditProductType {
    /** 爱花信用产品。 */
    AICREDIT,
    /** 爱借信用产品。 */
    AILOAN;

    /**
     * 处理业务数据。
     */
    public static TradeCreditProductType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("creditProductType must not be blank");
        }
        String normalizedCode = CreditProductCodes.normalizeOrDefault(raw);
        try {
            return TradeCreditProductType.valueOf(normalizedCode.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported creditProductType: " + raw);
        }
    }
}
