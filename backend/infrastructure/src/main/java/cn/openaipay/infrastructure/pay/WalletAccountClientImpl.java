package cn.openaipay.infrastructure.pay;

import cn.openaipay.application.walletaccount.facade.WalletAccountFacade;
import cn.openaipay.domain.pay.client.WalletAccountClient;
import org.joda.money.Money;
import org.springframework.stereotype.Component;

/**
 * WalletAccountClientImpl 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class WalletAccountClientImpl implements WalletAccountClient {

    /** 钱包信息 */
    private final WalletAccountFacade walletAccountFacade;

    public WalletAccountClientImpl(WalletAccountFacade walletAccountFacade) {
        this.walletAccountFacade = walletAccountFacade;
    }

    /**
     * 处理TCCTRY信息。
     */
    @Override
    public void tccTry(String xid, String branchId, Long userId, String operationType, String freezeType, Money amount, String businessNo) {
        walletAccountFacade.tccTry(xid, branchId, userId, operationType, freezeType, amount, businessNo);
    }

    /**
     * 处理TCC信息。
     */
    @Override
    public void tccConfirm(String xid, String branchId) {
        walletAccountFacade.tccConfirm(xid, branchId);
    }

    /**
     * 处理TCC信息。
     */
    @Override
    public void tccCancel(String xid, String branchId, Long userId, String operationType, String freezeType, Money amount, String businessNo) {
        walletAccountFacade.tccCancel(xid, branchId, userId, operationType, freezeType, amount, businessNo);
    }
}
