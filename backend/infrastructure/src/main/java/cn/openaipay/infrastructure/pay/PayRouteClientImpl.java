package cn.openaipay.infrastructure.pay;

import cn.openaipay.application.payroute.dto.PayRouteDTO;
import cn.openaipay.application.payroute.facade.PayRouteFacade;
import cn.openaipay.domain.pay.client.PayCreditRouteSnapshot;
import cn.openaipay.domain.pay.client.PayRouteClient;
import org.joda.money.Money;
import org.springframework.stereotype.Component;

/**
 * PayRouteClientImpl 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class PayRouteClientImpl implements PayRouteClient {

    /** 支付信息 */
    private final PayRouteFacade payRouteFacade;

    public PayRouteClientImpl(PayRouteFacade payRouteFacade) {
        this.payRouteFacade = payRouteFacade;
    }

    /**
     * 处理信用用于支付信息。
     */
    @Override
    public PayCreditRouteSnapshot routeCreditForPay(String businessSceneCode, String paymentMethod, Long payerUserId, Long payeeUserId) {
        PayRouteDTO payRoute = payRouteFacade.routeCreditForPay(businessSceneCode, paymentMethod, payerUserId, payeeUserId);
        if (payRoute == null) {
            return null;
        }
        return new PayCreditRouteSnapshot(
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

    /**
     * 处理TCCTRY信用信息。
     */
    @Override
    public void tccTryCredit(String xid, String branchId, String accountNo, String operationType, String assetCategory, Money amount, String businessNo) {
        payRouteFacade.tccTryCredit(xid, branchId, accountNo, operationType, assetCategory, amount, businessNo);
    }

    /**
     * 处理TCC信用信息。
     */
    @Override
    public void tccConfirmCredit(String xid, String branchId, String accountNo) {
        payRouteFacade.tccConfirmCredit(xid, branchId, accountNo);
    }

    /**
     * 处理TCC信用信息。
     */
    @Override
    public void tccCancelCredit(String xid, String branchId, String accountNo, String operationType, String assetCategory, Money amount, String businessNo) {
        payRouteFacade.tccCancelCredit(xid, branchId, accountNo, operationType, assetCategory, amount, businessNo);
    }
}
