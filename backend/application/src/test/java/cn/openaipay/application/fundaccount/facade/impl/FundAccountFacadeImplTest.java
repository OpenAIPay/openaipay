package cn.openaipay.application.fundaccount.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.application.fundaccount.command.FundSubscribeCancelCommand;
import cn.openaipay.application.fundaccount.command.FundSubscribeCommand;
import cn.openaipay.application.fundaccount.command.FundSubscribeConfirmCommand;
import cn.openaipay.application.fundaccount.dto.FundTransactionDTO;
import cn.openaipay.application.fundaccount.service.FundAccountService;
import cn.openaipay.domain.fundaccount.model.FundAccount;
import cn.openaipay.domain.fundaccount.model.FundAccountStatus;
import cn.openaipay.domain.fundaccount.model.FundTransaction;
import cn.openaipay.domain.fundaccount.model.FundTransactionStatus;
import cn.openaipay.domain.fundaccount.model.FundTransactionType;
import cn.openaipay.domain.fundaccount.repository.FundAccountRepository;
import cn.openaipay.domain.fundtrade.repository.FundTradeRepository;
import cn.openaipay.domain.riskpolicy.model.RiskDecision;
import cn.openaipay.domain.riskpolicy.service.RiskPolicyDomainService;
import cn.openaipay.domain.shared.number.FundAmount;
import cn.openaipay.domain.trade.repository.TradeRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
 * FundAccountFacadeImplTest 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@ExtendWith(MockitoExtension.class)
class FundAccountFacadeImplTest {

    /** 资金信息 */
    @Mock
    private FundAccountService fundAccountService;
    /** 资金信息 */
    @Mock
    private FundAccountRepository fundAccountRepository;
    /** 基金交易仓储。 */
    @Mock
    private FundTradeRepository fundTradeRepository;
    /** 交易信息 */
    @Mock
    private TradeRepository tradeRepository;
    /** 风控服务。 */
    @Mock
    private RiskPolicyDomainService riskPolicyDomainService;

    /** 门面信息 */
    private FundAccountFacadeImpl facade;

    @BeforeEach
    void setUp() {
        facade = new FundAccountFacadeImpl(
                fundAccountService,
                fundAccountRepository,
                fundTradeRepository,
                tradeRepository,
                riskPolicyDomainService
        );
    }

    @Test
    void freezeShareForPayShouldCreatePendingFreezeTradeAndFreezeShare() {
        Long userId = 880100068483692100L;
        String fundTradeNo = "30262026031911000000000000000001";
        String fundCode = "AICASH";
        String businessNo = "30222026031911000000000000000001";
        Money amount = Money.of(CurrencyUnit.of("CNY"), 20.00);
        LocalDateTime now = LocalDateTime.of(2026, 3, 19, 11, 0);
        FundAccount fundAccount = new FundAccount(
                userId,
                fundCode,
                "CNY",
                fa("100"),
                fa("0"),
                fa("0"),
                fa("0"),
                fa("0"),
                fa("0"),
                fa("2"),
                FundAccountStatus.ACTIVE,
                now,
                now
        );

        when(riskPolicyDomainService.evaluate(any())).thenReturn(RiskDecision.pass());
        when(fundTradeRepository.findTransactionForUpdate(fundTradeNo)).thenReturn(Optional.empty());
        when(fundAccountRepository.findByUserIdAndFundCode(userId, fundCode)).thenReturn(Optional.of(fundAccount));
        when(fundAccountRepository.findByUserIdAndFundCodeForUpdate(userId, fundCode)).thenReturn(Optional.of(fundAccount));

        var result = facade.freezeShareForPay(fundTradeNo, userId, fundCode, amount, businessNo);

        assertEquals("AICASH", result.fundCode());
        assertEquals("10.0000", result.share().toPlainString());
        assertEquals("2.0000", result.nav().toPlainString());
        assertEquals("90.0000", fundAccount.getAvailableShare().toPlainString());
        assertEquals("10.0000", fundAccount.getFrozenShare().toPlainString());

        ArgumentCaptor<FundTransaction> transactionCaptor = ArgumentCaptor.forClass(FundTransaction.class);
        verify(fundTradeRepository).saveTransaction(transactionCaptor.capture());
        FundTransaction savedTransaction = transactionCaptor.getValue();
        assertNotNull(savedTransaction);
        assertEquals(fundTradeNo, savedTransaction.getOrderNo());
        assertEquals(FundTransactionType.FREEZE, savedTransaction.getTransactionType());
        assertEquals(FundTransactionStatus.PENDING, savedTransaction.getTransactionStatus());
        assertEquals("20.0000", savedTransaction.getRequestAmount().toPlainString());
        assertEquals("10.0000", savedTransaction.getRequestShare().toPlainString());
    }

