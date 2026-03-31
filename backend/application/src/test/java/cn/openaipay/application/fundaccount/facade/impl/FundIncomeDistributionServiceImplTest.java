package cn.openaipay.application.fundaccount.facade.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.application.fundaccount.command.FundIncomeSettleCommand;
import cn.openaipay.application.fundaccount.service.FundAccountService;
import cn.openaipay.application.fundaccount.service.impl.FundIncomeDistributionServiceImpl;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.fundaccount.model.FundAccount;
import cn.openaipay.domain.fundaccount.model.FundAccountStatus;
import cn.openaipay.domain.fundaccount.model.FundIncomeCalendar;
import cn.openaipay.domain.fundaccount.model.FundTransaction;
import cn.openaipay.domain.fundaccount.model.FundTransactionStatus;
import cn.openaipay.domain.fundaccount.model.FundTransactionType;
import cn.openaipay.domain.fundaccount.repository.FundAccountRepository;
import cn.openaipay.domain.fundtrade.repository.FundTradeRepository;
import cn.openaipay.domain.shared.number.FundAmount;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * FundIncomeDistributionServiceImplTest 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/26
 */
@ExtendWith(MockitoExtension.class)
class FundIncomeDistributionServiceImplTest {

    @Mock
    private FundAccountRepository fundAccountRepository;
    @Mock
    private FundTradeRepository fundTradeRepository;
    @Mock
    private FundAccountService fundAccountService;
    @Mock
    private AiPayIdGenerator aiPayIdGenerator;

