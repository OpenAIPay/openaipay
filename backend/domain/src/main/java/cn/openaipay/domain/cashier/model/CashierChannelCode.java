package cn.openaipay.domain.cashier.model;

/**
 * 收银台渠道编码枚举，用于声明某个业务场景允许用户看到并选择的付款渠道。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public enum CashierChannelCode {
    /**
     * 账户余额渠道，适用于转账、还款等可以直接从主余额账户扣款的场景。
     */
    WALLET,
    /**
     * 爱存渠道，适用于爱付式理财余额可直接支付或还款的场景。
     */
    FUND,
    /**
     * 爱花渠道，适用于先消费后还款的信用支付场景。
     */
    AICREDIT,
    /**
     * 银行卡渠道，具体可用借记卡或信用卡再由银行卡策略决定。
     */
    BANK_CARD
}
