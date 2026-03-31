package cn.openaipay.domain.deliver.model;

/**
 * 投放排序类型枚举。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public enum DeliverSortType {
    /** 按优先级排序。 */
    PRIORITY,
    /** 按权重排序。 */
    WEIGHT,
    /** 按人工顺序排序。 */
    MANUAL
}