    private static final Long USER_ID = 880109000000000001L;
    private static final String FUND_CODE = "AICASH";
    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 3, 26, 10, 30, 0);

    private FundIncomeDistributionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TestableFundIncomeDistributionServiceImpl(
                fundAccountRepository,
                fundTradeRepository,
                fundAccountService,
                aiPayIdGenerator,
                new BigDecimal("0.010450"),
                FIXED_NOW
        );
    }

    @Test
    void shouldNotSettleIncomeWhenHoldingOnlyFromTodaySubscribe() {
        FundAccount account = accountWithAvailableShare("100.0000");
        FundIncomeCalendar calendar = publishedCalendar(FIXED_NOW.toLocalDate(), "1.0000", "2.0000");
        FundTransaction todaySubscribe = confirmedSubscribe("100.0000");

        when(fundAccountRepository.findByUserIdAndFundCodeForUpdate(USER_ID, FUND_CODE)).thenReturn(Optional.of(account));
        when(fundAccountRepository.findIncomeCalendarForUpdate(FUND_CODE, FIXED_NOW.toLocalDate())).thenReturn(Optional.of(calendar));
        when(fundTradeRepository.findConfirmedTransactionsUpdatedInRange(
                eq(USER_ID),
                eq(FUND_CODE),
                eq(FIXED_NOW.toLocalDate().atStartOfDay()),
                eq(FIXED_NOW),
                any()
        )).thenReturn(List.of(todaySubscribe));

        boolean settled = service.settleTodayIncomeForUserIfNeeded(USER_ID, FUND_CODE);

        assertFalse(settled);
        verify(fundAccountService, never()).settleIncome(any(FundIncomeSettleCommand.class));
    }

    @Test
    void shouldSettleIncomeBasedOnTMinusOneShareAfterTodayRedeem() {
        FundAccount account = accountWithAvailableShare("50.0000");
        FundIncomeCalendar calendar = publishedCalendar(FIXED_NOW.toLocalDate(), "1.0000", "2.0000");
        FundTransaction todayRedeem = confirmedRedeem("50.0000");

        when(fundAccountRepository.findByUserIdAndFundCodeForUpdate(USER_ID, FUND_CODE)).thenReturn(Optional.of(account));
        when(fundAccountRepository.findIncomeCalendarForUpdate(FUND_CODE, FIXED_NOW.toLocalDate())).thenReturn(Optional.of(calendar));
        when(fundTradeRepository.findConfirmedTransactionsUpdatedInRange(
                eq(USER_ID),
                eq(FUND_CODE),
                eq(FIXED_NOW.toLocalDate().atStartOfDay()),
                eq(FIXED_NOW),
                any()
        )).thenReturn(List.of(todayRedeem));
        when(fundTradeRepository.findTransactionByBusinessNoAndType(
                USER_ID,
                FUND_CODE,
                FundTransactionType.INCOME_SETTLE,
                "FIS:AICASH:20260326:" + USER_ID
        )).thenReturn(Optional.empty());
        when(aiPayIdGenerator.generate(any(), any(), any())).thenReturn("30922026032610300000010000000001");

        boolean settled = service.settleTodayIncomeForUserIfNeeded(USER_ID, FUND_CODE);

        assertTrue(settled);
        ArgumentCaptor<FundIncomeSettleCommand> commandCaptor = ArgumentCaptor.forClass(FundIncomeSettleCommand.class);
        verify(fundAccountService).settleIncome(commandCaptor.capture());
        FundIncomeSettleCommand command = commandCaptor.getValue();
        // 100 份额 * 2.0000 / 10000 = 0.0200
        org.junit.jupiter.api.Assertions.assertEquals("0.0200", command.incomeAmount().toPlainString());
        org.junit.jupiter.api.Assertions.assertEquals("FIS:AICASH:20260326:" + USER_ID, command.businessNo());
    }

    private FundAccount accountWithAvailableShare(String availableShare) {
        return new FundAccount(
                USER_ID,
                FUND_CODE,
                "CNY",
                fa(availableShare),
                fa("0"),
                fa("0"),
                fa("0"),
                fa("0"),
                fa("0"),
                fa("1.0000"),
                FundAccountStatus.ACTIVE,
                FIXED_NOW.minusDays(1),
                FIXED_NOW
        );
    }

    private FundIncomeCalendar publishedCalendar(LocalDate bizDate, String nav, String incomePer10k) {
        FundIncomeCalendar calendar = FundIncomeCalendar.planned(FUND_CODE, bizDate, FIXED_NOW.minusHours(2));
        calendar.publish(fa(nav), fa(incomePer10k), FIXED_NOW.minusHours(1));
        return calendar;
    }

    private FundTransaction confirmedSubscribe(String confirmedShare) {
        return new FundTransaction(
                "30922026032610200000010000000001",
                USER_ID,
                FUND_CODE,
                FundTransactionType.SUBSCRIBE,
                FundTransactionStatus.CONFIRMED,
                fa("100.0000"),
                fa("0"),
                fa("100.0000"),
                fa(confirmedShare),
                "BIZ-SUBSCRIBE-TODAY",
                null,
                FIXED_NOW.minusMinutes(20),
                FIXED_NOW.minusMinutes(20)
        );
    }

    private FundTransaction confirmedRedeem(String requestShare) {
        return new FundTransaction(
                "30922026032610250000010000000002",
                USER_ID,
                FUND_CODE,
                FundTransactionType.FAST_REDEEM,
                FundTransactionStatus.CONFIRMED,
                fa("50.0000"),
                fa(requestShare),
                fa("50.0000"),
                fa("0"),
                "BIZ-REDEEM-TODAY",
                "FAST|destination=BANK_CARD",
                FIXED_NOW.minusMinutes(10),
                FIXED_NOW.minusMinutes(5)
        );
    }

    private FundAmount fa(String value) {
        return FundAmount.of(new BigDecimal(value));
    }

    private static final class TestableFundIncomeDistributionServiceImpl extends FundIncomeDistributionServiceImpl {

        private final LocalDateTime fixedNow;

        private TestableFundIncomeDistributionServiceImpl(FundAccountRepository fundAccountRepository,
                                                          FundTradeRepository fundTradeRepository,
                                                          FundAccountService fundAccountService,
                                                          AiPayIdGenerator aiPayIdGenerator,
                                                          BigDecimal aicashAnnualizedYieldRate,
                                                          LocalDateTime fixedNow) {
            super(
                    fundAccountRepository,
                    fundTradeRepository,
                    fundAccountService,
                    aiPayIdGenerator,
                    aicashAnnualizedYieldRate
            );
            this.fixedNow = fixedNow;
        }

        @Override
        protected LocalDateTime now() {
            return fixedNow;
        }
    }
}
