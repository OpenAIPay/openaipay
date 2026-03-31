package cn.openaipay.domain.settle.service;

/**
 * 结算计划状态。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public enum SettlePlanStatus {
    /** 当前交易无需额外结算动作。 */
    NO_ACTION,
    /** 需要执行结算动作。 */
    EXECUTE,
    /** 当前数据不足以完成结算，需要进入对账待确认。 */
    RECON_PENDING
}
