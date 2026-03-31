package cn.openaipay.domain.trade.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TradeOrderTest 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
class TradeOrderTest {

    @Test
    void shouldAllowSucceededFundSnapshotWithoutSubmittedSplitPlan() {
        assertDoesNotThrow(() -> new TradeOrder(
                1L,
                "30112026031114584000210068116858",
                "FUND:30112026031114584000210068116858",
                TradeType.PAY,
                "FUND_SUBSCRIBE",
                "AICASH",
                "CODEX-YB-DBLINK-20260311-004",
                null,
                880100068483692100L,
                880100068483692100L,
                "FUND_ACCOUNT",
                money("34.56"),
                money("0.00"),
                money("34.56"),
                money("34.56"),
                TradeSplitPlan.empty(CurrencyUnit.of("CNY")),
                null,
                null,
                0,
                null,
                null,
                TradeStatus.SUCCEEDED,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));
    }

    @Test
    void shouldRejectSnapshotWhenPositiveSplitPlanExceedsPayableAmount() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new TradeOrder(
                2L,
                "30112026031114584000210068116859",
                "REQ-20260314-0001",
                TradeType.PAY,
                "TRADE_PAY",
                "WALLET",
                "REQ-20260314-0001",
                null,
                880100068483692100L,
                880100068483692101L,
                "WALLET",
                money("20.00"),
                money("0.00"),
                money("20.00"),
                money("20.00"),
                TradeSplitPlan.of(
                        CurrencyUnit.of("CNY"),
                        money("20.01"),
                        money("0.00"),
                        money("0.00"),
                        money("0.00")
                ),
                null,
                null,
                0,
                null,
                null,
                TradeStatus.SUCCEEDED,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        assertEquals("participant split amount must equal payableAmount", exception.getMessage());
    }

    @Test
    void shouldAllowMarkPaySubmittedWhenSplitIsLessThanPayableAfterCouponScenario() {
        TradeOrder tradeOrder = createQuotedTradeOrder("REQ-20260321-0001", money("100.00"));

        assertDoesNotThrow(() -> tradeOrder.markPaySubmitted(
                "30112026032114584000210068110001",
                TradeSplitPlan.of(
                        CurrencyUnit.of("CNY"),
                        money("0.00"),
                        money("0.00"),
                        money("0.00"),
                        money("95.00")
                ),
                LocalDateTime.now()
        ));
        assertEquals(TradeStatus.PAY_SUBMITTED, tradeOrder.getStatus());
    }

    @Test
    void shouldRejectMarkPaySubmittedWhenSplitExceedsPayable() {
        TradeOrder tradeOrder = createQuotedTradeOrder("REQ-20260321-0002", money("100.00"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> tradeOrder.markPaySubmitted(
                "30112026032114584000210068110002",
                TradeSplitPlan.of(
                        CurrencyUnit.of("CNY"),
                        money("0.00"),
                        money("0.00"),
                        money("0.00"),
                        money("100.01")
                ),
                LocalDateTime.now()
        ));
        assertEquals("participant split amount must equal payableAmount", exception.getMessage());
    }

    private TradeOrder createQuotedTradeOrder(String requestNo, Money payableAmount) {
        LocalDateTime now = LocalDateTime.now();
        TradeOrder tradeOrder = TradeOrder.create(
                "30112026032114584000210068118888",
                requestNo,
                TradeType.PAY,
                "APP_PHONE_TOP_UP",
                null,
                880100068483692100L,
                880201069206400001L,
                "BANK_CARD",
                payableAmount,
                "entry=mobile-hall-top-up;operator=CHINA_MOBILE",
                now
        );
        tradeOrder.markPricingQuoteApplied(
                "QTE-20260321-0001",
                money("0.00"),
                payableAmount,
                payableAmount,
                now
        );
        return tradeOrder;
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
