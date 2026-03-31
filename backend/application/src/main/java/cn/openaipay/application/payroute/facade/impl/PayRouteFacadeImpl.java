package cn.openaipay.application.payroute.facade.impl;

import cn.openaipay.application.payroute.dto.PayRouteDTO;
import cn.openaipay.application.payroute.facade.PayRouteFacade;
import cn.openaipay.application.payroute.service.PayRouteService;
import org.joda.money.Money;
import org.springframework.stereotype.Service;

/**
 * 支付产品路由门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Service
public class PayRouteFacadeImpl implements PayRouteFacade {

    /** 支付信息 */
    private final PayRouteService payRouteService;

    public PayRouteFacadeImpl(PayRouteService payRouteService) {
        this.payRouteService = payRouteService;
    }

    /**
     * 处理信用用于支付信息。
     */
    @Override
    public PayRouteDTO routeCreditForPay(String businessSceneCode,
                                         String paymentMethod,
                                         Long payerUserId,
                                         Long payeeUserId) {
        return payRouteService.routeCreditForPay(
                businessSceneCode,
                paymentMethod,
                payerUserId,
                payeeUserId
        );
    }

    /**
     * 处理信用用于交易信息。
     */
    @Override
    public PayRouteDTO routeCreditForTrade(String businessDomainCode,
                                           String businessSceneCode,
                                           String paymentMethod,
                                           Long payerUserId,
                                           Long payeeUserId) {
        return payRouteService.routeCreditForTrade(
                businessDomainCode,
                businessSceneCode,
                paymentMethod,
                payerUserId,
                payeeUserId
        );
    }

    /**
     * 处理TCCTRY信用信息。
     */
    @Override
    public void tccTryCredit(String xid,
                             String branchId,
                             String accountNo,
                             String operationType,
                             String assetCategory,
                             Money amount,
                             String businessNo) {
        payRouteService.tccTryCredit(
                xid,
                branchId,
                accountNo,
                operationType,
                assetCategory,
                amount,
                businessNo
        );
    }

    /**
     * 处理TCC信用信息。
     */
    @Override
    public void tccConfirmCredit(String xid, String branchId, String accountNo) {
        payRouteService.tccConfirmCredit(xid, branchId, accountNo);
    }

    /**
     * 处理TCC信用信息。
     */
    @Override
    public void tccCancelCredit(String xid,
                                String branchId,
                                String accountNo,
                                String operationType,
                                String assetCategory,
                                Money amount,
                                String businessNo) {
        payRouteService.tccCancelCredit(
                xid,
                branchId,
                accountNo,
                operationType,
                assetCategory,
                amount,
                businessNo
        );
    }
}
