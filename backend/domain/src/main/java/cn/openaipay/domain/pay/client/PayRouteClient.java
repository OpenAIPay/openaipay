package cn.openaipay.domain.pay.client;

import org.joda.money.Money;

/**
 * PayRouteClient 客户端
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface PayRouteClient {
    /**
     * 处理信用用于支付信息。
     */
    PayCreditRouteSnapshot routeCreditForPay(String businessSceneCode, String paymentMethod, Long payerUserId, Long payeeUserId);

    /**
     * 处理TCCTRY信用信息。
     */
    void tccTryCredit(String xid, String branchId, String accountNo, String operationType, String assetCategory, Money amount, String businessNo);

    /**
     * 处理TCC信用信息。
     */
    void tccConfirmCredit(String xid, String branchId, String accountNo);

    /**
     * 处理TCC信用信息。
     */
    void tccCancelCredit(String xid, String branchId, String accountNo, String operationType, String assetCategory, Money amount, String businessNo);
}
