package cn.openaipay.domain.accounting.model;

/**
 * 会计科目编码常量。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public final class AccountingSubjectCodes {

    private AccountingSubjectCodes() {
    }

    /** 备付金银行存款。 */
    public static final String ASSET_RESERVE_BANK_DEPOSIT = "100101";
    /** 入金渠道待清算资产。 */
    public static final String ASSET_INBOUND_CHANNEL_CLEARING = "100201";
    /** 爱花应收。 */
    public static final String ASSET_AICREDIT_RECEIVABLE = "100301";
    /** 爱借应收。 */
    public static final String ASSET_AILOAN_RECEIVABLE = "100302";

    /** 用户钱包可用余额负债。 */
    public static final String LIABILITY_USER_WALLET_AVAILABLE = "200101";
    /** 待结算资金负债。 */
    public static final String LIABILITY_PENDING_SETTLEMENT = "200201";
    /** 用户提现处理中负债。 */
    public static final String LIABILITY_WITHDRAW_PENDING = "200301";
    /** 爱存份额负债。 */
    public static final String LIABILITY_AICASH_SHARE = "200401";

    /** 支付手续费收入。 */
    public static final String INCOME_PAYMENT_SERVICE_FEE = "500101";
    /** 提现手续费收入。 */
    public static final String INCOME_WITHDRAW_SERVICE_FEE = "500102";

    /** 入金通道手续费成本。 */
    public static final String EXPENSE_INBOUND_CHANNEL_FEE = "600101";

    /** 待映射会计科目。 */
    public static final String MEMO_UNMAPPED = "900101";
}
