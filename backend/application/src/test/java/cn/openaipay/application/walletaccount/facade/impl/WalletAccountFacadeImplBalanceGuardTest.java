package cn.openaipay.application.walletaccount.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.application.walletaccount.service.impl.WalletAccountServiceImpl;
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
 * 钱包门面余额守卫测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
@ExtendWith(MockitoExtension.class)
class WalletAccountFacadeImplBalanceGuardTest {

    @Mock
    private WalletAccountRepository walletAccountRepository;

    private WalletAccountFacadeImpl facade;

    @BeforeEach
    void setUp() {
        WalletAccountServiceImpl service = new WalletAccountServiceImpl(walletAccountRepository);
        facade = new WalletAccountFacadeImpl(service);
    }

    @Test
    void tccTryDebitShouldFailWhenAvailableBalanceIsInsufficient() {
        Long userId = 880100068483692100L;
        String xid = "pay-global:30212026032712000000000000000001";
        String branchId = "30222026032712000000000000000001";
        String businessNo = "30212026032712000000000000000001";

        when(walletAccountRepository.findBranchForUpdate(xid, branchId)).thenReturn(Optional.empty());
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

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> facade.tccTry(
                        xid,
                        branchId,
                        userId,
                        "DEBIT",
                        "PAY_HOLD",
                        money("10.00"),
                        businessNo
                )
        );

        assertEquals("insufficient available balance", exception.getMessage());
        verify(walletAccountRepository, never()).save(any());
        verify(walletAccountRepository, never()).createBranch(any());
        verify(walletAccountRepository, never()).createFreezeRecord(any());
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
