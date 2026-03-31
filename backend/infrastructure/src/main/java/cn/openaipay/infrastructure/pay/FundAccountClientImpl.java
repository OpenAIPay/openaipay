package cn.openaipay.infrastructure.pay;

import cn.openaipay.application.fundaccount.facade.FundAccountFacade;
import cn.openaipay.application.fundaccount.facade.FundFreezeResult;
import cn.openaipay.domain.pay.client.FundAccountClient;
import cn.openaipay.domain.pay.client.PayFundFreezeResultSnapshot;
import org.joda.money.Money;
import org.springframework.stereotype.Component;

/**
 * FundAccountClientImpl 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class FundAccountClientImpl implements FundAccountClient {

    /** 资金信息 */
    private final FundAccountFacade fundAccountFacade;

    public FundAccountClientImpl(FundAccountFacade fundAccountFacade) {
        this.fundAccountFacade = fundAccountFacade;
    }

    /**
     * 解析基金编码。
     */
    @Override
    public String resolveFundCode(Long userId, String preferredFundCode) {
        return fundAccountFacade.resolveFundCode(userId, preferredFundCode);
    }

    /**
     * 处理份额用于支付信息。
     */
    @Override
    public PayFundFreezeResultSnapshot freezeShareForPay(String fundTradeOrderNo,
                                                         Long userId,
                                                         String preferredFundCode,
                                                         Money amount,
                                                         String businessNo) {
        FundFreezeResult result = fundAccountFacade.freezeShareForPay(
                fundTradeOrderNo,
                userId,
                preferredFundCode,
                amount,
                businessNo
        );
        return new PayFundFreezeResultSnapshot(result.fundCode(), result.share(), result.nav());
    }

    /**
     * 确认份额用于支付信息。
     */
    @Override
    public void confirmFrozenShareForPay(Long userId, String fundTradeOrderNo) {
        fundAccountFacade.confirmFrozenShareForPay(userId, fundTradeOrderNo);
    }

    /**
     * 处理份额用于支付信息。
     */
    @Override
    public void compensateFrozenShareForPay(Long userId,
                                            String fundTradeOrderNo,
                                            String preferredFundCode,
                                            String businessNo) {
        fundAccountFacade.compensateFrozenShareForPay(userId, fundTradeOrderNo, preferredFundCode, businessNo);
    }
}
