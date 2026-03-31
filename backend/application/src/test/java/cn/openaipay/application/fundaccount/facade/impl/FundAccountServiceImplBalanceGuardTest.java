package cn.openaipay.application.fundaccount.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.application.fundaccount.command.FundSubscribeCommand;
import cn.openaipay.application.fundaccount.service.impl.FundAccountServiceImpl;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.fundaccount.model.FundProduct;
import cn.openaipay.domain.fundaccount.repository.FundAccountRepository;
import cn.openaipay.domain.fundtrade.repository.FundTradeRepository;
import cn.openaipay.domain.riskpolicy.model.RiskDecision;
import cn.openaipay.domain.riskpolicy.service.RiskPolicyDomainService;
import cn.openaipay.domain.shared.number.FundAmount;
import cn.openaipay.domain.trade.repository.TradeRepository;
import cn.openaipay.domain.walletaccount.model.WalletAccount;
import cn.openaipay.domain.walletaccount.model.WalletAccountStatus;
import cn.openaipay.domain.walletaccount.repository.WalletAccountRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * FundAccountServiceImplBalanceGuardTest 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
@ExtendWith(MockitoExtension.class)
class FundAccountServiceImplBalanceGuardTest {

    @Mock
    private FundAccountRepository fundAccountRepository;
    @Mock
    private FundTradeRepository fundTradeRepository;
    @Mock
    private TradeRepository tradeRepository;
    @Mock
    private WalletAccountRepository walletAccountRepository;
    @Mock
    private RiskPolicyDomainService riskPolicyDomainService;
    @Mock
    private AiPayIdGenerator aiPayIdGenerator;

    private FundAccountServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FundAccountServiceImpl(
                fundAccountRepository,
                fundTradeRepository,
                tradeRepository,
                walletAccountRepository,
                riskPolicyDomainService,
                aiPayIdGenerator,
                new BigDecimal("0.01045")
        );
    }

    @Test
    void subscribeShouldFailWhenWalletBalanceIsInsufficient() {
        Long userId = 880100068483692100L;
        String orderNo = "30262026032712000000000000000001";
        String fundCode = "AICASH";
        FundSubscribeCommand command = new FundSubscribeCommand(
                orderNo,
                userId,
                fundCode,
                new FundAmount("100.0000"),
                "BIZ-20260327-0001"
        );

        when(aiPayIdGenerator.validate(orderNo)).thenReturn(true);
        when(riskPolicyDomainService.evaluate(any())).thenReturn(RiskDecision.pass());
        when(fundTradeRepository.findTransactionForUpdate(orderNo)).thenReturn(Optional.empty());
        when(fundAccountRepository.findProduct(fundCode)).thenReturn(Optional.of(
                FundProduct.defaultOf(fundCode, "CNY", LocalDateTime.now())
        ));
        when(walletAccountRepository.findByUserIdAndCurrencyForUpdate(eq(userId), eq("CNY")))
                .thenReturn(Optional.of(new WalletAccount(
                        userId,
                        "CNY",
                        money("0.00"),
                        money("0.00"),
                        WalletAccountStatus.ACTIVE,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.subscribe(command));

        assertEquals("账户余额不足", exception.getMessage());
        verify(fundAccountRepository, never()).save(any());
        verify(fundTradeRepository, never()).saveTransaction(any());
        verify(tradeRepository, never()).saveTradeOrder(any());
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
