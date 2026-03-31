package cn.openaipay.application.payroute.facade;

import cn.openaipay.application.payroute.dto.PayRouteDTO;
import org.joda.money.Money;

/**
 * 支付产品路由门面接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface PayRouteFacade {

    /**
     * 为支付域授信扣款解析路由结果。
     */
    PayRouteDTO routeCreditForPay(String businessSceneCode,
                                  String paymentMethod,
                                  Long payerUserId,
                                  Long payeeUserId);

    /**
     * 为交易域信用扩展单解析路由结果。
     *
     * 当当前交易并非信用产品链路时，返回 {@code null}。
     */
    PayRouteDTO routeCreditForTrade(String businessDomainCode,
                                    String businessSceneCode,
                                    String paymentMethod,
                                    Long payerUserId,
                                    Long payeeUserId);

    /**
     * 对信用账户执行 TCC Try。
     */
    void tccTryCredit(String xid,
                      String branchId,
                      String accountNo,
                      String operationType,
                      String assetCategory,
                      Money amount,
                      String businessNo);

    /**
     * 处理TCC信用信息。
     */
    void tccConfirmCredit(String xid, String branchId, String accountNo);

    /**
     * 对信用账户执行 TCC Cancel。
     */
    void tccCancelCredit(String xid,
                         String branchId,
                         String accountNo,
                         String operationType,
                         String assetCategory,
                         Money amount,
                         String businessNo);
}
