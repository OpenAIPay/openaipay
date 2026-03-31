package cn.openaipay.domain.creditaccount.model;

/**
 * 信用账户状态枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum CreditAccountStatus {
    /**
      * 正常状态，可执行常规业务操作。
       */
    NORMAL,
    /**
      * 冻结放款能力，禁止新增借款。
       */
    FREEZE_LEND,
    /**
      * 账户整体冻结，禁止全部资金操作。
       */
    FREEZE_ACCOUNT,
    /**
      * CLOSE 取值。
       */
    CLOSE
}
