package cn.openaipay.domain.deliver.model;

/**
 * 定向类型枚举。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public enum DeliverTargetingType {
    /** 按用户标签定向。 */
    USER_TAG,
    /** 按人群分群定向。 */
    AUDIENCE_SEGMENT,
    /** 按渠道定向。 */
    CHANNEL,
    /** 按场景定向。 */
    SCENE,
    /** 按客户端定向。 */
    CLIENT,
    /** 按时间范围定向。 */
    TIME_RANGE
}
