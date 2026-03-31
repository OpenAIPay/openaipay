package cn.openaipay.application.accountquery.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.application.creditaccount.dto.CreditAccountDTO;
import cn.openaipay.application.creditaccount.dto.CreditCurrentBillDetailDTO;
import cn.openaipay.application.creditaccount.facade.CreditAccountFacade;
import cn.openaipay.application.fundaccount.dto.FundAccountDTO;
import cn.openaipay.application.fundaccount.facade.FundAccountFacade;
import cn.openaipay.application.loanaccount.dto.LoanAccountDTO;
import cn.openaipay.application.loanaccount.facade.LoanAccountFacade;
import cn.openaipay.application.walletaccount.dto.WalletAccountDTO;
import cn.openaipay.application.walletaccount.facade.WalletAccountFacade;
import java.math.BigDecimal;
import java.util.List;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 账户查询门面实现测试
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
@ExtendWith(MockitoExtension.class)
class AccountQueryFacadeImplTest {

    @Mock
    private CreditAccountFacade creditAccountFacade;
    @Mock
    private LoanAccountFacade loanAccountFacade;
    @Mock
    private FundAccountFacade fundAccountFacade;
    @Mock
    private WalletAccountFacade walletAccountFacade;

    private AccountQueryFacadeImpl facade;

    @BeforeEach
    void setUp() {
        facade = new AccountQueryFacadeImpl(
                creditAccountFacade,
                loanAccountFacade,
                fundAccountFacade,
                walletAccountFacade
        );
    }

    @Test
    void shouldDelegateCreditAccountQuery() {
        Long userId = 880100068483692100L;
        CreditAccountDTO expected = new CreditAccountDTO(
                "CA0001",
                userId,
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("200000.00")),
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("180000.00")),
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("20000.00")),
                Money.zero(CurrencyUnit.of("CNY")),
                Money.zero(CurrencyUnit.of("CNY")),
                Money.zero(CurrencyUnit.of("CNY")),
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("123.45")),
                10,
                "NORMAL",
                "NORMAL"
        );
        when(creditAccountFacade.getCreditAccountByUserId(userId)).thenReturn(expected);

        CreditAccountDTO actual = facade.getCreditAccountByUserId(userId);

        assertEquals("CA0001", actual.accountNo());
        verify(creditAccountFacade).getCreditAccountByUserId(userId);
    }

    @Test
    void shouldDelegateCreditBillQueries() {
        Long userId = 880100068483692100L;
        CreditCurrentBillDetailDTO current = new CreditCurrentBillDetailDTO(
                "当前账单",
                "入账周期",
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("99.00")),
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("120.00")),
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("1.00")),
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("20.00")),
                List.of()
        );
        CreditCurrentBillDetailDTO next = new CreditCurrentBillDetailDTO(
                "下期账单",
                "入账周期",
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("66.00")),
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("80.00")),
                Money.zero(CurrencyUnit.of("CNY")),
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("14.00")),
                List.of()
        );
        when(creditAccountFacade.getCurrentBillDetailByUserId(userId)).thenReturn(current);
        when(creditAccountFacade.getNextBillDetailByUserId(userId)).thenReturn(next);

        CreditCurrentBillDetailDTO currentActual = facade.getCreditCurrentBillDetailByUserId(userId);
        CreditCurrentBillDetailDTO nextActual = facade.getCreditNextBillDetailByUserId(userId);

        assertEquals("当前账单", currentActual.title());
        assertEquals("下期账单", nextActual.title());
        verify(creditAccountFacade).getCurrentBillDetailByUserId(userId);
        verify(creditAccountFacade).getNextBillDetailByUserId(userId);
    }

    @Test
    void shouldDelegateLoanFundWalletQueries() {
        Long userId = 880100068483692100L;
        LoanAccountDTO loan = new LoanAccountDTO(
                "LA0001",
                userId,
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("200000.00")),
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("180000.00")),
                new BigDecimal("3.24"),
                new BigDecimal("5.04"),
                10,
                "ACTIVE",
                "ACTIVE"
        );
        FundAccountDTO fund = new FundAccountDTO(
                userId,
                "AICASH",
                "CNY",
                cn.openaipay.domain.shared.number.FundAmount.of(new BigDecimal("100.0000")),
                cn.openaipay.domain.shared.number.FundAmount.of(new BigDecimal("0.0000")),
                cn.openaipay.domain.shared.number.FundAmount.of(new BigDecimal("0.0000")),
                cn.openaipay.domain.shared.number.FundAmount.of(new BigDecimal("0.0000")),
                cn.openaipay.domain.shared.number.FundAmount.of(new BigDecimal("100.0000")),
                cn.openaipay.domain.shared.number.FundAmount.of(new BigDecimal("1.0000")),
                cn.openaipay.domain.shared.number.FundAmount.of(new BigDecimal("0.0200")),
                cn.openaipay.domain.shared.number.FundAmount.of(new BigDecimal("2.0000")),
                "ACTIVE",
                new BigDecimal("2.50"),
                cn.openaipay.domain.shared.number.FundAmount.of(new BigDecimal("0.8000"))
        );
        WalletAccountDTO wallet = new WalletAccountDTO(
                userId,
                "CNY",
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("88.00")),
                Money.zero(CurrencyUnit.of("CNY")),
                "ACTIVE"
        );
        when(loanAccountFacade.getLoanAccountByUserId(userId)).thenReturn(loan);
        when(fundAccountFacade.getFundAccount(userId)).thenReturn(fund);
        when(walletAccountFacade.getWalletAccount(userId)).thenReturn(wallet);

        LoanAccountDTO loanActual = facade.getLoanAccountByUserId(userId);
        FundAccountDTO fundActual = facade.getFundAccountByUserId(userId, null);
        WalletAccountDTO walletActual = facade.getWalletAccountByUserId(userId, null);

        assertEquals("LA0001", loanActual.accountNo());
        assertEquals("AICASH", fundActual.fundCode());
        assertEquals("CNY", walletActual.currencyCode());
        verify(loanAccountFacade).getLoanAccountByUserId(userId);
        verify(fundAccountFacade).getFundAccount(userId);
        verify(walletAccountFacade).getWalletAccount(userId);
    }
}