    @Test
    void confirmFrozenShareForPayShouldSettleFrozenShareAndMarkConfirmed() {
        Long userId = 880100068483692100L;
        String fundTradeNo = "30262026031911000000000000000002";
        String fundCode = "AICASH";
        String businessNo = "30222026031911000000000000000002";
        LocalDateTime now = LocalDateTime.of(2026, 3, 19, 11, 5);
        FundTransaction pendingFreeze = new FundTransaction(
                fundTradeNo,
                userId,
                fundCode,
                FundTransactionType.FREEZE,
                FundTransactionStatus.PENDING,
                fa("20"),
                fa("10"),
                fa("0"),
                fa("0"),
                businessNo,
                "flow=PAY_FUND_FREEZE;nav=2.0000",
                now,
                now
        );
        FundAccount fundAccount = new FundAccount(
                userId,
                fundCode,
                "CNY",
                fa("90"),
                fa("10"),
                fa("0"),
                fa("0"),
                fa("0"),
                fa("0"),
                fa("2"),
                FundAccountStatus.ACTIVE,
                now,
                now
        );

        when(fundTradeRepository.findTransactionForUpdate(fundTradeNo)).thenReturn(Optional.of(pendingFreeze));
        when(fundAccountRepository.findByUserIdAndFundCodeForUpdate(userId, fundCode)).thenReturn(Optional.of(fundAccount));

        facade.confirmFrozenShareForPay(userId, fundTradeNo);

        assertEquals("0.0000", fundAccount.getFrozenShare().toPlainString());
        assertEquals(FundTransactionStatus.CONFIRMED, pendingFreeze.getTransactionStatus());
        assertEquals("20.0000", pendingFreeze.getConfirmedAmount().toPlainString());
        assertEquals("10.0000", pendingFreeze.getConfirmedShare().toPlainString());
        verify(fundAccountRepository).save(fundAccount);
        verify(fundTradeRepository).saveTransaction(pendingFreeze);
    }

    @Test
    void compensateFrozenShareForPayShouldUnfreezeShareAndMarkCompensated() {
        Long userId = 880100068483692100L;
        String fundTradeNo = "30262026031911000000000000000003";
        String fundCode = "AICASH";
        String businessNo = "30222026031911000000000000000003";
        LocalDateTime now = LocalDateTime.of(2026, 3, 19, 11, 10);
        FundTransaction pendingFreeze = new FundTransaction(
                fundTradeNo,
                userId,
                fundCode,
                FundTransactionType.FREEZE,
                FundTransactionStatus.PENDING,
                fa("20"),
                fa("10"),
                fa("0"),
                fa("0"),
                businessNo,
                "flow=PAY_FUND_FREEZE;nav=2.0000",
                now,
                now
        );
        FundAccount fundAccount = new FundAccount(
                userId,
                fundCode,
                "CNY",
                fa("90"),
                fa("10"),
                fa("0"),
                fa("0"),
                fa("0"),
                fa("0"),
                fa("2"),
                FundAccountStatus.ACTIVE,
                now,
                now
        );

        when(fundTradeRepository.findTransactionForUpdate(fundTradeNo)).thenReturn(Optional.of(pendingFreeze));
        when(fundAccountRepository.findByUserIdAndFundCodeForUpdate(userId, fundCode)).thenReturn(Optional.of(fundAccount));

        facade.compensateFrozenShareForPay(userId, fundTradeNo, fundCode, businessNo);

        assertEquals("100.0000", fundAccount.getAvailableShare().toPlainString());
        assertEquals("0.0000", fundAccount.getFrozenShare().toPlainString());
        assertEquals(FundTransactionStatus.COMPENSATED, pendingFreeze.getTransactionStatus());
        verify(fundAccountRepository).save(fundAccount);
        verify(fundTradeRepository).saveTransaction(pendingFreeze);
    }

