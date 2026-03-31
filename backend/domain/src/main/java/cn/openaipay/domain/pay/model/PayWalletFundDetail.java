package cn.openaipay.domain.pay.model;

import org.joda.money.Money;

import java.time.LocalDateTime;

/**
 * 钱包资金明细模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class PayWalletFundDetail extends PayFundDetailSummary {

    /** 账户号 */
    private final String accountNo;

    public PayWalletFundDetail(Long id,
                               String payOrderNo,
                               PayFundDetailOwner detailOwner,
                               Money amount,
                               Money cumulativeRefundAmount,
                               String accountNo,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt) {
        super(
                id,
                payOrderNo,
                PayFundDetailTool.WALLET,
                detailOwner,
                amount,
                cumulativeRefundAmount,
                createdAt,
                updatedAt
        );
        this.accountNo = normalizeRequired(accountNo, "accountNo");
    }

    /**
     * 创建业务数据。
     */
    public static PayWalletFundDetail create(String payOrderNo,
                                             PayFundDetailOwner detailOwner,
                                             Money amount,
                                             String accountNo,
                                             LocalDateTime now) {
        return new PayWalletFundDetail(
                null,
                payOrderNo,
                detailOwner,
                amount,
                null,
                accountNo,
                now,
                now
        );
    }

    /**
     * 获取账户NO信息。
     */
    public String getAccountNo() {
        return accountNo;
    }
}
