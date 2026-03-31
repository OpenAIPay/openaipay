package cn.openaipay.domain.fundaccount.model;

/**
 * 基金产品状态枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum FundProductStatus {
    /**
      * 已启用状态，可参与线上业务流程。
       */
    ACTIVE,
    /**
      * 暂停状态，暂不参与线上流程。
       */
    PAUSED,
    /**
      * 已关闭状态，后续交易能力不可用。
       */
    CLOSED
}
