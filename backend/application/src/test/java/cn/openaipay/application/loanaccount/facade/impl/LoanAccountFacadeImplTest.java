package cn.openaipay.application.loanaccount.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.application.agreement.facade.AgreementFacade;
import cn.openaipay.application.loanaccount.command.CreateLoanAccountCommand;
import cn.openaipay.application.loanaccount.command.LoanTccCancelCommand;
import cn.openaipay.application.loanaccount.command.LoanTccConfirmCommand;
import cn.openaipay.application.loanaccount.command.LoanTccTryCommand;
import cn.openaipay.application.loanaccount.dto.LoanAccountDTO;
import cn.openaipay.application.loanaccount.dto.LoanTccBranchDTO;
import cn.openaipay.application.loanaccount.service.LoanAccountService;
import java.math.BigDecimal;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * LoanAccountFacadeImpl 门面行为测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
@ExtendWith(MockitoExtension.class)
class LoanAccountFacadeImplTest {

    @Mock
    private LoanAccountService loanAccountService;
    @Mock
    private AgreementFacade agreementFacade;

    private LoanAccountFacadeImpl facade;

    @BeforeEach
    void setUp() {
        facade = new LoanAccountFacadeImpl(loanAccountService, agreementFacade);
    }

    @Test
    void createLoanAccountShouldDelegateToAccountService() {
        CreateLoanAccountCommand command = new CreateLoanAccountCommand(
                880100068483692100L,
                "LA0001",
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("1000.00")),
                10
        );
        when(loanAccountService.createLoanAccount(command)).thenReturn("LA0001");

        String accountNo = facade.createLoanAccount(command);

        assertEquals("LA0001", accountNo);
        verify(loanAccountService).createLoanAccount(command);
    }

    @Test
    void tccTryShouldDelegateToLoanAccountService() {
        LoanTccBranchDTO expected = new LoanTccBranchDTO("x1", "b1", "TRIED", "ok");
        when(loanAccountService.tccTry(org.mockito.ArgumentMatchers.any())).thenReturn(expected);

        LoanTccBranchDTO actual = facade.tccTry(
                "x1",
                "b1",
                "LA0001",
                "REPAY",
                "PRINCIPAL",
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("88.00")),
                "BIZ-1"
        );

        assertEquals("TRIED", actual.branchStatus());
        ArgumentCaptor<LoanTccTryCommand> captor = ArgumentCaptor.forClass(LoanTccTryCommand.class);
        verify(loanAccountService).tccTry(captor.capture());
        assertEquals("x1", captor.getValue().xid());
        assertEquals("b1", captor.getValue().branchId());
        assertEquals("LA0001", captor.getValue().accountNo());
    }

    @Test
    void tccConfirmShouldDelegateToLoanAccountService() {
        LoanTccBranchDTO expected = new LoanTccBranchDTO("x2", "b2", "CONFIRMED", "ok");
        when(loanAccountService.tccConfirm(org.mockito.ArgumentMatchers.any())).thenReturn(expected);

        LoanTccBranchDTO actual = facade.tccConfirm("x2", "b2");

        assertEquals("CONFIRMED", actual.branchStatus());
        ArgumentCaptor<LoanTccConfirmCommand> captor = ArgumentCaptor.forClass(LoanTccConfirmCommand.class);
        verify(loanAccountService).tccConfirm(captor.capture());
        assertEquals("x2", captor.getValue().xid());
        assertEquals("b2", captor.getValue().branchId());
    }

    @Test
    void tccCancelShouldDelegateToLoanAccountService() {
        LoanTccBranchDTO expected = new LoanTccBranchDTO("x3", "b3", "CANCELED", "ok");
        when(loanAccountService.tccCancel(org.mockito.ArgumentMatchers.any())).thenReturn(expected);

        LoanTccBranchDTO actual = facade.tccCancel(
                "x3",
                "b3",
                "LA0001",
                "REPAY",
                "PRINCIPAL",
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("66.00")),
                "BIZ-3"
        );

        assertEquals("CANCELED", actual.branchStatus());
        ArgumentCaptor<LoanTccCancelCommand> captor = ArgumentCaptor.forClass(LoanTccCancelCommand.class);
        verify(loanAccountService).tccCancel(captor.capture());
        assertEquals("x3", captor.getValue().xid());
        assertEquals("b3", captor.getValue().branchId());
        assertEquals("LA0001", captor.getValue().accountNo());
    }

    @Test
    void getAccountNoByUserIdShouldReadFromAccountService() {
        when(agreementFacade.isAiLoanOpened(880100068483692100L)).thenReturn(true);
        LoanAccountDTO account = new LoanAccountDTO(
                "LA0001",
                880100068483692100L,
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("200000.00")),
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("180000.00")),
                new java.math.BigDecimal("3.24"),
                new java.math.BigDecimal("5.04"),
                10,
                "ACTIVE",
                "ACTIVE"
        );
        when(loanAccountService.getLoanAccountByUserId(880100068483692100L)).thenReturn(account);

        String accountNo = facade.getAccountNoByUserId(880100068483692100L);

        assertEquals("LA0001", accountNo);
        verify(loanAccountService).getLoanAccountByUserId(880100068483692100L);
    }
}
