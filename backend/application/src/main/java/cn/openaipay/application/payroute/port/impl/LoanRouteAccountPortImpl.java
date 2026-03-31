package cn.openaipay.application.payroute.port.impl;

import cn.openaipay.application.agreement.facade.AgreementFacade;
import cn.openaipay.application.auth.exception.ForbiddenException;
import cn.openaipay.application.loanaccount.command.LoanTccCancelCommand;
import cn.openaipay.application.loanaccount.command.LoanTccConfirmCommand;
import cn.openaipay.application.loanaccount.command.LoanTccTryCommand;
import cn.openaipay.application.loanaccount.dto.LoanAccountDTO;
import cn.openaipay.application.loanaccount.service.LoanAccountService;
import cn.openaipay.application.payroute.port.LoanRouteAccountPort;
import java.util.NoSuchElementException;
import org.joda.money.Money;
import org.springframework.stereotype.Component;

/**
 * 借款账户路由能力端口实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
@Component
public class LoanRouteAccountPortImpl implements LoanRouteAccountPort {

    /** 协议门面。 */
    private final AgreementFacade agreementFacade;
    /** 借款账户应用服务。 */
    private final LoanAccountService loanAccountService;

    public LoanRouteAccountPortImpl(AgreementFacade agreementFacade,
                                    LoanAccountService loanAccountService) {
        this.agreementFacade = agreementFacade;
        this.loanAccountService = loanAccountService;
    }

    /**
     * 按用户ID解析账户单号。
     */
    @Override
    public String getAccountNoByUserId(Long userId) {
        validateUserId(userId);
        if (!agreementFacade.isAiLoanOpened(userId)) {
            throw new ForbiddenException("爱借服务未开通，请先完成开通协议");
        }
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
    public void tccTry(String xid,
                       String branchId,
                       String accountNo,
                       String operationType,
                       String assetCategory,
                       Money amount,
                       String businessNo) {
        loanAccountService.tccTry(new LoanTccTryCommand(
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
    public void tccConfirm(String xid, String branchId) {
        loanAccountService.tccConfirm(new LoanTccConfirmCommand(xid, branchId));
    }

    /**
     * 处理TCC信息。
     */
    @Override
    public void tccCancel(String xid,
                          String branchId,
                          String accountNo,
                          String operationType,
                          String assetCategory,
                          Money amount,
                          String businessNo) {
        loanAccountService.tccCancel(new LoanTccCancelCommand(
                xid,
                branchId,
                accountNo,
                operationType,
                assetCategory,
                amount,
                businessNo
        ));
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
    }
}

