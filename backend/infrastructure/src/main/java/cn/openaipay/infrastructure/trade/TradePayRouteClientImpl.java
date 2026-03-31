package cn.openaipay.infrastructure.trade;

import cn.openaipay.application.payroute.dto.PayRouteDTO;
import cn.openaipay.application.payroute.facade.PayRouteFacade;
import cn.openaipay.domain.trade.client.TradeCreditRouteSnapshot;
import cn.openaipay.domain.trade.client.TradePayRouteClient;
import org.springframework.stereotype.Component;

/**
 * TradePayRouteClientImpl 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class TradePayRouteClientImpl implements TradePayRouteClient {

    /** 支付信息 */
    private final PayRouteFacade payRouteFacade;

    public TradePayRouteClientImpl(PayRouteFacade payRouteFacade) {
        this.payRouteFacade = payRouteFacade;
    }

    /**
     * 处理信用用于交易信息。
     */
    @Override
    public TradeCreditRouteSnapshot routeCreditForTrade(String businessDomainCode,
                                                        String businessSceneCode,
                                                        String paymentMethod,
                                                        Long payerUserId,
                                                        Long payeeUserId) {
        PayRouteDTO payRoute = payRouteFacade.routeCreditForTrade(
                businessDomainCode,
                businessSceneCode,
                paymentMethod,
                payerUserId,
                payeeUserId
        );
        if (payRoute == null) {
            return null;
        }
        return new TradeCreditRouteSnapshot(
                payRoute.businessDomainCode(),
                payRoute.productCode(),
                payRoute.accountModuleCode(),
                payRoute.accountTypeCode(),
                payRoute.accountOwnerUserId(),
                payRoute.accountNo(),
                payRoute.operationType(),
                payRoute.assetCategory()
        );
    }
}
