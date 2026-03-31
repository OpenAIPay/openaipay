package cn.openaipay.domain.accounting.model;

/**
 * 会计事件处理状态。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public enum AccountingEventStatus {
    NEW,
    PROCESSING,
    POSTED,
    FAILED,
    REVERSED
}
