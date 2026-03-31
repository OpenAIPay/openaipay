package cn.openaipay.domain.agreement.model;

/**
 * 协议签约状态。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public enum AgreementSignStatus {

    /** 待处理。 */
    PENDING,
    /** 已成功。 */
    SUCCEEDED,
    /** 已失败。 */
    FAILED
}
