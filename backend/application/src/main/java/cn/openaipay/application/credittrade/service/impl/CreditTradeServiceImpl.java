package cn.openaipay.application.credittrade.service.impl;

import cn.openaipay.application.creditaccount.dto.CreditTccBranchDTO;
import cn.openaipay.application.creditaccount.command.CreditTccCancelCommand;
import cn.openaipay.application.creditaccount.command.CreditTccConfirmCommand;
import cn.openaipay.application.creditaccount.command.CreditTccTryCommand;
import cn.openaipay.application.creditaccount.service.CreditAccountService;
import cn.openaipay.application.credittrade.service.CreditTradeService;
import cn.openaipay.application.trade.command.CreatePayTradeCommand;
import cn.openaipay.application.trade.dto.TradeOrderDTO;
import cn.openaipay.application.trade.facade.TradeFacade;
import org.joda.money.Money;
import org.springframework.stereotype.Service;

/**
 * 爱花交易应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
@Service
public class CreditTradeServiceImpl implements CreditTradeService {

    /** 统一交易门面。 */
    private final TradeFacade tradeFacade;
    /** 爱花账户应用服务。 */
    private final CreditAccountService creditAccountService;

    public CreditTradeServiceImpl(TradeFacade tradeFacade,
                                  CreditAccountService creditAccountService) {
        this.tradeFacade = tradeFacade;
        this.creditAccountService = creditAccountService;
    }

    /**
     * 同步提交到统一交易主链路。
     */
    @Override
    public TradeOrderDTO submitToTrade(CreatePayTradeCommand command) {
        return tradeFacade.pay(command);
    }

    /**
     * 爱花账户 TCC TRY。
     */
    @Override
    public CreditTccBranchDTO tccTry(String xid,
                                     String branchId,
                                     String accountNo,
                                     String operationType,
                                     String assetCategory,
                                     Money amount,
                                     String businessNo) {
        return creditAccountService.tccTry(new CreditTccTryCommand(
                xid,
                branchId,
                accountNo,
                operationType,
                assetCategory,
                amount,
                businessNo
        ));
    }

    /**
     * 爱花账户 TCC CONFIRM。
     */
    @Override
    public CreditTccBranchDTO tccConfirm(String xid, String branchId) {
        return creditAccountService.tccConfirm(new CreditTccConfirmCommand(xid, branchId));
    }

    /**
     * 爱花账户 TCC CANCEL。
     */
    @Override
    public CreditTccBranchDTO tccCancel(String xid,
                                        String branchId,
                                        String accountNo,
                                        String operationType,
                                        String assetCategory,
                                        Money amount,
                                        String businessNo) {
        return creditAccountService.tccCancel(new CreditTccCancelCommand(
                xid,
                branchId,
                accountNo,
                operationType,
                assetCategory,
                amount,
                businessNo
        ));
    }
}
