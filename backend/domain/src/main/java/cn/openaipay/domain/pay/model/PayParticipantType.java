package cn.openaipay.domain.pay.model;

import java.util.Locale;

/**
 * 支付参与方类型枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum PayParticipantType {
    /**
      * 红包参与方分支。
       */
    COUPON,
    /**
      * 钱包账户参与方分支。
       */
    WALLET_ACCOUNT,
    /**
      * 基金账户参与方分支。
       */
    FUND_ACCOUNT,
    /**
      * 信用账户参与方分支。
       */
    CREDIT_ACCOUNT,
    /**
      * 入金网关参与方分支。
       */
    INBOUND,
    /**
      * 出金网关参与方分支。
       */
    OUTBOUND;

    /**
     * 处理业务数据。
     */
    public static PayParticipantType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("participantType must not be blank");
        }
        try {
            return PayParticipantType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported participantType: " + raw);
        }
    }
}
