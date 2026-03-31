package cn.openaipay.domain.bankcard.model;

import java.util.Locale;

/**
 * 银行卡状态枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum BankCardStatus {
    /**
     * 已激活状态，可在收银台中用于付款。
     */
    ACTIVE,
    /**
     * 已停用状态，保留绑卡关系但不可用于付款。
     */
    INACTIVE,
    /**
     * 已解绑状态，仅保留历史记录。
     */
    UNBOUND;

    /**
     * 处理业务数据。
     */
    public static BankCardStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("bank card status must not be blank");
        }
        try {
            return BankCardStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported bank card status: " + raw);
        }
    }
}
