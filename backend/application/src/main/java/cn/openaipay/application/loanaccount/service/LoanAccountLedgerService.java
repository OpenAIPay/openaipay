package cn.openaipay.application.loanaccount.service;

import cn.openaipay.application.loanaccount.command.LoanTccCancelCommand;
import cn.openaipay.application.loanaccount.command.LoanTccConfirmCommand;
import cn.openaipay.application.loanaccount.command.LoanTccTryCommand;
import cn.openaipay.application.loanaccount.dto.LoanAccountLedgerBranchDTO;
import cn.openaipay.application.loanaccount.dto.LoanAccountLedgerDTO;
import org.joda.money.Money;

/**
 * 爱借账本服务端口。
 *
 * 业务语义：对 LoanAccount 应用服务暴露“爱借账本能力”，
 * 屏蔽底层实现来源，避免直接依赖爱花模块类型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/28
 */
public interface LoanAccountLedgerService {

    /**
     * 创建爱借账本账户。
     */
    LoanAccountLedgerDTO createLoanAccount(Long userId, String accountNo, Money totalLimit, Integer repayDayOfMonth);

    /**
     * 查询爱借账本账户。
     */
    LoanAccountLedgerDTO getLoanAccount(String accountNo);

    /**
     * 按用户查询爱借账本账户。
     */
    LoanAccountLedgerDTO getLoanAccountByUserId(Long userId);

    /**
     * 按用户查询或创建爱借账本账户。
     */
    LoanAccountLedgerDTO getOrCreateLoanAccountByUserId(Long userId, Money totalLimit, Integer repayDayOfMonth);

    /**
     * 爱借账本TCC Try。
     */
    LoanAccountLedgerBranchDTO tccTry(LoanTccTryCommand command);

    /**
     * 爱借账本TCC Confirm。
     */
    LoanAccountLedgerBranchDTO tccConfirm(LoanTccConfirmCommand command);

    /**
     * 爱借账本TCC Cancel。
     */
    LoanAccountLedgerBranchDTO tccCancel(LoanTccCancelCommand command);
}
