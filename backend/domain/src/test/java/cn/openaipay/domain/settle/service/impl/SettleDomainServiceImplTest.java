package cn.openaipay.domain.settle.service.impl;

import cn.openaipay.domain.settle.service.SettlePlan;
import cn.openaipay.domain.settle.service.SettlePlanStatus;
import cn.openaipay.domain.settle.service.SettleRequest;
import cn.openaipay.domain.settle.service.SettleWalletAction;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * SettleDomainServiceImplTest 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
class SettleDomainServiceImplTest {

    /** 结算域信息 */
    private final SettleDomainServiceImpl settleDomainService = new SettleDomainServiceImpl();

    @Test
    void transferShouldCreditPayeeBySettleAmountAndAppendPlatformFeeIncome() {
        SettlePlan plan = settleDomainService.resolveCommittedTradePlan(new SettleRequest(
                "TRANSFER",
                1001L,
                2002L,
                "PAY202603140001",
                money("100.00"),
                money("2.00"),
                money("100.00"),
                money("102.00"),
                true,
                "PAYER",
                880921068428800021L
        ));

        assertEquals(SettlePlanStatus.EXECUTE, plan.status());
        assertEquals(2, plan.primaryActions().size());
        assertEquals(1, plan.compensationActions().size());

        assertWalletAction(
                plan.primaryActions().get(0),
                2002L,
                "100.00",
                "CREDIT",
                "TRANSFER",
                "PAY202603140001"
        );
        assertWalletAction(
                plan.primaryActions().get(1),
                880921068428800021L,
                "2.00",
                "CREDIT",
                "TRANSFER",
                "PAY202603140001-FEE-INCOME"
        );
        assertWalletAction(
                plan.compensationActions().get(0),
                1001L,
                "100.00",
                "CREDIT",
                "TRANSFER",
                "PAY202603140001-COMPENSATE"
        );
    }

    private void assertWalletAction(SettleWalletAction action,
                                    Long expectedUserId,
                                    String expectedAmount,
                                    String expectedOperationType,
                                    String expectedTradeBizType,
                                    String expectedSettleBizNo) {
        assertEquals(expectedUserId, action.userId());
        assertEquals(money(expectedAmount), action.amount());
        assertEquals(expectedOperationType, action.operationType());
        assertEquals(expectedTradeBizType, action.tradeBizType());
        assertEquals(expectedSettleBizNo, action.settleBizNo());
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
