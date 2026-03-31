package cn.openaipay.adapter.admin.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.adapter.admin.security.AdminRequestContext;
import cn.openaipay.adapter.admin.web.request.RequeueOutboxDeadLetterRequest;
import cn.openaipay.adapter.admin.web.request.RequeueOutboxDeadLettersRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.outbox.facade.OutboxMonitorFacade;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 后台 outbox 控制器测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@ExtendWith(MockitoExtension.class)
class AdminOutboxControllerTest {

    @Mock
    private OutboxMonitorFacade outboxMonitorFacade;
    @Mock
    private AdminRequestContext adminRequestContext;

    private AdminOutboxController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminOutboxController(outboxMonitorFacade, adminRequestContext);
    }

    @Test
    void requeueDeadLetterShouldUseCurrentUsernameAsOperator() {
        LocalDateTime retryAt = LocalDateTime.of(2026, 3, 23, 10, 30);
        when(adminRequestContext.currentAdminUsername()).thenReturn("ops_admin");
        when(outboxMonitorFacade.requeueDeadLetter(1001L, retryAt, "ops_admin")).thenReturn(true);

        ApiResponse<AdminOutboxController.OutboxRequeueResultResponse> response = controller.requeueDeadLetter(
                1001L,
                new RequeueOutboxDeadLetterRequest(retryAt)
        );

        verify(outboxMonitorFacade).requeueDeadLetter(1001L, retryAt, "ops_admin");
        assertEquals(1, response.data().requestedLimit());
        assertEquals(1, response.data().requeuedCount());
        assertThat(response.data().nextRetryAt()).isEqualTo(retryAt);
    }

    @Test
    void requeueDeadLettersShouldNormalizeTopicAndLimit() {
        LocalDateTime retryAt = LocalDateTime.of(2026, 3, 23, 12, 0);
        when(adminRequestContext.currentAdminUsername()).thenReturn(" ");
        when(adminRequestContext.currentAdminId()).thenReturn(9002L);
        when(outboxMonitorFacade.requeueDeadLetters("PAY_EXECUTE", 200, retryAt, "admin#9002")).thenReturn(17);

        ApiResponse<AdminOutboxController.OutboxRequeueResultResponse> response = controller.requeueDeadLetters(
                new RequeueOutboxDeadLettersRequest("  PAY_EXECUTE  ", 300, retryAt)
        );

        verify(outboxMonitorFacade).requeueDeadLetters("PAY_EXECUTE", 200, retryAt, "admin#9002");
        assertEquals(200, response.data().requestedLimit());
        assertEquals(17, response.data().requeuedCount());
        assertEquals("PAY_EXECUTE", response.data().topic());
    }
}
