package cn.openaipay.domain.bankcard.model;

import java.util.Locale;

/**
 * 银行卡类型枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum BankCardType {
    /**
     * 借记卡，可直接使用账户存款完成付款。
     */
    DEBIT,
    /**
     * 信用卡，可先消费后还款。
     */
    CREDIT;

    /**
     * 处理业务数据。
     */
    public static BankCardType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("bank card type must not be blank");
        }
        try {
            return BankCardType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported bank card type: " + raw);
        }
    }
}
