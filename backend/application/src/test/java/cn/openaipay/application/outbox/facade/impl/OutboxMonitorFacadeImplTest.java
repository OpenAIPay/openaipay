package cn.openaipay.application.outbox.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openaipay.application.outbox.service.OutboxMonitorService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * OutboxMonitorFacadeImpl 测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@ExtendWith(MockitoExtension.class)
class OutboxMonitorFacadeImplTest {

    @Mock
    private OutboxMonitorService outboxMonitorService;

    private OutboxMonitorFacadeImpl facade;

    @BeforeEach
    void setUp() {
        facade = new OutboxMonitorFacadeImpl(outboxMonitorService);
    }

    @Test
    void requeueDeadLetterShouldDelegateToService() {
        LocalDateTime retryAt = LocalDateTime.of(2026, 3, 23, 13, 0);
        when(outboxMonitorService.requeueDeadLetter(1001L, retryAt, "ops_admin")).thenReturn(true);

        boolean result = facade.requeueDeadLetter(1001L, retryAt, "ops_admin");

        verify(outboxMonitorService).requeueDeadLetter(1001L, retryAt, "ops_admin");
        assertEquals(true, result);
    }

    @Test
    void requeueDeadLettersShouldDelegateToService() {
        LocalDateTime retryAt = LocalDateTime.of(2026, 3, 23, 14, 0);
        when(outboxMonitorService.requeueDeadLetters("PAY_EXECUTE", 50, retryAt, "ops_admin")).thenReturn(12);

        int result = facade.requeueDeadLetters("PAY_EXECUTE", 50, retryAt, "ops_admin");

        verify(outboxMonitorService).requeueDeadLetters("PAY_EXECUTE", 50, retryAt, "ops_admin");
        assertEquals(12, result);
    }
}
