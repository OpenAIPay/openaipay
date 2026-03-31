package cn.openaipay.domain.fundaccount.model;

/**
 * 基金收益日历状态枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum FundIncomeCalendarStatus {
    /**
      * 收益日历已规划，待发布。
       */
    PLANNED,
    /**
      * 收益已发布，待执行结算。
       */
    PUBLISHED,
    /**
      * 收益已完成结算入账。
       */
    SETTLED
}
