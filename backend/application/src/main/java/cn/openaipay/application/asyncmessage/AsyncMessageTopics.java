package cn.openaipay.application.asyncmessage;

/**
 * 系统内可靠异步消息主题常量。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public final class AsyncMessageTopics {
    /** 支付信息 */
    public static final String PAY_EXECUTE_REQUESTED = "PAY_EXECUTE_REQUESTED";
    /** 支付信息 */
    public static final String PAY_RECON_REQUESTED = "PAY_RECON_REQUESTED";
    /** 支付事件信息 */
    public static final String PAY_ACCOUNTING_EVENT_REQUESTED = "PAY_ACCOUNTING_EVENT_REQUESTED";
    /** 支付结果信息 */
    public static final String PAY_RESULT_CHANGED = "PAY_RESULT_CHANGED";
    /** 结算事件信息 */
    public static final String SETTLE_ACCOUNTING_EVENT_REQUESTED = "SETTLE_ACCOUNTING_EVENT_REQUESTED";
    /** 用户注册赠送初始余额请求 */
    public static final String USER_REGISTER_INITIAL_WALLET_TOPUP_REQUESTED = "USER_REGISTER_INITIAL_WALLET_TOPUP_REQUESTED";

    private AsyncMessageTopics() {
    }
}
