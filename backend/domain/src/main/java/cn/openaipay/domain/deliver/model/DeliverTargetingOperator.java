package cn.openaipay.domain.deliver.model;

/**
 * 定向操作符枚举。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public enum DeliverTargetingOperator {
    /** 包含。 */
    IN,
    /** 不包含。 */
    NOT_IN,
    /** 等于。 */
    EQUALS,
    /** 不等于。 */
    NOT_EQUALS
}
