package cn.openaipay.application.loanaccount.facade;

import cn.openaipay.application.loanaccount.command.CreateLoanAccountCommand;
import cn.openaipay.application.loanaccount.dto.LoanAccountDTO;
import cn.openaipay.application.loanaccount.dto.LoanTccBranchDTO;
import org.joda.money.Money;

/**
 * 借贷账户门面接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public interface LoanAccountFacade {

    /**
     * 创建借款信息。
     */
    String createLoanAccount(CreateLoanAccountCommand command);

    /**
     * 按用户ID获取借款信息。
     */
    LoanAccountDTO getLoanAccountByUserId(Long userId);

    /**
     * 获取借款信息。
     */
    LoanAccountDTO getLoanAccount(String accountNo);

    /**
     * 按用户ID获取单号。
     */
    String getAccountNoByUserId(Long userId);

    LoanTccBranchDTO tccTry(String xid,
                            String branchId,
                            String accountNo,
                            String operationType,
                            String assetCategory,
                            Money amount,
                            String businessNo);

    /**
     * 处理TCC信息。
     */
    LoanTccBranchDTO tccConfirm(String xid, String branchId);

    LoanTccBranchDTO tccCancel(String xid,
                               String branchId,
                               String accountNo,
                               String operationType,
                               String assetCategory,
                               Money amount,
                               String businessNo);
}
