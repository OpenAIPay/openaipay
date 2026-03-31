package cn.openaipay.domain.pay.model;

import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import org.joda.money.Money;

import java.time.LocalDateTime;

/**
 * 信用账户资金明细模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public class PayCreditAccountFundDetail extends PayFundDetailSummary {

    /** 业务单号 */
    private final String accountNo;
    /** 信用类型 */
    private final CreditAccountType creditAccountType;
    /** 信用产品编码 */
    private final String creditProductCode;

    public PayCreditAccountFundDetail(Long id,
                                      String payOrderNo,
                                      PayFundDetailOwner detailOwner,
                                      Money amount,
                                      Money cumulativeRefundAmount,
                                      String accountNo,
                                      CreditAccountType creditAccountType,
                                      String creditProductCode,
                                      LocalDateTime createdAt,
                                      LocalDateTime updatedAt) {
        super(
                id,
                payOrderNo,
                PayFundDetailTool.CREDIT,
                detailOwner,
                amount,
                cumulativeRefundAmount,
                createdAt,
                updatedAt
        );
        this.accountNo = normalizeRequired(accountNo, "accountNo");
        this.creditAccountType = creditAccountType == null
                ? CreditAccountType.fromAccountNo(this.accountNo)
                : creditAccountType;
        this.creditProductCode = normalizeRequired(creditProductCode, "creditProductCode");
    }

    /**
     * 创建业务数据。
     */
    public static PayCreditAccountFundDetail create(String payOrderNo,
                                                    PayFundDetailOwner detailOwner,
                                                    Money amount,
                                                    String accountNo,
                                                    CreditAccountType creditAccountType,
                                                    String creditProductCode,
                                                    LocalDateTime now) {
        return new PayCreditAccountFundDetail(
                null,
                payOrderNo,
                detailOwner,
                amount,
                null,
                accountNo,
                creditAccountType,
                creditProductCode,
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

    /**
     * 获取信用账户类型信息。
     */
    public CreditAccountType getCreditAccountType() {
        return creditAccountType;
    }

    /**
     * 获取信用编码。
     */
    public String getCreditProductCode() {
        return creditProductCode;
    }
}
