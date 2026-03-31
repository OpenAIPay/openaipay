package cn.openaipay.domain.pay.model;

import java.util.Locale;

/**
 * 支付资金明细工具类型枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum PayFundDetailTool {
    /**
     * 银行卡
     */
    BANK_CARD,
    /**
     * 红包
     */
    RED_PACKET,
    /**
     * 钱包
     */
    WALLET,
    /**
     * 基金
     */
    FUND,
    /**
     * 信用
     */
    CREDIT;

    /**
     * 处理业务数据。
     */
    public static PayFundDetailTool from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("payFundDetailTool must not be blank");
        }
        try {
            return PayFundDetailTool.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported payFundDetailTool: " + raw);
        }
    }
}
