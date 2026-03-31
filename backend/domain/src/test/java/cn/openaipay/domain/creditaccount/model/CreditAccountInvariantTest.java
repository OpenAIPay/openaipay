package cn.openaipay.domain.creditaccount.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;

/**
 * CreditAccountInvariantTest 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
class CreditAccountInvariantTest {

    @Test
    void constructorShouldRejectNegativePrincipalBalance() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new CreditAccount(
                "AICREDIT-880100068483692100",
                880100068483692100L,
                money("1000.00"),
                money("-0.01"),
                money("0.00"),
                money("0.00"),
                money("0.00"),
                money("0.00"),
                money("0.00"),
                CreditAccountStatus.NORMAL,
                CreditAccountPayStatus.NORMAL,
                10,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        assertEquals("principalBalance must not be less than 0", exception.getMessage());
    }

    @Test
    void holdRepayShouldFailWhenPrincipalBalanceInsufficient() {
        CreditAccount account = new CreditAccount(
                "AICREDIT-880100068483692100",
                880100068483692100L,
                money("1000.00"),
                money("10.00"),
                money("0.00"),
                money("0.00"),
                money("0.00"),
                money("0.00"),
                money("0.00"),
                CreditAccountStatus.NORMAL,
                CreditAccountPayStatus.NORMAL,
                10,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        assertThrows(IllegalArgumentException.class, () -> account.hold(
                CreditTccOperationType.REPAY,
                CreditAssetCategory.PRINCIPAL,
                money("10.01"),
                LocalDateTime.now()
        ));
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
