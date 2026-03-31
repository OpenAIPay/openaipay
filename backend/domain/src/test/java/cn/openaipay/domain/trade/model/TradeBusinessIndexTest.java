package cn.openaipay.domain.trade.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradeBusinessIndexTest {

    @Test
    void fromTradeOrder_shouldUseRealMobileForMobileTopUpDisplayTitle_whenMetadataContainsMobile() {
        TradeOrder tradeOrder = createTradeOrder(
                "APP_MOBILE_HALL_TOP_UP",
                "entry=mobile-hall-top-up;operator=CHINA_MOBILE;product=PHONE_BILL;mobile=13912345678"
        );

        TradeBusinessIndex index = TradeBusinessIndex.fromTradeOrder(tradeOrder);

        assertEquals("为13912345678话费充值", index.getDisplayTitle());
    }

    @Test
    void fromTradeOrder_shouldFallbackToSceneCode_whenMobileTopUpMetadataMissingMobile() {
        TradeOrder tradeOrder = createTradeOrder(
                "APP_MOBILE_HALL_TOP_UP",
                "entry=mobile-hall-top-up;operator=CHINA_MOBILE;product=PHONE_BILL"
        );

        TradeBusinessIndex index = TradeBusinessIndex.fromTradeOrder(tradeOrder);

        assertEquals("APP_MOBILE_HALL_TOP_UP", index.getDisplayTitle());
    }

    @Test
    void fromTradeOrder_shouldKeepSceneCode_whenNotMobileTopUpScene() {
        TradeOrder tradeOrder = createTradeOrder(
                "APP_TRANSFER",
                "entry=transfer;mobile=13912345678"
        );

        TradeBusinessIndex index = TradeBusinessIndex.fromTradeOrder(tradeOrder);

        assertEquals("APP_TRANSFER", index.getDisplayTitle());
    }

    private TradeOrder createTradeOrder(String sceneCode, String metadata) {
        LocalDateTime now = LocalDateTime.of(2026, 3, 28, 10, 0, 0);
        return TradeOrder.create(
                "30922026032800000000000000000001",
                "REQ-20260328-0001",
                TradeType.PAY,
                sceneCode,
                null,
                880109000000000001L,
                880201069206400001L,
                "BANK_CARD",
                Money.of(CurrencyUnit.of("CNY"), new BigDecimal("50.00")),
                metadata,
                now
        );
    }
}

