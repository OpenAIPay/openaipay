package cn.openaipay.domain.fundaccount.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cn.openaipay.domain.shared.number.FundAmount;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * FundAccountInvariantTest 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
class FundAccountInvariantTest {

    @Test
    void constructorShouldRejectNegativeAvailableShare() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new FundAccount(
                880100068483692100L,
                "AICASH",
                "CNY",
                new FundAmount("-0.0001"),
                FundAmount.ZERO,
                FundAmount.ZERO,
                FundAmount.ZERO,
                FundAmount.ZERO,
                FundAmount.ZERO,
                FundAmount.ONE,
                FundAccountStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        assertEquals("availableShare must not be less than 0", exception.getMessage());
    }

    @Test
    void freezeShareShouldFailWhenAvailableShareInsufficient() {
        FundAccount account = new FundAccount(
                880100068483692100L,
                "AICASH",
                "CNY",
                new FundAmount("1.0000"),
                FundAmount.ZERO,
                FundAmount.ZERO,
                FundAmount.ZERO,
                FundAmount.ZERO,
                FundAmount.ZERO,
                FundAmount.ONE,
                FundAccountStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        assertThrows(IllegalArgumentException.class, () -> account.freezeShare(
                new FundAmount("1.0001"),
                LocalDateTime.now()
        ));
    }
}
