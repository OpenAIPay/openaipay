package cn.openaipay.application.bill.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import cn.openaipay.application.bill.dto.BillEntryDTO;
import cn.openaipay.application.bill.dto.BillEntryPageDTO;
import cn.openaipay.application.bill.service.impl.BillServiceImpl;
import cn.openaipay.domain.message.model.RedPacketOrder;
import cn.openaipay.domain.message.repository.RedPacketOrderRepository;
import cn.openaipay.domain.pay.model.PayOrder;
import cn.openaipay.domain.pay.model.PaySplitPlan;
import cn.openaipay.domain.pay.repository.PayOrderRepository;
import cn.openaipay.domain.trade.model.TradeBusinessDomainCode;
import cn.openaipay.domain.trade.model.TradeBusinessIndex;
import cn.openaipay.domain.trade.model.TradeStatus;
import cn.openaipay.domain.trade.repository.TradeRepository;
import cn.openaipay.domain.user.model.UserAggregate;
import cn.openaipay.domain.user.model.UserProfile;
import cn.openaipay.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.mock;

/**
 * BillVisibilityFacadeTest 业务模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@ExtendWith(MockitoExtension.class)
class BillVisibilityFacadeTest {

    /** 交易仓储。 */
    @Mock
    private TradeRepository tradeRepository;
    /** 支付仓储。 */
    @Mock
    private PayOrderRepository payOrderRepository;
    /** 用户仓储。 */
    @Mock
    private UserRepository userRepository;
    /** 红包订单仓储。 */
    @Mock
    private RedPacketOrderRepository redPacketOrderRepository;

    /** 账单服务。 */
    private BillServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BillServiceImpl(tradeRepository, payOrderRepository, userRepository, redPacketOrderRepository);
        when(payOrderRepository.findLatestOrdersByTradeOrderNos(anyList())).thenReturn(null);
        when(payOrderRepository.findOrdersByBizOrderNos(anyList())).thenReturn(null);
        when(payOrderRepository.findOrdersByPayOrderNos(anyList())).thenReturn(null);
        when(payOrderRepository.findLatestOrdersBySourceBizNos(anyString(), anyList())).thenReturn(null);
        lenient().when(redPacketOrderRepository.findByClaimTradeNo(anyString())).thenReturn(Optional.empty());
        lenient().when(userRepository.findByUserId(org.mockito.ArgumentMatchers.anyLong())).thenReturn(Optional.empty());
    }

    @Test
    void queryUserBillEntriesShouldExcludeFailedTradeBusinessIndex() {
        Long userId = 880100068483692100L;
        LocalDateTime now = LocalDateTime.of(2026, 3, 21, 21, 5);

        when(tradeRepository.findRecentTradeBusinessIndexesByUserId(
                userId,
                "2026-03",
                TradeBusinessDomainCode.AICASH,
                20
        )).thenReturn(List.of(
                index("30922026032121050000000000000001", "FIS:AICASH:FAILED", TradeStatus.FAILED, now),
                index("30922026032121050000000000000002", "FIS:AICASH:SUCCESS", TradeStatus.SUCCEEDED, now.minusMinutes(1))
        ));
        when(tradeRepository.findRecentSucceededTradesByUserId(userId, 80)).thenReturn(List.of());

        List<BillEntryDTO> result = service.queryUserBillEntries(userId, "2026-03", "AICASH", 20);

        assertEquals(1, result.size());
        assertEquals("30922026032121050000000000000002", result.getFirst().tradeOrderNo());
        assertEquals("SUCCEEDED", result.getFirst().status());
        assertEquals("CREDIT", result.getFirst().direction());
        assertEquals("FUND", result.getFirst().tradeType());
    }

    @Test
    void queryUserBillEntriesPageShouldExcludeFailedTradeBusinessIndex() {
        Long userId = 880100068483692100L;
        LocalDateTime now = LocalDateTime.of(2026, 3, 21, 21, 10);

        when(tradeRepository.findTradeBusinessIndexesByUserId(
                userId,
                "2026-03",
                TradeBusinessDomainCode.AICASH,
                0,
                3
        )).thenReturn(List.of(
                index("30922026032121100000000000000001", "FIS:AICASH:FAILED", TradeStatus.FAILED, now),
                index("30922026032121100000000000000002", "FIS:AICASH:SUCCESS-1", TradeStatus.SUCCEEDED, now.minusMinutes(1)),
                index("30922026032121100000000000000003", "FIS:AICASH:SUCCESS-2", TradeStatus.SUCCEEDED, now.minusMinutes(2))
        ));

        BillEntryPageDTO page = service.queryUserBillEntriesPage(userId, "2026-03", "AICASH", 1, 2, null, null);

        assertEquals(2, page.items().size());
        assertEquals("30922026032121100000000000000002", page.items().get(0).tradeOrderNo());
        assertEquals("30922026032121100000000000000003", page.items().get(1).tradeOrderNo());
        assertFalse(page.hasMore());
    }

    @Test
    void queryUserBillEntriesPageShouldSupportCursorPaging() {
        Long userId = 880100068483692100L;
        LocalDateTime cursorTradeTime = LocalDateTime.of(2026, 3, 21, 21, 12, 0);
        LocalDateTime now = LocalDateTime.of(2026, 3, 21, 21, 10, 0);
        when(tradeRepository.findTradeBusinessIndexesByUserIdAfterCursor(
                userId,
                "2026-03",
                TradeBusinessDomainCode.AICASH,
                cursorTradeTime,
                99L,
                3
        )).thenReturn(List.of(
                indexWithId(120L, "30922026032121100000000000001001", "FIS:AICASH:SUCCESS-1", TradeStatus.SUCCEEDED, now),
                indexWithId(119L, "30922026032121100000000000001002", "FIS:AICASH:SUCCESS-2", TradeStatus.SUCCEEDED, now.minusMinutes(1)),
                indexWithId(118L, "30922026032121100000000000001003", "FIS:AICASH:SUCCESS-3", TradeStatus.SUCCEEDED, now.minusMinutes(2))
        ));

        BillEntryPageDTO page = service.queryUserBillEntriesPage(
                userId,
                "2026-03",
                "AICASH",
                1,
                2,
                "2026-03-21 21:12:00",
                99L
        );

        assertEquals(2, page.items().size());
        assertTrue(page.hasMore());
        assertEquals("2026-03-21 21:09:00", page.nextCursorTradeTime());
        assertEquals(119L, page.nextCursorId());
    }

    @Test
    void queryUserBillEntriesShouldReturnExpectedDirectionAndTradeTypeForMainScenes() {
        Long userId = 880100068483692100L;
        LocalDateTime now = LocalDateTime.of(2026, 3, 22, 10, 0);
        when(tradeRepository.findRecentTradeBusinessIndexesByUserId(
                userId,
                "2026-03",
                null,
                20
        )).thenReturn(List.of(
                indexWithScenario(
                        "TRADE_TOPUP_0001",
                        TradeBusinessDomainCode.TRADE,
                        "TRADE",
                        "DEPOSIT",
                        "余额充值",
                        Money.of(CurrencyUnit.of("CNY"), new BigDecimal("88.00")),
                        TradeStatus.SUCCEEDED,
                        now
                ),
                indexWithScenario(
                        "TRADE_TRANSFER_0001",
                        TradeBusinessDomainCode.TRADE,
                        "TRADE",
                        "TRANSFER",
                        "向张三转账",
                        Money.of(CurrencyUnit.of("CNY"), new BigDecimal("18.80")),
                        TradeStatus.SUCCEEDED,
                        now.minusMinutes(1)
                ),
                indexWithScenario(
                        "TRADE_REFUND_0001",
                        TradeBusinessDomainCode.TRADE,
                        "TRADE",
                        "ORDER_REFUND",
                        "订单退款",
                        Money.of(CurrencyUnit.of("CNY"), new BigDecimal("6.66")),
                        TradeStatus.SUCCEEDED,
                        now.minusMinutes(2)
                ),
                indexWithScenario(
                        "LOAN_REPAY_0001",
                        TradeBusinessDomainCode.AILOAN,
                        "AILOAN",
                        "APP_LOAN_ACCOUNT_CREDIT_REPAY",
                        "爱借还款",
                        Money.of(CurrencyUnit.of("CNY"), new BigDecimal("188.00")),
                        TradeStatus.SUCCEEDED,
                        now.minusMinutes(3)
                ),
                indexWithScenario(
                        "FUND_SUBSCRIBE_0001",
                        TradeBusinessDomainCode.AICASH,
                        "AICASH",
                        "FUND_SUBSCRIBE",
                        "爱存-单次转入",
                        Money.of(CurrencyUnit.of("CNY"), new BigDecimal("520.00")),
                        TradeStatus.SUCCEEDED,
                        now.minusMinutes(4)
                )
        ));
        when(tradeRepository.findRecentSucceededTradesByUserId(userId, 80)).thenReturn(List.of());

        List<BillEntryDTO> result = service.queryUserBillEntries(userId, "2026-03", null, 20);
        Map<String, BillEntryDTO> entryByTradeNo = result.stream()
                .collect(Collectors.toMap(BillEntryDTO::tradeOrderNo, entry -> entry));

        assertEquals("DEPOSIT", entryByTradeNo.get("TRADE_TOPUP_0001").tradeType());
        assertEquals("CREDIT", entryByTradeNo.get("TRADE_TOPUP_0001").direction());

        assertEquals("TRANSFER", entryByTradeNo.get("TRADE_TRANSFER_0001").tradeType());
        assertEquals("DEBIT", entryByTradeNo.get("TRADE_TRANSFER_0001").direction());

        assertEquals("REFUND", entryByTradeNo.get("TRADE_REFUND_0001").tradeType());
        assertEquals("CREDIT", entryByTradeNo.get("TRADE_REFUND_0001").direction());

        assertEquals("LOAN", entryByTradeNo.get("LOAN_REPAY_0001").tradeType());
        assertEquals("DEBIT", entryByTradeNo.get("LOAN_REPAY_0001").direction());

        assertEquals("FUND", entryByTradeNo.get("FUND_SUBSCRIBE_0001").tradeType());
        assertEquals("CREDIT", entryByTradeNo.get("FUND_SUBSCRIBE_0001").direction());
    }

    @Test
    void queryUserBillEntriesShouldMapAiCashIncomeSettleToExplicitBillSemantics() {
        Long userId = 880100068483692100L;
        LocalDateTime now = LocalDateTime.of(2026, 3, 23, 2, 0);
        when(tradeRepository.findRecentTradeBusinessIndexesByUserId(
                userId,
                "2026-03",
                TradeBusinessDomainCode.AICASH,
                20
        )).thenReturn(List.of(
                indexWithScenario(
                        "AICASH_INCOME_SETTLE_0001",
                        TradeBusinessDomainCode.AICASH,
                        "AICASH",
                        "YIELD_SETTLE",
                        "FUND_INCOME_SETTLE",
                        Money.of(CurrencyUnit.of("CNY"), new BigDecimal("10.19")),
                        TradeStatus.SUCCEEDED,
                        now
                )
        ));
        when(tradeRepository.findRecentSucceededTradesByUserId(userId, 80)).thenReturn(List.of());

        List<BillEntryDTO> result = service.queryUserBillEntries(userId, "2026-03", "AICASH", 20);

        assertEquals(1, result.size());
        BillEntryDTO entry = result.getFirst();
        assertEquals("AICASH_INCOME_SETTLE_0001", entry.tradeOrderNo());
        assertEquals("爱存收益发放", entry.displayTitle());
        assertEquals("投资理财", entry.displaySubtitle());
        assertEquals("FUND", entry.tradeType());
        assertEquals("CREDIT", entry.direction());
        assertEquals("10.19", entry.amount());
    }

    @Test
    void queryUserBillEntriesShouldMapAiCashRedeemToDestinationAwareTitles() {
        Long userId = 880100068483692100L;
        LocalDateTime now = LocalDateTime.of(2026, 3, 23, 11, 30);
        when(tradeRepository.findRecentTradeBusinessIndexesByUserId(
                userId,
                "2026-03",
                TradeBusinessDomainCode.AICASH,
                20
        )).thenReturn(List.of(
                indexWithScenario(
                        "AICASH_REDEEM_BALANCE_0001",
                        TradeBusinessDomainCode.AICASH,
                        "AICASH",
                        "FUND_FAST_REDEEM",
                        "FUND_FAST_REDEEM",
                        "FUND_ACCOUNT",
                        Money.of(CurrencyUnit.of("CNY"), new BigDecimal("500.00")),
                        TradeStatus.SUCCEEDED,
                        now
                ),
                indexWithScenario(
                        "AICASH_REDEEM_BANK_0001",
                        TradeBusinessDomainCode.AICASH,
                        "AICASH",
                        "FUND_REDEEM",
                        "FUND_REDEEM",
                        "招商银行(尾号8888)",
                        Money.of(CurrencyUnit.of("CNY"), new BigDecimal("800.00")),
                        TradeStatus.SUCCEEDED,
                        now.minusMinutes(1)
                ),
                indexWithScenario(
                        "AICASH_REDEEM_BANK_GENERIC_0001",
                        TradeBusinessDomainCode.AICASH,
                        "AICASH",
                        "FUND_FAST_REDEEM",
                        "FUND_FAST_REDEEM",
                        "BANK_CARD",
                        Money.of(CurrencyUnit.of("CNY"), new BigDecimal("300.00")),
                        TradeStatus.SUCCEEDED,
                        now.minusMinutes(2)
                ),
                indexWithScenario(
                        "AICASH_REDEEM_BANK_CARD_TEXT_0001",
                        TradeBusinessDomainCode.AICASH,
                        "AICASH",
                        "FUND_FAST_REDEEM",
                        "FUND_FAST_REDEEM",
                        "储蓄卡(尾号6666)",
                        Money.of(CurrencyUnit.of("CNY"), new BigDecimal("260.00")),
                        TradeStatus.SUCCEEDED,
                        now.minusMinutes(3)
                )
        ));
        when(tradeRepository.findRecentSucceededTradesByUserId(userId, 80)).thenReturn(List.of());

        List<BillEntryDTO> result = service.queryUserBillEntries(userId, "2026-03", "AICASH", 20);
        Map<String, BillEntryDTO> entryByTradeNo = result.stream()
                .collect(Collectors.toMap(BillEntryDTO::tradeOrderNo, entry -> entry));

        assertEquals("爱存转出至余额", entryByTradeNo.get("AICASH_REDEEM_BALANCE_0001").displayTitle());
        assertEquals("爱存转出至招商银行", entryByTradeNo.get("AICASH_REDEEM_BANK_0001").displayTitle());
        assertEquals("爱存转出至银行卡", entryByTradeNo.get("AICASH_REDEEM_BANK_GENERIC_0001").displayTitle());
        assertEquals("爱存转出至银行卡", entryByTradeNo.get("AICASH_REDEEM_BANK_CARD_TEXT_0001").displayTitle());
    }

    @Test
    void queryUserBillEntriesShouldMapRedPacketClaimToExplicitBillSemantics() {
        Long userId = 880100068483692100L;
        LocalDateTime now = LocalDateTime.of(2026, 3, 23, 3, 8);
        when(tradeRepository.findRecentTradeBusinessIndexesByUserId(
                userId,
                "2026-03",
                null,
                20
        )).thenReturn(List.of(
                indexWithScenario(
                        "RED_PACKET_CLAIM_0001",
                        TradeBusinessDomainCode.RED_PACKET,
                        "RED_PACKET",
                        "CHAT_RED_PACKET_CLAIM",
                        "收到来自阿楷的红包",
                        Money.of(CurrencyUnit.of("CNY"), new BigDecimal("8.88")),
                        TradeStatus.SUCCEEDED,
                        now
                )
        ));
        when(tradeRepository.findRecentSucceededTradesByUserId(userId, 80)).thenReturn(List.of());

        List<BillEntryDTO> result = service.queryUserBillEntries(userId, "2026-03", null, 20);

        assertEquals(1, result.size());
        BillEntryDTO entry = result.getFirst();
        assertEquals("RED_PACKET_CLAIM_0001", entry.tradeOrderNo());
        assertEquals("领取阿楷的红包", entry.displayTitle());
        assertEquals("红包", entry.displaySubtitle());
        assertEquals("RED_PACKET", entry.tradeType());
        assertEquals("CREDIT", entry.direction());
        assertEquals("8.88", entry.amount());
    }

    @Test
    void queryUserBillEntriesShouldResolveRedPacketClaimSenderFromClaimTradeWhenTitleUsesMiddleAccount() {
        Long userId = 880100068483692100L;
        Long senderUserId = 880100068483692101L;
        LocalDateTime now = LocalDateTime.of(2026, 3, 23, 3, 18);
        when(tradeRepository.findRecentTradeBusinessIndexesByUserId(
                userId,
                "2026-03",
                null,
                20
        )).thenReturn(List.of(
                indexWithScenario(
                        "RED_PACKET_CLAIM_0002",
                        TradeBusinessDomainCode.RED_PACKET,
                        "RED_PACKET",
                        "CHAT_RED_PACKET_CLAIM",
                        "领取爱付红包中间账户的红包",
                        Money.of(CurrencyUnit.of("CNY"), new BigDecimal("5.55")),
                        TradeStatus.SUCCEEDED,
                        now
                )
        ));
        when(tradeRepository.findRecentSucceededTradesByUserId(userId, 80)).thenReturn(List.of());
        RedPacketOrder order = mock(RedPacketOrder.class);
        when(order.getSenderUserId()).thenReturn(senderUserId);
        when(redPacketOrderRepository.findByClaimTradeNo("RED_PACKET_CLAIM_0002")).thenReturn(Optional.of(order));
        when(userRepository.findByUserId(senderUserId)).thenReturn(Optional.of(mockUser(senderUserId, "林泽楷")));

        List<BillEntryDTO> result = service.queryUserBillEntries(userId, "2026-03", null, 20);

        assertEquals(1, result.size());
        assertEquals("领取林泽楷的红包", result.getFirst().displayTitle());
    }

    @Test
    void queryUserBillEntriesShouldFillCouponDiscountFromPayOrderByTradeOrderNo() {
        Long userId = 880100068483692100L;
        LocalDateTime now = LocalDateTime.of(2026, 3, 22, 12, 0);
        when(tradeRepository.findRecentTradeBusinessIndexesByUserId(
                userId,
                "2026-03",
                TradeBusinessDomainCode.TRADE,
                20
        )).thenReturn(List.of(
                indexWithScenario(
                        "TRADE_MOBILE_TOPUP_0001",
                        TradeBusinessDomainCode.TRADE,
                        "TRADE",
                        "APP_MOBILE_HALL_TOP_UP",
                        "话费充值",
                        Money.of(CurrencyUnit.of("CNY"), new BigDecimal("30.00")),
                        TradeStatus.SUCCEEDED,
                        now
                )
        ));
        when(tradeRepository.findRecentSucceededTradesByUserId(userId, 80)).thenReturn(List.of());
        when(payOrderRepository.findLatestOrderByTradeOrderNo("TRADE_MOBILE_TOPUP_0001"))
                .thenReturn(Optional.of(mockPayOrder(
                        "PAY_MOBILE_TOPUP_0001",
                        "TRADE_MOBILE_TOPUP_0001",
                        "BIZ-TRADE_MOBILE_TOPUP_0001",
                        "3.00",
                        "COUPON_MOBILE_TOPUP_0001"
                )));

        List<BillEntryDTO> result = service.queryUserBillEntries(userId, "2026-03", "TRADE", 20);

        assertEquals(1, result.size());
        BillEntryDTO entry = result.getFirst();
        assertEquals("3.00", entry.couponDiscountAmount());
        assertEquals("3.00", entry.discountAmount());
        assertEquals("COUPON_MOBILE_TOPUP_0001", entry.couponNo());
    }

    @Test
    void queryUserBillEntriesShouldFillCouponDiscountByPayOrderNoFallback() {
        Long userId = 880100068483692100L;
        LocalDateTime now = LocalDateTime.of(2026, 3, 22, 13, 0);
        when(tradeRepository.findRecentTradeBusinessIndexesByUserId(
                userId,
                "2026-03",
                TradeBusinessDomainCode.AICREDIT,
                20
        )).thenReturn(List.of(
                new TradeBusinessIndex(
                        null,
                        "TRADE_MOBILE_TOPUP_0002",
                        TradeBusinessDomainCode.AICREDIT,
                        "PAY_MOBILE_TOPUP_0002",
                        "AICREDIT",
                        "CONSUME",
                        userId,
                        null,
                        "CA880100068483692100",
                        "HBILL-CA880100068483692100-202604",
                        "2026-03",
                        "APP_MOBILE_HALL_TOP_UP",
                        "CREDIT_ACCOUNT",
                        Money.of(CurrencyUnit.of("CNY"), new BigDecimal("100.00")),
                        TradeStatus.SUCCEEDED,
                        now,
                        now,
                        now
                )
        ));
        when(tradeRepository.findRecentSucceededTradesByUserId(userId, 80)).thenReturn(List.of());
        when(payOrderRepository.findLatestOrderByTradeOrderNo("TRADE_MOBILE_TOPUP_0002"))
                .thenReturn(Optional.empty());
        when(payOrderRepository.findOrderByBizOrderNo("PAY_MOBILE_TOPUP_0002"))
                .thenReturn(Optional.empty());
        when(payOrderRepository.findOrderByPayOrderNo("PAY_MOBILE_TOPUP_0002"))
                .thenReturn(Optional.of(mockPayOrder(
                        "PAY_MOBILE_TOPUP_0002",
                        "TRADE_MOBILE_TOPUP_PAY_0002",
                        "IOS-PAY-TRADE_MOBILE_TOPUP_0002",
                        "2.95",
                        "COUPON_MOBILE_TOPUP_0002"
                )));

        List<BillEntryDTO> result = service.queryUserBillEntries(userId, "2026-03", "AICREDIT", 20);

        assertEquals(1, result.size());
        BillEntryDTO entry = result.getFirst();
        assertEquals("2.95", entry.couponDiscountAmount());
        assertEquals("2.95", entry.discountAmount());
        assertEquals("COUPON_MOBILE_TOPUP_0002", entry.couponNo());
    }

    private TradeBusinessIndex index(String tradeOrderNo,
                                     String bizOrderNo,
                                     TradeStatus status,
                                     LocalDateTime tradeTime) {
        return indexWithId(null, tradeOrderNo, bizOrderNo, status, tradeTime);
    }

    private TradeBusinessIndex indexWithId(Long id,
                                           String tradeOrderNo,
                                           String bizOrderNo,
                                           TradeStatus status,
                                           LocalDateTime tradeTime) {
        return new TradeBusinessIndex(
                id,
                tradeOrderNo,
                TradeBusinessDomainCode.AICASH,
                bizOrderNo,
                "AICASH",
                "FUND_SUBSCRIBE",
                880100068483692100L,
                null,
                "FUNDACC-880100068483692100-AICASH",
                "AICASHBILL-FUNDACC-880100068483692100-AICASH-202603",
                "2026-03",
                "爱存-单次转入",
                "账户余额",
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("500.00")),
                status,
                tradeTime,
                tradeTime,
                tradeTime
        );
    }

    private TradeBusinessIndex indexWithScenario(String tradeOrderNo,
                                                 TradeBusinessDomainCode businessDomainCode,
                                                 String productType,
                                                 String businessType,
                                                 String displayTitle,
                                                 Money amount,
                                                 TradeStatus status,
                                                 LocalDateTime tradeTime) {
        return indexWithScenario(
                tradeOrderNo,
                businessDomainCode,
                productType,
                businessType,
                displayTitle,
                "测试账单",
                amount,
                status,
                tradeTime
        );
    }

    private TradeBusinessIndex indexWithScenario(String tradeOrderNo,
                                                 TradeBusinessDomainCode businessDomainCode,
                                                 String productType,
                                                 String businessType,
                                                 String displayTitle,
                                                 String displaySubtitle,
                                                 Money amount,
                                                 TradeStatus status,
                                                 LocalDateTime tradeTime) {
        return new TradeBusinessIndex(
                null,
                tradeOrderNo,
                businessDomainCode,
                "BIZ-" + tradeOrderNo,
                productType,
                businessType,
                880100068483692100L,
                null,
                "ACC-" + tradeOrderNo,
                "BILL-" + tradeOrderNo,
                "2026-03",
                displayTitle,
                displaySubtitle,
                amount,
                status,
                tradeTime,
                tradeTime,
                tradeTime
        );
    }

    private PayOrder mockPayOrder(String payOrderNo,
                                  String tradeOrderNo,
                                  String bizOrderNo,
                                  String discountAmount,
                                  String couponNo) {
        CurrencyUnit currency = CurrencyUnit.of("CNY");
        Money originalAmount = Money.of(currency, new BigDecimal("30.00"));
        Money discount = Money.of(currency, new BigDecimal(discountAmount));
        return PayOrder.createSubmitted(
                payOrderNo,
                tradeOrderNo,
                bizOrderNo,
                "TRADE",
                bizOrderNo,
                1,
                null,
                "APP_MOBILE_HALL_TOP_UP",
                880100068483692100L,
                880100068483692101L,
                originalAmount,
                discount,
                originalAmount.minus(discount),
                PaySplitPlan.of(currency, originalAmount.minus(discount), Money.zero(currency), Money.zero(currency), Money.zero(currency)),
                couponNo,
                null,
                "TXID-" + payOrderNo,
                LocalDateTime.of(2026, 3, 22, 12, 0)
        );
    }

    private UserAggregate mockUser(Long userId, String nickname) {
        UserProfile profile = new UserProfile(
                userId,
                nickname,
                null,
                "86",
                null,
                null,
                null,
                "UNKNOWN",
                "CN",
                null,
                LocalDateTime.of(2026, 3, 1, 0, 0),
                LocalDateTime.of(2026, 3, 1, 0, 0)
        );
        return new UserAggregate(null, profile, null, null);
    }
}
