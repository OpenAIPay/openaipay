package cn.openaipay.domain.fundaccount.model;

/**
 * 基金账户状态枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum FundAccountStatus {
    /**
      * 已启用状态，可参与线上业务流程。
       */
    ACTIVE,
    /**
      * 冻结状态，暂不可执行关键操作。
       */
    FROZEN,
    /**
      * 已关闭状态，后续交易能力不可用。
       */
    CLOSED
}
