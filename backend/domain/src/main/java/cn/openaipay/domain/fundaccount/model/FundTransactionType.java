package cn.openaipay.domain.fundaccount.model;

/**
 * 基金交易类型枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum FundTransactionType {
    /**
      * 基金申购交易。
       */
    SUBSCRIBE,
    /**
      * 基金赎回交易。
       */
    REDEEM,
    /**
      * 基金快速赎回交易。
       */
    FAST_REDEEM,
    /**
      * 基金产品转换交易。
       */
    PRODUCT_SWITCH,
    /**
      * 基金收益结算交易。
       */
    INCOME_SETTLE,
    /**
      * 基金份额冻结操作。
       */
    FREEZE,
    /**
      * 基金份额解冻操作。
       */
    UNFREEZE
}
