package cn.openaipay.domain.pay.model;

import org.joda.money.Money;

import java.time.LocalDateTime;

/**
 * 基金账户资金明细模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public class PayFundAccountFundDetail extends PayFundDetailSummary {

    /** 资金编码 */
    private final String fundCode;
    /** 资金产品编码 */
    private final String fundProductCode;
    /** 身份信息 */
    private final String accountIdentity;

    public PayFundAccountFundDetail(Long id,
                                    String payOrderNo,
                                    PayFundDetailOwner detailOwner,
                                    Money amount,
                                    Money cumulativeRefundAmount,
                                    String fundCode,
                                    String fundProductCode,
                                    String accountIdentity,
                                    LocalDateTime createdAt,
                                    LocalDateTime updatedAt) {
        super(
                id,
                payOrderNo,
                PayFundDetailTool.FUND,
                detailOwner,
                amount,
                cumulativeRefundAmount,
                createdAt,
                updatedAt
        );
        this.fundCode = normalizeRequired(fundCode, "fundCode");
        this.fundProductCode = normalizeRequired(fundProductCode, "fundProductCode");
        this.accountIdentity = normalizeOptional(accountIdentity);
    }

    /**
     * 创建业务数据。
     */
    public static PayFundAccountFundDetail create(String payOrderNo,
                                                  PayFundDetailOwner detailOwner,
                                                  Money amount,
                                                  String fundCode,
                                                  String fundProductCode,
                                                  String accountIdentity,
                                                  LocalDateTime now) {
        return new PayFundAccountFundDetail(
                null,
                payOrderNo,
                detailOwner,
                amount,
                null,
                fundCode,
                fundProductCode,
                accountIdentity,
                now,
                now
        );
    }

    /**
     * 获取基金编码。
     */
    public String getFundCode() {
        return fundCode;
    }

    /**
     * 获取基金编码。
     */
    public String getFundProductCode() {
        return fundProductCode;
    }

    /**
     * 获取业务数据。
     */
    public String getAccountIdentity() {
        return accountIdentity;
    }
}
