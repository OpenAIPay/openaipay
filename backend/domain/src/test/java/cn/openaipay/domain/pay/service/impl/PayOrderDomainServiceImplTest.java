package cn.openaipay.domain.pay.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cn.openaipay.domain.pay.model.PayOrder;
import cn.openaipay.domain.pay.service.PayOrderSubmission;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;

/**
 * PayOrderDomainServiceImplTest 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
class PayOrderDomainServiceImplTest {

    /** 服务信息 */
    private final PayOrderDomainServiceImpl service = new PayOrderDomainServiceImpl();

    @Test
    void createSubmittedOrderShouldDeriveTradeOrderNoFromTradeSource() {
        PayOrder order = service.createSubmittedOrder(new PayOrderSubmission(
                "30212026032100000000000000000001",
                null,
                "BIZ202603210001",
                "TRADE",
                "TRD202603210001",
                1,
                "{\"sourceTradeType\":\"TRANSFER\"}",
                "TRANSFER",
                880100068483692100L,
                880100068483692101L,
                money("88.80"),
                money("88.80"),
                money("0.00"),
                money("0.00"),
                money("0.00"),
                money("0.00"),
                null,
                "{\"originalAmount\":\"88.80\"}",
                "pay-global:30212026032100000000000000000001",
                LocalDateTime.of(2026, 3, 21, 20, 0)
        ));

        assertEquals("TRD202603210001", order.getTradeOrderNo());
        assertEquals("BIZ202603210001", order.getBizOrderNo());
        assertEquals("TRADE", order.getSourceBizType());
        assertEquals("TRD202603210001", order.getSourceBizNo());
    }

    @Test
    void createSubmittedOrderShouldKeepExplicitTradeOrderNoForNonTradeSource() {
        PayOrder order = service.createSubmittedOrder(new PayOrderSubmission(
                "30212026032100000000000000000002",
                "TRD202603210099",
                "BIZ202603210099",
                "FUND_ACCOUNT",
                "FUNDTXN202603210099",
                1,
                null,
                "FUND_SUBSCRIBE",
                880100068483692100L,
                880100068483692100L,
                money("500.00"),
                money("0.00"),
                money("500.00"),
                money("0.00"),
                money("0.00"),
                money("0.00"),
                null,
                "{\"originalAmount\":\"500.00\"}",
                "pay-global:30212026032100000000000000000002",
                LocalDateTime.of(2026, 3, 21, 20, 5)
        ));

        assertEquals("TRD202603210099", order.getTradeOrderNo());
        assertEquals("BIZ202603210099", order.getBizOrderNo());
    }

    @Test
    void createSubmittedOrderShouldKeepTradeOrderNoEmptyWhenNoUpstreamTradeExists() {
        PayOrder order = service.createSubmittedOrder(new PayOrderSubmission(
                "30212026032100000000000000000003",
                null,
                "BIZ202603210100",
                "INBOUND",
                "INB202603210100",
                1,
                null,
                "RECHARGE",
                880100068483692100L,
                880100068483692100L,
                money("66.00"),
                money("0.00"),
                money("0.00"),
                money("0.00"),
                money("66.00"),
                money("0.00"),
                null,
                "{\"originalAmount\":\"66.00\"}",
                "pay-global:30212026032100000000000000000003",
                LocalDateTime.of(2026, 3, 21, 20, 10)
        ));

        assertNull(order.getTradeOrderNo());
        assertEquals("BIZ202603210100", order.getBizOrderNo());
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
