package cn.openaipay.adapter.message.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.message.web.request.ClaimRedPacketRequest;
import cn.openaipay.adapter.message.web.request.SendTransferMessageRequest;
import cn.openaipay.application.message.dto.MessageDTO;
import cn.openaipay.application.message.dto.RedPacketDetailDTO;
import cn.openaipay.application.message.facade.MessageFacade;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * MessageControllerTest 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    /** 消息信息 */
    @Mock
    private MessageFacade messageFacade;

    /** controller信息 */
    private MessageController controller;

    @BeforeEach
    void setUp() {
        controller = new MessageController(messageFacade);
    }

    @Test
    void sendTransferShouldMapRequestIntoCommandAndReturnFacadeResult() {
        MessageDTO dto = new MessageDTO(
                "MSG202603150001",
                "CONV202603150001",
                880100068483692100L,
                880100068483692101L,
                "TRANSFER",
                "[转账]",
                null,
                money("88.88"),
                "TRD202603150001",
                "{\"memo\":\"chat\"}",
                "SENT",
                LocalDateTime.of(2026, 3, 15, 12, 0)
        );
        when(messageFacade.sendTransferMessage(any())).thenReturn(dto);

        ApiResponse<MessageDTO> response = controller.sendTransfer(new SendTransferMessageRequest(
                880100068483692100L,
                880100068483692101L,
                money("88.88"),
                "BANK_CARD",
                "CARD_TOOL_ICBC_9005",
                "房租",
                "{\"memo\":\"chat\"}"
        ));

        ArgumentCaptor<cn.openaipay.application.message.command.SendTransferMessageCommand> commandCaptor =
                ArgumentCaptor.forClass(cn.openaipay.application.message.command.SendTransferMessageCommand.class);
        verify(messageFacade).sendTransferMessage(commandCaptor.capture());
        assertEquals(880100068483692100L, commandCaptor.getValue().senderUserId());
        assertEquals(880100068483692101L, commandCaptor.getValue().receiverUserId());
        assertEquals("BANK_CARD", commandCaptor.getValue().paymentMethod());
        assertEquals("CARD_TOOL_ICBC_9005", commandCaptor.getValue().paymentToolCode());
        assertEquals("房租", commandCaptor.getValue().remark());
        assertEquals("{\"memo\":\"chat\"}", commandCaptor.getValue().extPayload());
        assertEquals(true, response.success());
        assertEquals("MSG202603150001", response.data().messageId());
    }

    @Test
    void claimRedPacketShouldUsePathVariableAndBodyUserId() {
        RedPacketDetailDTO dto = new RedPacketDetailDTO(
                "RPK202603150001",
                "MSG202603150001",
                "CONV202603150001",
                880100068483692100L,
                "顾郡",
                "/api/media/gujun.png",
                880100068483692101L,
                "祁欣",
                "/api/media/qixin.png",
                880231069206400031L,
                money("6.66"),
                "WALLET",
                "cover-1",
                "好运红包",
                "周末快乐",
                "CLAIMED",
                "TRD_FUND_001",
                "TRD_CLAIM_001",
                false,
                true,
                LocalDateTime.of(2026, 3, 15, 12, 30),
                LocalDateTime.of(2026, 3, 15, 12, 0)
        );
        when(messageFacade.claimRedPacket(880100068483692101L, "RPK202603150001")).thenReturn(dto);

        ApiResponse<RedPacketDetailDTO> response =
                controller.claimRedPacket("RPK202603150001", new ClaimRedPacketRequest(880100068483692101L));

        verify(messageFacade).claimRedPacket(880100068483692101L, "RPK202603150001");
        assertEquals(true, response.success());
        assertEquals("CLAIMED", response.data().status());
    }

    private Money money(String amount) {
        return Money.of(CurrencyUnit.of("CNY"), new BigDecimal(amount));
    }
}
