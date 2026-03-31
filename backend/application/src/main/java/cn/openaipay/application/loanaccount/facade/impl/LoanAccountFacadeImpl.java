package cn.openaipay.application.loanaccount.facade.impl;

import cn.openaipay.application.agreement.facade.AgreementFacade;
import cn.openaipay.application.auth.exception.ForbiddenException;
import cn.openaipay.application.loanaccount.command.CreateLoanAccountCommand;
import cn.openaipay.application.loanaccount.command.LoanTccCancelCommand;
import cn.openaipay.application.loanaccount.command.LoanTccConfirmCommand;
import cn.openaipay.application.loanaccount.command.LoanTccTryCommand;
import cn.openaipay.application.loanaccount.dto.LoanAccountDTO;
import cn.openaipay.application.loanaccount.dto.LoanTccBranchDTO;
import cn.openaipay.application.loanaccount.facade.LoanAccountFacade;
import cn.openaipay.application.loanaccount.service.LoanAccountService;
import java.util.NoSuchElementException;
import org.joda.money.Money;
import org.springframework.stereotype.Service;

/**
 * 借贷账户门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Service
public class LoanAccountFacadeImpl implements LoanAccountFacade {

    /** 借款信息 */
    private final LoanAccountService loanAccountService;
    /** 协议信息 */
    private final AgreementFacade agreementFacade;

    public LoanAccountFacadeImpl(LoanAccountService loanAccountService,
                                 AgreementFacade agreementFacade) {
        this.loanAccountService = loanAccountService;
        this.agreementFacade = agreementFacade;
    }

    /**
     * 创建借款账户信息。
     */
    @Override
    public String createLoanAccount(CreateLoanAccountCommand command) {
        return loanAccountService.createLoanAccount(command);
    }

    /**
     * 按用户ID获取借款信息。
     */
    @Override
    public LoanAccountDTO getLoanAccountByUserId(Long userId) {
        ensureAiLoanOpened(userId);
        return loanAccountService.getLoanAccountByUserId(userId);
    }

    /**
     * 获取借款账户信息。
     */
    @Override
    public LoanAccountDTO getLoanAccount(String accountNo) {
        return loanAccountService.getLoanAccount(accountNo);
    }

    /**
     * 按用户ID获取单号。
     */
    @Override
    public String getAccountNoByUserId(Long userId) {
        ensureAiLoanOpened(userId);
        LoanAccountDTO account = loanAccountService.getLoanAccountByUserId(userId);
        if (account == null || account.accountNo() == null || account.accountNo().isBlank()) {
            throw new NoSuchElementException("loan account not found for userId=" + userId);
        }
        return account.accountNo();
    }

    /**
     * 处理TCCTRY信息。
     */
    @Override
    public LoanTccBranchDTO tccTry(String xid,
                                   String branchId,
                                   String accountNo,
                                   String operationType,
                                   String assetCategory,
                                   Money amount,
                                   String businessNo) {
        return loanAccountService.tccTry(new LoanTccTryCommand(
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
     * 处理TCC信息。
     */
    @Override
    public LoanTccBranchDTO tccConfirm(String xid, String branchId) {
        return loanAccountService.tccConfirm(new LoanTccConfirmCommand(xid, branchId));
    }

    /**
     * 处理TCC信息。
     */
    @Override
    public LoanTccBranchDTO tccCancel(String xid,
                                      String branchId,
                                      String accountNo,
                                      String operationType,
                                      String assetCategory,
                                      Money amount,
                                      String businessNo) {
        return loanAccountService.tccCancel(new LoanTccCancelCommand(
                xid,
                branchId,
                accountNo,
                operationType,
                assetCategory,
                amount,
                businessNo
        ));
    }

    private void ensureAiLoanOpened(Long userId) {
        if (!agreementFacade.isAiLoanOpened(userId)) {
            throw new ForbiddenException("爱借服务未开通，请先完成开通协议");
        }
    }
}
