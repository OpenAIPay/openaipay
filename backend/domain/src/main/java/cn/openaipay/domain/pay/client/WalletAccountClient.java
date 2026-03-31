package cn.openaipay.domain.pay.client;

import org.joda.money.Money;

/**
 * WalletAccountClient 客户端
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface WalletAccountClient {
    /**
     * 处理TCCTRY信息。
     */
    void tccTry(String xid, String branchId, Long userId, String operationType, String freezeType, Money amount, String businessNo);

    /**
     * 处理TCC信息。
     */
    void tccConfirm(String xid, String branchId);

    /**
     * 处理TCC信息。
     */
    void tccCancel(String xid, String branchId, Long userId, String operationType, String freezeType, Money amount, String businessNo);
}
