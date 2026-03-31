package cn.openaipay.application.loantrade.service;

import cn.openaipay.application.loanaccount.command.LoanTccCancelCommand;
import cn.openaipay.application.loanaccount.command.LoanTccConfirmCommand;
import cn.openaipay.application.loanaccount.command.LoanTccTryCommand;
import cn.openaipay.application.loanaccount.dto.LoanTccBranchDTO;

/**
 * 爱借交易应用服务接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
public interface LoanTradeService {

    /**
     * 处理 TCC TRY。
     */
    LoanTccBranchDTO tccTry(LoanTccTryCommand command);

    /**
     * 处理 TCC CONFIRM。
     */
    LoanTccBranchDTO tccConfirm(LoanTccConfirmCommand command);

    /**
     * 处理 TCC CANCEL。
     */
    LoanTccBranchDTO tccCancel(LoanTccCancelCommand command);
}
