package cn.openaipay.domain.message.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;

/**
 * RedPacketOrderTest 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
class RedPacketOrderTest {

    @Test
    void createPendingShouldInitializeClaimState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 15, 12, 0);

        RedPacketOrder order = RedPacketOrder.createPending(
                "RPK202603150001",
                "MSG202603150001",
                "CONV202603150001",
                880100068483692100L,
                880100068483692101L,
                880231069206400031L,
                money("8.88"),
                "TRD_FUND_001",
                "WALLET",
                "cover-1",
                "好运来",
                "周末快乐",
                createdAt
        );

        assertEquals("RPK202603150001", order.getRedPacketNo());
        assertEquals(RedPacketOrderStatus.PENDING_CLAIM, order.getStatus());
        assertEquals("TRD_FUND_001", order.getFundingTradeNo());
        assertEquals("WALLET", order.getPaymentMethod());
        assertNull(order.getClaimTradeNo());
        assertNull(order.getClaimedAt());
        assertEquals(createdAt, order.getCreatedAt());
        assertEquals(createdAt, order.getUpdatedAt());
    }

    @Test
    void markClaimedShouldUpdateStatusTradeAndTimestamps() {
        RedPacketOrder order = pendingOrder();
        LocalDateTime claimedAt = LocalDateTime.of(2026, 3, 15, 12, 30);

        order.markClaimed(880100068483692101L, "TRD_CLAIM_001", claimedAt);

        assertEquals(RedPacketOrderStatus.CLAIMED, order.getStatus());
        assertEquals("TRD_CLAIM_001", order.getClaimTradeNo());
        assertEquals(claimedAt, order.getClaimedAt());
        assertEquals(claimedAt, order.getUpdatedAt());
    }

    @Test
    void markClaimedShouldBeIdempotentWhenAlreadyClaimed() {
        RedPacketOrder order = pendingOrder();
        LocalDateTime firstClaimAt = LocalDateTime.of(2026, 3, 15, 12, 30);
        order.markClaimed(880100068483692101L, "TRD_CLAIM_001", firstClaimAt);

        order.markClaimed(880100068483692101L, "TRD_CLAIM_002", LocalDateTime.of(2026, 3, 15, 13, 0));

        assertEquals(RedPacketOrderStatus.CLAIMED, order.getStatus());
        assertEquals("TRD_CLAIM_001", order.getClaimTradeNo());
        assertEquals(firstClaimAt, order.getClaimedAt());
    }

    @Test
    void markClaimedShouldRejectNonReceiver() {
        RedPacketOrder order = pendingOrder();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> order.markClaimed(880100068483692199L, "TRD_CLAIM_001", LocalDateTime.now()));

        assertEquals("current user is not the red packet receiver", exception.getMessage());
    }

    private RedPacketOrder pendingOrder() {
        return RedPacketOrder.createPending(
                "RPK202603150001",
                "MSG202603150001",
                "CONV202603150001",
                880100068483692100L,
                880100068483692101L,
                880231069206400031L,
                money("8.88"),
                "TRD_FUND_001",
                "WALLET",
                "cover-1",
                "好运来",
                "周末快乐",
                LocalDateTime.of(2026, 3, 15, 12, 0)
        );
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
