package cn.openaipay.domain.creditaccount.model;

import java.util.Locale;

/**
 * 爱花还款目标账单模式。
 *
 * 业务场景：APP 里的“还 3 月账单”和“提前还 4 月账单”都会走 APP_CREDIT_REPAY，
 * 如果不把目标账单模式单独建模，当前账单还款会误扣下期累计账单，导致结果页和总计账单金额错误。
 */
public enum CreditRepayBillMode {

    /** 当前已出账账单还款。 */
    CURRENT,

    /** 下期待出账账单预还款，仅匹配显式标记 repayBillMode=next 的交易。 */
    NEXT;

    /**
     * 返回 trade_order.metadata 中使用的标准化模式值。
     */
    public String metadataValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
