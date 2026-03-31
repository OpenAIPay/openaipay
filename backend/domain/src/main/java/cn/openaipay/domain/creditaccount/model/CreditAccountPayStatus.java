package cn.openaipay.domain.creditaccount.model;

/**
 * 信用账户支付状态枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum CreditAccountPayStatus {
    /**
      * 正常状态，可执行常规业务操作。
       */
    NORMAL,
    /**
      * 账务已还清或已完成支付。
       */
    PAID
}