    @Test
    void compensateFrozenShareForPayShouldCreateFenceWhenTradeMissing() {
        Long userId = 880100068483692100L;
        String fundTradeNo = "30262026031911000000000000000004";
        String fundCode = "AICASH";
        String businessNo = "30222026031911000000000000000004";

        when(fundTradeRepository.findTransactionForUpdate(fundTradeNo)).thenReturn(Optional.empty());
        when(fundTradeRepository.saveTransaction(any(FundTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, FundTransaction.class));

        facade.compensateFrozenShareForPay(userId, fundTradeNo, fundCode, businessNo);

        ArgumentCaptor<FundTransaction> transactionCaptor = ArgumentCaptor.forClass(FundTransaction.class);
        verify(fundTradeRepository).saveTransaction(transactionCaptor.capture());
        verify(fundAccountRepository, never()).save(any(FundAccount.class));
        FundTransaction compensateFence = transactionCaptor.getValue();
        assertEquals(FundTransactionType.FREEZE, compensateFence.getTransactionType());
        assertEquals(FundTransactionStatus.COMPENSATED, compensateFence.getTransactionStatus());
        assertEquals("0.0000", compensateFence.getRequestAmount().toPlainString());
        assertEquals("0.0000", compensateFence.getRequestShare().toPlainString());
    }

    @Test
    void subscribeShouldDelegateToFundAccountService() {
        FundSubscribeCommand command = new FundSubscribeCommand(
                "30262026032711000000000000000001",
                880100068483692100L,
                "AICASH",
                FundAmount.of(new BigDecimal("20.0000")),
                "BIZ-1"
        );
        FundTransactionDTO expected = new FundTransactionDTO(
                command.orderNo(),
                "SUBSCRIBE",
                "PENDING",
                "ok"
        );
        when(fundAccountService.subscribe(command)).thenReturn(expected);

        FundTransactionDTO actual = facade.subscribe(command);

        assertEquals(command.orderNo(), actual.orderNo());
        verify(fundAccountService).subscribe(command);
    }

    @Test
    void confirmSubscribeShouldDelegateToFundAccountService() {
        FundSubscribeConfirmCommand command = new FundSubscribeConfirmCommand(
                "30262026032711000000000000000002",
                FundAmount.of(new BigDecimal("9.8765")),
                FundAmount.of(new BigDecimal("2.1234"))
        );
        FundTransactionDTO expected = new FundTransactionDTO(
                command.orderNo(),
                "SUBSCRIBE",
                "CONFIRMED",
                "ok"
        );
        when(fundAccountService.confirmSubscribe(command)).thenReturn(expected);

        FundTransactionDTO actual = facade.confirmSubscribe(command);

        assertEquals("CONFIRMED", actual.transactionStatus());
        verify(fundAccountService).confirmSubscribe(command);
    }

    @Test
    void cancelSubscribeShouldDelegateToFundAccountService() {
        FundSubscribeCancelCommand command = new FundSubscribeCancelCommand(
                "30262026032711000000000000000003"
        );
        FundTransactionDTO expected = new FundTransactionDTO(
                command.orderNo(),
                "SUBSCRIBE",
                "CANCELED",
                "ok"
        );
        when(fundAccountService.cancelSubscribe(command)).thenReturn(expected);

        FundTransactionDTO actual = facade.cancelSubscribe(command);

        assertEquals("CANCELED", actual.transactionStatus());
        verify(fundAccountService).cancelSubscribe(command);
    }

    private FundAmount fa(String value) {
        return FundAmount.of(new BigDecimal(value));
    }
}
