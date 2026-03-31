package cn.openaipay.domain.trade.model;

import cn.openaipay.domain.fundaccount.model.FundProductCodes;

import java.util.Locale;

/**
 * 基金业务产品类型。
 *
 * 业务场景：爱存当前归属于基金业务域，后续可扩展更多理财产品并沿用同一查询模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public enum TradeFundProductType {
    /** 爱存产品。 */
    AICASH;

    /**
     * 处理业务数据。
     */
    public static TradeFundProductType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("fundProductType must not be blank");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (FundProductCodes.isPrimaryFundCode(normalized)) {
            return AICASH;
        }
        try {
            return TradeFundProductType.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported fundProductType: " + raw);
        }
    }
}
