package cn.openaipay.infrastructure.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.domain.message.model.RedPacketOrder;
import cn.openaipay.domain.message.model.RedPacketOrderStatus;
import cn.openaipay.infrastructure.message.dataobject.RedPacketOrderDO;
import cn.openaipay.infrastructure.message.mapper.RedPacketOrderMapper;
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
 * RedPacketOrderRepositoryImplTest 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@ExtendWith(MockitoExtension.class)
class RedPacketOrderRepositoryImplTest {

    /** RED订单信息 */
    @Mock
    private RedPacketOrderMapper redPacketOrderMapper;

    /** 仓储信息 */
    private RedPacketOrderRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new RedPacketOrderRepositoryImpl(redPacketOrderMapper);
    }

    @Test
    void findByRedPacketNoShouldMapPersistenceEntityIntoDomainModel() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 15, 10, 0);
        LocalDateTime claimedAt = LocalDateTime.of(2026, 3, 15, 10, 30);
        RedPacketOrderDO entity = new RedPacketOrderDO();
        entity.setRedPacketNo("RPK202603150001");
        entity.setMessageId("MSG202603150001");
        entity.setConversationNo("CONV202603150001");
        entity.setSenderUserId(880100068483692100L);
        entity.setReceiverUserId(880100068483692101L);
        entity.setHoldingUserId(880231069206400031L);
        entity.setAmount(money("18.88"));
        entity.setFundingTradeNo("TRD_FUND_001");
        entity.setClaimTradeNo("TRD_CLAIM_001");
        entity.setPaymentMethod("WALLET");
        entity.setCoverId("cover-1");
        entity.setCoverTitle("好运红包");
        entity.setBlessingText("周末快乐");
        entity.setStatus("claimed");
        entity.setClaimedAt(claimedAt);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(claimedAt);
        when(redPacketOrderMapper.findByRedPacketNo("RPK202603150001")).thenReturn(Optional.of(entity));

        Optional<RedPacketOrder> result = repository.findByRedPacketNo("RPK202603150001");

        assertTrue(result.isPresent());
        assertEquals("MSG202603150001", result.get().getMessageId());
        assertEquals("CONV202603150001", result.get().getConversationNo());
        assertEquals(money("18.88"), result.get().getAmount());
        assertEquals(RedPacketOrderStatus.CLAIMED, result.get().getStatus());
        assertEquals(claimedAt, result.get().getClaimedAt());
        assertEquals(createdAt, result.get().getCreatedAt());
    }

    @Test
    void saveShouldPopulateNewEntityUsingDomainFields() {
        RedPacketOrder pending = RedPacketOrder.createPending(
                "RPK202603150002",
                "MSG202603150002",
                "CONV202603150002",
                880100068483692100L,
                880100068483692101L,
                880231069206400031L,
                money("6.66"),
                "TRD_FUND_002",
                "BANK_CARD",
                "cover-2",
                "春日红包",
                "好运常在",
                LocalDateTime.of(2026, 3, 15, 11, 0)
        );
        when(redPacketOrderMapper.findByRedPacketNo("RPK202603150002")).thenReturn(Optional.empty());
        when(redPacketOrderMapper.save(any(RedPacketOrderDO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RedPacketOrder saved = repository.save(pending);

        ArgumentCaptor<RedPacketOrderDO> captor = ArgumentCaptor.forClass(RedPacketOrderDO.class);
        verify(redPacketOrderMapper).save(captor.capture());
        RedPacketOrderDO persisted = captor.getValue();
        assertEquals("MSG202603150002", persisted.getMessageId());
        assertEquals("BANK_CARD", persisted.getPaymentMethod());
        assertEquals("PENDING_CLAIM", persisted.getStatus());
        assertEquals("春日红包", persisted.getCoverTitle());
        assertEquals(pending.getCreatedAt(), persisted.getCreatedAt());
        assertNotNull(persisted.getUpdatedAt());

        assertEquals("RPK202603150002", saved.getRedPacketNo());
        assertEquals(RedPacketOrderStatus.PENDING_CLAIM, saved.getStatus());
        assertEquals("BANK_CARD", saved.getPaymentMethod());
    }

    @Test
    void saveShouldKeepExistingCreatedAtWhenUpdatingClaimedOrder() {
        LocalDateTime originalCreatedAt = LocalDateTime.of(2026, 3, 15, 9, 0);
        RedPacketOrderDO existing = new RedPacketOrderDO();
        existing.setRedPacketNo("RPK202603150003");
        existing.setCreatedAt(originalCreatedAt);

        RedPacketOrder claimed = RedPacketOrder.createPending(
                "RPK202603150003",
                "MSG202603150003",
                "CONV202603150003",
                880100068483692100L,
                880100068483692101L,
                880231069206400031L,
                money("9.99"),
                "TRD_FUND_003",
                "WALLET",
                null,
                null,
                "恭喜发财",
                LocalDateTime.of(2026, 3, 15, 9, 30)
        );
        LocalDateTime claimedAt = LocalDateTime.of(2026, 3, 15, 12, 0);
        claimed.markClaimed(880100068483692101L, "TRD_CLAIM_003", claimedAt);

        when(redPacketOrderMapper.findByRedPacketNo("RPK202603150003")).thenReturn(Optional.of(existing));
        when(redPacketOrderMapper.save(any(RedPacketOrderDO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RedPacketOrder saved = repository.save(claimed);

        ArgumentCaptor<RedPacketOrderDO> captor = ArgumentCaptor.forClass(RedPacketOrderDO.class);
        verify(redPacketOrderMapper).save(captor.capture());
        RedPacketOrderDO persisted = captor.getValue();
        assertEquals(originalCreatedAt, persisted.getCreatedAt());
        assertEquals("CLAIMED", persisted.getStatus());
        assertEquals("TRD_CLAIM_003", persisted.getClaimTradeNo());
        assertEquals(claimedAt, persisted.getClaimedAt());
        assertEquals(originalCreatedAt, saved.getCreatedAt());
        assertEquals(RedPacketOrderStatus.CLAIMED, saved.getStatus());
        assertEquals("TRD_CLAIM_003", saved.getClaimTradeNo());
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
