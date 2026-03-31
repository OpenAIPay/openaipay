package cn.openaipay.domain.walletaccount.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;

/**
 * WalletAccountInvariantTest 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
class WalletAccountInvariantTest {

    @Test
    void constructorShouldRejectNegativeAvailableBalance() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new WalletAccount(
                880100068483692100L,
                "CNY",
                money("-0.01"),
                money("0.00"),
                WalletAccountStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        assertEquals("availableBalance must not be less than 0", exception.getMessage());
    }

    @Test
    void holdDebitShouldFailWhenAvailableBalanceInsufficient() {
        WalletAccount account = new WalletAccount(
                880100068483692100L,
                "CNY",
                money("10.00"),
                money("0.00"),
                WalletAccountStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        assertThrows(IllegalArgumentException.class, () -> account.hold(
                TccOperationType.DEBIT,
                money("10.01"),
                LocalDateTime.now()
        ));
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
