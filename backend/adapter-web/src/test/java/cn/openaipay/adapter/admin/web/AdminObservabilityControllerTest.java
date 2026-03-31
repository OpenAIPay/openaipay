package cn.openaipay.adapter.admin.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.common.logging.ApiRequestMetricsRecorder;
import cn.openaipay.application.outbox.dto.OutboxOverviewDTO;
import cn.openaipay.application.outbox.facade.OutboxMonitorFacade;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 后台可观测控制器测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@ExtendWith(MockitoExtension.class)
class AdminObservabilityControllerTest {

    @Mock
    private OutboxMonitorFacade outboxMonitorFacade;

    private SimpleMeterRegistry meterRegistry;
    private AdminObservabilityController controller;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        controller = new AdminObservabilityController(meterRegistry, outboxMonitorFacade);
    }

    @Test
    void overviewShouldAggregateMetricsAndStorageStats() {
        meterRegistry.counter(ApiRequestMetricsRecorder.METRIC_REQUEST_TOTAL, "scene", "PAY", "outcome", "SUCCESS").increment(5);
        meterRegistry.counter(ApiRequestMetricsRecorder.METRIC_REQUEST_TOTAL, "scene", "PAY", "outcome", "FAILURE").increment(2);
        meterRegistry.timer(ApiRequestMetricsRecorder.METRIC_REQUEST_LATENCY, "scene", "PAY", "outcome", "SUCCESS")
                .record(120, TimeUnit.MILLISECONDS);
        meterRegistry.timer(ApiRequestMetricsRecorder.METRIC_REQUEST_LATENCY, "scene", "PAY", "outcome", "FAILURE")
                .record(80, TimeUnit.MILLISECONDS);

        when(outboxMonitorFacade.getOverview()).thenReturn(new OutboxOverviewDTO(100, 20, 5, 70, 3, 15, 7, 12, 1));

        ApiResponse<AdminObservabilityController.ObservabilityOverviewResponse> response = controller.overview();

        assertEquals(7L, response.data().apiRequestTotal());
        assertEquals(5L, response.data().apiRequestSuccessTotal());
        assertEquals(2L, response.data().apiRequestFailureTotal());
        assertEquals(3L, response.data().outboxDeadCount());
        assertEquals(7L, response.data().outboxRetryPendingCount());
        assertEquals(0L, response.data().reconPendingPayCount());
        assertEquals(0L, response.data().tradeFailedTodayCount());
        assertThat(response.data().apiAvgLatencyMs()).isPositive();
    }

    @Test
    void apiScenesShouldReturnSceneRowsOrderedByRequestTotal() {
        meterRegistry.counter(ApiRequestMetricsRecorder.METRIC_REQUEST_TOTAL, "scene", "PAY", "outcome", "SUCCESS").increment(8);
        meterRegistry.counter(ApiRequestMetricsRecorder.METRIC_REQUEST_TOTAL, "scene", "PAY", "outcome", "FAILURE").increment(1);
        meterRegistry.counter(ApiRequestMetricsRecorder.METRIC_REQUEST_TOTAL, "scene", "AUTH", "outcome", "SUCCESS").increment(3);
        meterRegistry.timer(ApiRequestMetricsRecorder.METRIC_REQUEST_LATENCY, "scene", "PAY", "outcome", "SUCCESS")
                .record(10, TimeUnit.MILLISECONDS);

        ApiResponse<List<AdminObservabilityController.ApiSceneMetricRow>> response = controller.apiScenes(10);

        assertEquals(2, response.data().size());
        assertEquals("PAY", response.data().get(0).scene());
        assertEquals(9L, response.data().get(0).requestTotal());
        assertEquals(8L, response.data().get(0).successTotal());
        assertEquals(1L, response.data().get(0).failureTotal());
    }
}
