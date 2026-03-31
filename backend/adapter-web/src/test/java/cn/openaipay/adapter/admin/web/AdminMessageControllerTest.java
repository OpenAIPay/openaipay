package cn.openaipay.adapter.admin.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.common.config.JacksonMoneyConfig;
import cn.openaipay.application.adminmessage.dto.AdminConversationDetailDTO;
import cn.openaipay.application.adminmessage.dto.AdminConversationMemberRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminConversationRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminMessageOverviewDTO;
import cn.openaipay.application.adminmessage.dto.AdminMessageRowDTO;
import cn.openaipay.application.adminmessage.facade.AdminMessageManageFacade;
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
 * 消息中心控制器测试
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@ExtendWith(MockitoExtension.class)
class AdminMessageControllerTest {

    /** 消息中心门面 */
    @Mock
    private AdminMessageManageFacade adminMessageManageFacade;

    /** 控制器 */
    private AdminMessageController controller;
    /** 映射器 */
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        controller = new AdminMessageController(adminMessageManageFacade);
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new JacksonMoneyConfig().moneyModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void overviewShouldReturnStableResponseFields() throws Exception {
        when(adminMessageManageFacade.overview()).thenReturn(new AdminMessageOverviewDTO(9, 18, 3, 1, 7, 2));

        ApiResponse<AdminMessageOverviewDTO> response = controller.overview();

        verify(adminMessageManageFacade).overview();
        assertEquals(true, response.success());

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));
        assertThat(json.at("/data/conversationCount").asInt()).isEqualTo(9);
        assertThat(json.at("/data/messageCount").asInt()).isEqualTo(18);
        assertThat(json.at("/data/redPacketCount").asInt()).isEqualTo(3);
        assertThat(json.at("/data/pendingContactRequestCount").asInt()).isEqualTo(1);
        assertThat(json.at("/data/friendshipCount").asInt()).isEqualTo(7);
        assertThat(json.at("/data/blacklistCount").asInt()).isEqualTo(2);
    }

    @Test
    void conversationDetailShouldKeepExistingJsonFieldNames() throws Exception {
        AdminConversationDetailDTO dto = new AdminConversationDetailDTO(
                new AdminConversationRowDTO(
                        "CONV202603210001",
                        "PRIVATE",
                        "CHAT",
                        "你好",
                        LocalDateTime.of(2026, 3, 21, 11, 0),
                        2L,
                        null,
                        null,
                        null,
                        null
                ),
                List.of(new AdminConversationMemberRowDTO(
                        "CONV202603210001",
                        880100068483692100L,
                        "顾郡",
                        "gujun001",
                        880100068483692101L,
                        "祁欣",
                        "qixin001",
                        3L,
                        "MSG202603210003",
                        LocalDateTime.of(2026, 3, 21, 10, 58),
                        false,
                        true,
                        LocalDateTime.of(2026, 3, 21, 10, 59)
                )),
                List.of(new AdminMessageRowDTO(
                        "MSG202603210003",
                        "CONV202603210001",
                        880100068483692100L,
                        "顾郡",
                        880100068483692101L,
                        "祁欣",
                        "TRANSFER",
                        "[转账]",
                        null,
                        money("88.66"),
                        "TRD202603210001",
                        "SENT",
                        "{\"memo\":\"chat\"}",
                        LocalDateTime.of(2026, 3, 21, 10, 57),
                        LocalDateTime.of(2026, 3, 21, 10, 57)
                ))
        );
        when(adminMessageManageFacade.getConversationDetail("CONV202603210001")).thenReturn(dto);

        ApiResponse<AdminConversationDetailDTO> response = controller.getConversationDetail("CONV202603210001");

        verify(adminMessageManageFacade).getConversationDetail("CONV202603210001");
        assertEquals("CONV202603210001", response.data().conversation().conversationNo());

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));
        assertThat(json.at("/data/conversation/conversationNo").asText()).isEqualTo("CONV202603210001");
        assertThat(json.at("/data/conversation/lastMessagePreview").asText()).isEqualTo("你好");
        assertThat(json.at("/data/members/0/userDisplayName").asText()).isEqualTo("顾郡");
        assertThat(json.at("/data/members/0/peerAipayUid").asText()).isEqualTo("qixin001");
        assertThat(json.at("/data/recentMessages/0/messageId").asText()).isEqualTo("MSG202603210003");
        assertThat(json.at("/data/recentMessages/0/senderDisplayName").asText()).isEqualTo("顾郡");
        assertThat(json.at("/data/recentMessages/0/amount/amount").asText()).isEqualTo("88.66");
        assertThat(json.at("/data/recentMessages/0/tradeOrderNo").asText()).isEqualTo("TRD202603210001");
        assertThat(json.at("/data/recentMessages/0/messageStatus").asText()).isEqualTo("SENT");
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
