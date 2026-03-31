package cn.openaipay.domain.payroute.service;

/**
 * 支付产品路由领域服务。
 *
 * 负责根据业务场景、支付方式和参与人，决定当前链路应落到爱花还是爱借模块。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface PayRouteDomainService {

    /**
     * 为支付域的授信扣款链路解析路由结果。
     */
    CreditPayRoutePlan routeCreditForPay(String businessSceneCode,
                                         String paymentMethod,
                                         Long payerUserId,
                                         Long payeeUserId);

    /**
     * 为交易域的信用扩展单链路解析路由结果。
     *
     * 当当前交易并非信用产品链路时，返回 {@code null}。
     */
    CreditPayRoutePlan routeCreditForTrade(String businessDomainCode,
                                           String businessSceneCode,
                                           String paymentMethod,
                                           Long payerUserId,
                                           Long payeeUserId);
}
