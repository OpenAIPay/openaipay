package cn.openaipay.domain.pay.model;

import java.util.Locale;

/**
 * 支付资金明细归属方枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum PayFundDetailOwner {
    /**
     * 付款方
     */
    PAYER,
    /**
     * 收款方
     */
    PAYEE;

    /**
     * 处理业务数据。
     */
    public static PayFundDetailOwner from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("payFundDetailOwner must not be blank");
        }
        try {
            return PayFundDetailOwner.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported payFundDetailOwner: " + raw);
        }
    }
}
