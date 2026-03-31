package cn.openaipay.application.credittrade.service;

import cn.openaipay.application.creditaccount.dto.CreditTccBranchDTO;
import cn.openaipay.application.trade.command.CreatePayTradeCommand;
import cn.openaipay.application.trade.dto.TradeOrderDTO;
import org.joda.money.Money;

/**
 * 爱花交易应用服务接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
public interface CreditTradeService {

    /**
     * 同步提交到统一交易主链路。
     */
    TradeOrderDTO submitToTrade(CreatePayTradeCommand command);

    /**
     * 爱花账户 TCC TRY。
     */
    CreditTccBranchDTO tccTry(String xid,
                              String branchId,
                              String accountNo,
                              String operationType,
                              String assetCategory,
                              Money amount,
                              String businessNo);

    /**
     * 爱花账户 TCC CONFIRM。
     */
    CreditTccBranchDTO tccConfirm(String xid, String branchId);

    /**
     * 爱花账户 TCC CANCEL。
     */
    CreditTccBranchDTO tccCancel(String xid,
                                 String branchId,
                                 String accountNo,
                                 String operationType,
                                 String assetCategory,
                                 Money amount,
                                 String businessNo);
}
