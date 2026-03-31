package cn.openaipay.application.loanaccount.service;

import cn.openaipay.application.loanaccount.command.CreateLoanAccountCommand;
import cn.openaipay.application.loanaccount.command.LoanTccCancelCommand;
import cn.openaipay.application.loanaccount.command.LoanTccConfirmCommand;
import cn.openaipay.application.loanaccount.command.LoanTccTryCommand;
import cn.openaipay.application.loanaccount.dto.LoanAccountDTO;
import cn.openaipay.application.loanaccount.dto.LoanTccBranchDTO;
import org.joda.money.Money;

/**
 * 借贷账户应用服务接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public interface LoanAccountService {

    /**
     * 创建借款信息。
     */
    String createLoanAccount(CreateLoanAccountCommand command);

    /**
     * 获取借款信息。
     */
    LoanAccountDTO getLoanAccount(String accountNo);

    /**
     * 按用户ID获取借款信息。
     */
    LoanAccountDTO getLoanAccountByUserId(Long userId);

    /**
     * 按用户ID获取或借款信息。
     */
    LoanAccountDTO getOrCreateLoanAccountByUserId(Long userId, Money totalLimit, Integer repayDayOfMonth);

    /**
     * 处理TCCTRY信息。
     */
    LoanTccBranchDTO tccTry(LoanTccTryCommand command);

    /**
     * 处理TCC信息。
     */
    LoanTccBranchDTO tccConfirm(LoanTccConfirmCommand command);

    /**
     * 处理TCC信息。
     */
    LoanTccBranchDTO tccCancel(LoanTccCancelCommand command);

}
