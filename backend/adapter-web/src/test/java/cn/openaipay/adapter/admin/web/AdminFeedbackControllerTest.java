package cn.openaipay.adapter.admin.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.adapter.admin.security.AdminRequestContext;
import cn.openaipay.adapter.admin.web.request.HandleFeedbackTicketRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.feedback.command.HandleFeedbackCommand;
import cn.openaipay.application.feedback.dto.FeedbackTicketDTO;
import cn.openaipay.application.feedback.facade.FeedbackFacade;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 后台反馈控制器测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@ExtendWith(MockitoExtension.class)
class AdminFeedbackControllerTest {

    /** 反馈门面。 */
    @Mock
    private FeedbackFacade feedbackFacade;
    /** 请求上下文。 */
    @Mock
    private AdminRequestContext adminRequestContext;

    /** 控制器。 */
    private AdminFeedbackController controller;
    /** 映射器。 */
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        controller = new AdminFeedbackController(feedbackFacade, adminRequestContext);
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void listTicketsShouldPassFiltersAndKeepFieldNames() throws Exception {
        when(feedbackFacade.listTickets("FDB202603210001", "PENDING", "PRODUCT_SUGGESTION", 880109000000000001L, 20))
                .thenReturn(List.of(ticket("FDB202603210001", "PENDING", null, null)));

        ApiResponse<List<FeedbackTicketDTO>> response =
                controller.listTickets("FDB202603210001", "PENDING", "PRODUCT_SUGGESTION", 880109000000000001L, 1, 20, null);

        verify(feedbackFacade).listTickets("FDB202603210001", "PENDING", "PRODUCT_SUGGESTION", 880109000000000001L, 20);
        JsonNode row = objectMapper.readTree(objectMapper.writeValueAsString(response)).at("/data/0");
        assertThat(row.at("/feedbackNo").asText()).isEqualTo("FDB202603210001");
        assertThat(row.at("/userId").asText()).isEqualTo("880109000000000001");
        assertThat(row.at("/feedbackType").asText()).isEqualTo("PRODUCT_SUGGESTION");
        assertThat(row.at("/sourcePageCode").asText()).isEqualTo("settings.feedback");
        assertThat(row.at("/status").asText()).isEqualTo("PENDING");
        assertThat(row.at("/attachmentUrls/0").asText()).isEqualTo("/api/media/fdb-1.png");
    }

    @Test
    void handleTicketShouldPreferCurrentAdminUsernameAsOperator() {
        when(adminRequestContext.currentAdminUsername()).thenReturn("ops_admin");
        when(feedbackFacade.handle(any())).thenReturn(ticket("FDB202603210001", "RESOLVED", "ops_admin", "已处理"));

        ApiResponse<FeedbackTicketDTO> response = controller.handleTicket(
                "FDB202603210001",
                new HandleFeedbackTicketRequest("RESOLVED", "已处理")
        );

        ArgumentCaptor<HandleFeedbackCommand> commandCaptor = ArgumentCaptor.forClass(HandleFeedbackCommand.class);
        verify(feedbackFacade).handle(commandCaptor.capture());
        HandleFeedbackCommand command = commandCaptor.getValue();

        assertEquals("FDB202603210001", command.feedbackNo());
        assertEquals("RESOLVED", command.status());
        assertEquals("ops_admin", command.handledBy());
        assertEquals("已处理", command.handleNote());
        assertEquals("RESOLVED", response.data().status());
    }

    @Test
    void handleTicketShouldFallbackOperatorFromAdminId() {
        when(adminRequestContext.currentAdminUsername()).thenReturn("   ");
        when(adminRequestContext.requiredAdminId()).thenReturn(9001L);
        when(feedbackFacade.handle(any())).thenReturn(ticket("FDB202603210002", "CLOSED", "admin#9001", "已关闭"));

        controller.handleTicket("FDB202603210002", new HandleFeedbackTicketRequest("CLOSED", "已关闭"));

        ArgumentCaptor<HandleFeedbackCommand> commandCaptor = ArgumentCaptor.forClass(HandleFeedbackCommand.class);
        verify(feedbackFacade).handle(commandCaptor.capture());
        assertEquals("admin#9001", commandCaptor.getValue().handledBy());
    }

    private FeedbackTicketDTO ticket(String feedbackNo, String status, String handledBy, String handleNote) {
        return new FeedbackTicketDTO(
                feedbackNo,
                "880109000000000001",
                "顾郡",
                "PRODUCT_SUGGESTION",
                "APP",
                "settings.feedback",
                "产品建议",
                "建议优化充值结果页",
                "13920000002",
                List.of("/api/media/fdb-1.png"),
                status,
                handledBy,
                handleNote,
                handledBy == null ? null : LocalDateTime.of(2026, 3, 21, 12, 0),
                "CLOSED".equals(status) ? LocalDateTime.of(2026, 3, 21, 12, 5) : null,
                LocalDateTime.of(2026, 3, 21, 10, 0),
                LocalDateTime.of(2026, 3, 21, 12, 5)
        );
    }
}
