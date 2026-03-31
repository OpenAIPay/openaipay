package cn.openaipay.domain.audience.model;

/**
 * 人群规则操作符
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public enum AudienceRuleOperator {
    /** 等于 */
    EQ,
    /** 不等于 */
    NEQ,
    /** 属于 */
    IN,
    /** 不属于 */
    NOT_IN,
    /** 大于 */
    GT,
    /** 大于等于 */
    GTE,
    /** 小于 */
    LT,
    /** 小于等于 */
    LTE,
    /** 区间 */
    BETWEEN,
    /** 存在 */
    EXISTS,
    /** 不存在 */
    NOT_EXISTS,
    /** 包含 */
    CONTAINS
}
