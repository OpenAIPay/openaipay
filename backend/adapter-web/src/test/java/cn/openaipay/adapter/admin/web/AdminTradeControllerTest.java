package cn.openaipay.adapter.admin.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.common.config.JacksonMoneyConfig;
import cn.openaipay.application.trade.dto.TradeOrderDTO;
import cn.openaipay.application.trade.facade.TradeFacade;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 后台交易控制器测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@ExtendWith(MockitoExtension.class)
class AdminTradeControllerTest {

    /** 交易门面。 */
    @Mock
    private TradeFacade tradeFacade;

    /** 控制器。 */
    private AdminTradeController controller;
    /** 映射器。 */
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        controller = new AdminTradeController(tradeFacade);
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new JacksonMoneyConfig().moneyModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void queryByTradeOrderNoShouldKeepResponseFieldNames() throws Exception {
        when(tradeFacade.queryByTradeOrderNo("TRD202603210001")).thenReturn(tradeOrder("TRD202603210001", "REQ202603210001"));

        ApiResponse<TradeOrderDTO> response = controller.queryByTradeOrderNo("TRD202603210001");

        verify(tradeFacade).queryByTradeOrderNo("TRD202603210001");
        assertEquals("TRD202603210001", response.data().tradeOrderNo());
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));
        assertThat(json.at("/data/requestNo").asText()).isEqualTo("REQ202603210001");
        assertThat(json.at("/data/businessSceneCode").asText()).isEqualTo("CHAT_TRANSFER");
        assertThat(json.at("/data/paymentMethod").asText()).isEqualTo("WALLET");
        assertThat(json.at("/data/originalAmount/amount").asText()).isEqualTo("88.88");
        assertThat(json.at("/data/status").asText()).isEqualTo("SUCCEEDED");
    }

    @Test
    void queryByBusinessOrderShouldPassBothPathVariables() {
        when(tradeFacade.queryByBusinessOrder("AICASH", "BIZ202603210001"))
                .thenReturn(tradeOrder("TRD202603210002", "REQ202603210002"));

        ApiResponse<TradeOrderDTO> response = controller.queryByBusinessOrder("AICASH", "BIZ202603210001");

        verify(tradeFacade).queryByBusinessOrder("AICASH", "BIZ202603210001");
        assertEquals(true, response.success());
        assertEquals("TRD202603210002", response.data().tradeOrderNo());
    }

    private TradeOrderDTO tradeOrder(String tradeOrderNo, String requestNo) {
        return new TradeOrderDTO(
                tradeOrderNo,
                requestNo,
                "TRANSFER",
                "CHAT_TRANSFER",
                "WALLET",
                "BIZ202603210001",
                null,
                880109000000000001L,
                880109000000000002L,
                "WALLET",
                money("88.88"),
                money("0.00"),
                money("88.88"),
                money("88.88"),
                null,
                null,
                "PAY202603210001",
                1,
                1,
                "SUCCESS",
                "success",
                1,
                "SUCCEEDED",
                null,
                null,
                null,
                LocalDateTime.of(2026, 3, 21, 10, 0),
                LocalDateTime.of(2026, 3, 21, 10, 1),
                List.of(),
                List.of()
        );
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
