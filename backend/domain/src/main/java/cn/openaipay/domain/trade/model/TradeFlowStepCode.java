package cn.openaipay.domain.trade.model;

import java.util.Locale;

/**
 * 交易流程步骤Code枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum TradeFlowStepCode {
    /**
      * 交易计费报价步骤。
       */
    PRICING_QUOTE,
    /**
      * 支付异步提交步骤。
       */
    PAY_SUBMIT,
    /**
      * 支付预处理步骤（TRY）。
       */
    PAY_PREPARE,
    /**
      * 支付提交步骤（CONFIRM）。
       */
    PAY_COMMIT,
    /**
      * 支付回滚步骤（CANCEL）。
       */
    PAY_ROLLBACK,
    /**
      * 充值入账步骤（CREDIT）。
       */
    DEPOSIT_CREDIT,
    /**
      * 转账收款入账步骤（CREDIT）。
       */
    TRANSFER_CREDIT,
    /**
      * 支付收款方入账步骤（CREDIT）。
       */
    PAYEE_CREDIT,
    /**
      * 支付结果回写交易步骤。
       */
    PAY_RESULT_APPLY,
    /**
      * 转账入账失败后的付款方补偿加款步骤（CREDIT）。
       */
    TRANSFER_COMPENSATE;

    /**
     * 处理业务数据。
     */
    public static TradeFlowStepCode from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("stepCode must not be blank");
        }
        try {
            return TradeFlowStepCode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported stepCode: " + raw);
        }
    }
}
