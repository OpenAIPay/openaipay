package cn.openaipay.application.loanaccount.service.impl;

import cn.openaipay.application.creditaccount.command.CreateCreditAccountCommand;
import cn.openaipay.application.creditaccount.command.CreditTccCancelCommand;
import cn.openaipay.application.creditaccount.command.CreditTccConfirmCommand;
import cn.openaipay.application.creditaccount.command.CreditTccTryCommand;
import cn.openaipay.application.creditaccount.dto.CreditAccountDTO;
import cn.openaipay.application.creditaccount.dto.CreditTccBranchDTO;
import cn.openaipay.application.creditaccount.service.CreditAccountService;
import cn.openaipay.application.loanaccount.command.LoanTccCancelCommand;
import cn.openaipay.application.loanaccount.command.LoanTccConfirmCommand;
import cn.openaipay.application.loanaccount.command.LoanTccTryCommand;
import cn.openaipay.application.loanaccount.dto.LoanAccountLedgerBranchDTO;
import cn.openaipay.application.loanaccount.dto.LoanAccountLedgerDTO;
import cn.openaipay.application.loanaccount.service.LoanAccountLedgerService;
import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import org.joda.money.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基于现有信用账本实现的爱借账本适配器。
 *
 * 业务语义：作为临时适配层，将 Loan 侧能力抽象为独立端口，
 * 上层不再直接依赖 Credit 模块类型，便于后续彻底拆分爱借独立账本实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/28
 */
@Service
public class CreditBackedLoanAccountLedgerService implements LoanAccountLedgerService {

    /** 信用账本应用服务（当前底层实现）。 */
    private final CreditAccountService creditAccountService;

    public CreditBackedLoanAccountLedgerService(CreditAccountService creditAccountService) {
        this.creditAccountService = creditAccountService;
    }

    /**
     * 创建爱借账本账户。
     */
    @Override
    @Transactional
    public LoanAccountLedgerDTO createLoanAccount(Long userId, String accountNo, Money totalLimit, Integer repayDayOfMonth) {
        String createdAccountNo = creditAccountService.createCreditAccount(new CreateCreditAccountCommand(
                userId,
                accountNo,
                totalLimit,
                repayDayOfMonth
        ));
        return toLoanAccountLedgerDTO(creditAccountService.getCreditAccount(createdAccountNo));
    }

    /**
     * 查询爱借账本账户。
     */
    @Override
    @Transactional(readOnly = true)
    public LoanAccountLedgerDTO getLoanAccount(String accountNo) {
        return toLoanAccountLedgerDTO(creditAccountService.getCreditAccount(accountNo));
    }

    /**
     * 按用户查询爱借账本账户。
     */
    @Override
    @Transactional(readOnly = true)
    public LoanAccountLedgerDTO getLoanAccountByUserId(Long userId) {
        return toLoanAccountLedgerDTO(creditAccountService.getCreditAccountByUserId(userId, CreditAccountType.LOAN_ACCOUNT));
    }

    /**
     * 按用户查询或创建爱借账本账户。
     */
    @Override
    @Transactional
    public LoanAccountLedgerDTO getOrCreateLoanAccountByUserId(Long userId, Money totalLimit, Integer repayDayOfMonth) {
        return toLoanAccountLedgerDTO(creditAccountService.getOrCreateCreditAccountByUserId(
                userId,
                CreditAccountType.LOAN_ACCOUNT,
                totalLimit,
                repayDayOfMonth
        ));
    }

    /**
     * 爱借账本TCC Try。
     */
    @Override
    @Transactional
    public LoanAccountLedgerBranchDTO tccTry(LoanTccTryCommand command) {
        return toLoanAccountLedgerBranchDTO(creditAccountService.tccTry(new CreditTccTryCommand(
                command.xid(),
                command.branchId(),
                command.accountNo(),
                command.operationType(),
                command.assetCategory(),
                command.amount(),
                command.businessNo()
        )));
    }

    /**
     * 爱借账本TCC Confirm。
     */
    @Override
    @Transactional
    public LoanAccountLedgerBranchDTO tccConfirm(LoanTccConfirmCommand command) {
        return toLoanAccountLedgerBranchDTO(creditAccountService.tccConfirm(new CreditTccConfirmCommand(
                command.xid(),
                command.branchId()
        )));
    }

    /**
     * 爱借账本TCC Cancel。
     */
    @Override
    @Transactional
    public LoanAccountLedgerBranchDTO tccCancel(LoanTccCancelCommand command) {
        return toLoanAccountLedgerBranchDTO(creditAccountService.tccCancel(new CreditTccCancelCommand(
                command.xid(),
                command.branchId(),
                command.accountNo(),
                command.operationType(),
                command.assetCategory(),
                command.amount(),
                command.businessNo()
        )));
    }

    private LoanAccountLedgerDTO toLoanAccountLedgerDTO(CreditAccountDTO dto) {
        return new LoanAccountLedgerDTO(
                dto.accountNo(),
                dto.userId(),
                dto.totalLimit(),
                dto.availableLimit(),
                dto.repayDayOfMonth(),
                dto.accountStatus(),
                dto.payStatus()
        );
    }

    private LoanAccountLedgerBranchDTO toLoanAccountLedgerBranchDTO(CreditTccBranchDTO branch) {
        return new LoanAccountLedgerBranchDTO(
                branch.xid(),
                branch.branchId(),
                branch.branchStatus(),
                branch.message()
        );
    }
}
