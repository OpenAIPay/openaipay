package cn.openaipay.application.creditaccount.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.application.agreement.facade.AgreementFacade;
import cn.openaipay.application.creditaccount.command.CreditTccCancelCommand;
import cn.openaipay.application.creditaccount.command.CreditTccConfirmCommand;
import cn.openaipay.application.creditaccount.command.CreditTccTryCommand;
import cn.openaipay.application.creditaccount.dto.CreditTccBranchDTO;
import cn.openaipay.application.creditaccount.service.CreditAccountService;
import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import cn.openaipay.domain.creditaccount.repository.CreditAccountRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 信用账户门面行为测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
@ExtendWith(MockitoExtension.class)
class CreditAccountFacadeImplTest {

    @Mock
    private CreditAccountService creditAccountService;
    @Mock
    private CreditAccountRepository creditAccountRepository;
    @Mock
    private AgreementFacade agreementFacade;

    private CreditAccountFacadeImpl facade;

    @BeforeEach
    void setUp() {
        facade = new CreditAccountFacadeImpl(
                creditAccountService,
                creditAccountRepository,
                agreementFacade
        );
    }

    @Test
    void tccTryShouldDelegateToCreditAccountService() {
        CreditTccBranchDTO expected = new CreditTccBranchDTO("x1", "b1", "TRIED", "ok");
        when(creditAccountService.tccTry(org.mockito.ArgumentMatchers.any())).thenReturn(expected);

        CreditTccBranchDTO actual = facade.tccTry(
                "x1",
                "b1",
                "CA0001",
                "LEND",
                "PRINCIPAL",
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("88.00")),
                "BIZ-1"
        );

        assertEquals("TRIED", actual.branchStatus());
        ArgumentCaptor<CreditTccTryCommand> commandCaptor = ArgumentCaptor.forClass(CreditTccTryCommand.class);
        verify(creditAccountService).tccTry(commandCaptor.capture());
        assertEquals("x1", commandCaptor.getValue().xid());
        assertEquals("b1", commandCaptor.getValue().branchId());
        assertEquals("CA0001", commandCaptor.getValue().accountNo());
        assertEquals("88.00", commandCaptor.getValue().amount().getAmount().toPlainString());
    }

    @Test
    void tccConfirmShouldDelegateToCreditAccountService() {
        CreditTccBranchDTO expected = new CreditTccBranchDTO("x2", "b2", "CONFIRMED", "ok");
        when(creditAccountService.tccConfirm(org.mockito.ArgumentMatchers.any())).thenReturn(expected);

        CreditTccBranchDTO actual = facade.tccConfirm("x2", "b2");

        assertEquals("CONFIRMED", actual.branchStatus());
        ArgumentCaptor<CreditTccConfirmCommand> commandCaptor = ArgumentCaptor.forClass(CreditTccConfirmCommand.class);
        verify(creditAccountService).tccConfirm(commandCaptor.capture());
        assertEquals("x2", commandCaptor.getValue().xid());
        assertEquals("b2", commandCaptor.getValue().branchId());
    }

    @Test
    void tccCancelShouldDelegateToCreditAccountService() {
        CreditTccBranchDTO expected = new CreditTccBranchDTO("x3", "b3", "CANCELED", "ok");
        when(creditAccountService.tccCancel(org.mockito.ArgumentMatchers.any())).thenReturn(expected);

        CreditTccBranchDTO actual = facade.tccCancel(
                "x3",
                "b3",
                "CA0001",
                "REPAY",
                "PRINCIPAL",
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("66.00")),
                "BIZ-3"
        );

        assertEquals("CANCELED", actual.branchStatus());
        ArgumentCaptor<CreditTccCancelCommand> commandCaptor = ArgumentCaptor.forClass(CreditTccCancelCommand.class);
        verify(creditAccountService).tccCancel(commandCaptor.capture());
        assertEquals("x3", commandCaptor.getValue().xid());
        assertEquals("b3", commandCaptor.getValue().branchId());
        assertEquals("CA0001", commandCaptor.getValue().accountNo());
        assertEquals("66.00", commandCaptor.getValue().amount().getAmount().toPlainString());
    }

    @Test
    void getAccountNoByUserIdShouldReadFromRepositoryWithAgreementGuard() {
        Long userId = 880100068483692100L;
        when(agreementFacade.isAiCreditOpened(userId)).thenReturn(true);
        cn.openaipay.domain.creditaccount.model.CreditAccount account =
                mock(cn.openaipay.domain.creditaccount.model.CreditAccount.class);
        when(account.getAccountNo()).thenReturn("CA0001");
        when(creditAccountRepository.findByUserIdAndType(userId, CreditAccountType.AICREDIT))
                .thenReturn(Optional.of(account));

        String accountNo = facade.getAccountNoByUserId(userId);

        assertEquals("CA0001", accountNo);
    }
}
