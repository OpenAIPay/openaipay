package cn.openaipay.domain.pay.model;

import java.util.Locale;

/**
 * 支付订单状态枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum PayOrderStatus {
    /**
      * 单据已创建，等待后续业务编排。
       */
    CREATED,
    /**
      * 支付请求已受理，等待异步执行。
       */
    SUBMITTED,
    /**
      * 正在执行TRY阶段，准备冻结或预占资金。
       */
    TRYING,
    /**
      * TRY阶段全部成功，已具备提交条件。
       */
    PREPARED,
    /**
      * 正在执行确认提交，准备落账。
       */
    COMMITTING,
    /**
      * 确认提交完成，业务正式生效。
       */
    COMMITTED,
    /**
      * 支付结果待对账或补偿处理。
       */
    RECON_PENDING,
    /**
      * 正在执行回滚流程，释放预占资源。
       */
    ROLLING_BACK,
    /**
      * 回滚完成，状态恢复到失败前。
       */
    ROLLED_BACK,
    /**
      * 流程执行失败，需人工或系统补偿处理。
       */
    FAILED;

    /**
     * 处理业务数据。
     */
    public static PayOrderStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("pay order status must not be blank");
        }
        try {
            return PayOrderStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported pay order status: " + raw);
        }
    }
}
