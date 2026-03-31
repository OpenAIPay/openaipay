package cn.openaipay.domain.pay.client;

import org.joda.money.Money;

/**
 * FundAccountClient 客户端
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface FundAccountClient {
    /**
     * 解析基金编码。
     */
    String resolveFundCode(Long userId, String preferredFundCode);

    PayFundFreezeResultSnapshot freezeShareForPay(String fundTradeOrderNo,
                                                  Long userId,
                                                  String preferredFundCode,
                                                  Money amount,
                                                  String businessNo);

    /**
     * 确认份额用于支付信息。
     */
    void confirmFrozenShareForPay(Long userId, String fundTradeOrderNo);

    void compensateFrozenShareForPay(Long userId,
                                     String fundTradeOrderNo,
                                     String preferredFundCode,
                                     String businessNo);
}
