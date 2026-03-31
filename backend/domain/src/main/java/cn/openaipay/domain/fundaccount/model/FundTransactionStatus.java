package cn.openaipay.domain.fundaccount.model;

/**
 * 基金交易状态枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum FundTransactionStatus {
    /**
      * 交易已受理，等待后续确认。
       */
    PENDING,
    /**
      * CONFIRM阶段执行成功。
       */
    CONFIRMED,
    /**
      * CANCEL阶段执行成功。
       */
    CANCELED,
    /**
      * COMPENSATE阶段执行成功。
       */
    COMPENSATED,
    /**
      * 交易被拒绝，不会进入确认流程。
       */
    REJECTED
}
