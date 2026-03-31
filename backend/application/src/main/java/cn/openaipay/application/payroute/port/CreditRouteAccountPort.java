package cn.openaipay.application.payroute.port;

import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import org.joda.money.Money;

/**
 * 信用账户路由能力端口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
public interface CreditRouteAccountPort {

    /**
     * 按用户ID和账户类型解析账户单号。
     */
    String getAccountNoByUserId(Long userId, CreditAccountType accountType);

    /**
     * 处理TCCTRY信息。
     */
    void tccTry(String xid,
                String branchId,
                String accountNo,
                String operationType,
                String assetCategory,
                Money amount,
                String businessNo);

    /**
     * 处理TCC信息。
     */
    void tccConfirm(String xid, String branchId);

    /**
     * 处理TCC信息。
     */
    void tccCancel(String xid,
                   String branchId,
                   String accountNo,
                   String operationType,
                   String assetCategory,
                   Money amount,
                   String businessNo);
}

