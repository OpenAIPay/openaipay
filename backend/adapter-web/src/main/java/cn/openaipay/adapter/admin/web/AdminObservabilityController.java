package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.common.logging.ApiRequestMetricsRecorder;
import cn.openaipay.application.outbox.dto.OutboxOverviewDTO;
import cn.openaipay.application.outbox.facade.OutboxMonitorFacade;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import io.micrometer.core.instrument.Timer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台可观测性控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@RestController
@RequestMapping("/api/admin/observability")
public class AdminObservabilityController {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final MeterRegistry meterRegistry;
    private final OutboxMonitorFacade outboxMonitorFacade;

    public AdminObservabilityController(MeterRegistry meterRegistry,
                                        OutboxMonitorFacade outboxMonitorFacade) {
        this.meterRegistry = meterRegistry;
        this.outboxMonitorFacade = outboxMonitorFacade;
    }

    /**
     * 可观测概览。
     */
    @GetMapping("/overview")
    @RequireAdminPermission("ops.observability.view")
    public ApiResponse<ObservabilityOverviewResponse> overview() {
        MetricSummary summary = buildMetricSummary();
        OutboxOverviewDTO outboxOverview = outboxMonitorFacade.getOverview();

        return ApiResponse.success(new ObservabilityOverviewResponse(
                summary.requestTotal,
                summary.requestSuccess,
                summary.requestFailure,
                summary.avgLatencyMs,
                summary.maxLatencyMs,
                outboxOverview.deadCount(),
                outboxOverview.retryPendingCount(),
                0L,
                0L,
                LocalDateTime.now()
        ));
    }

    /**
     * 场景维度 API 指标。
     */
    @GetMapping("/api-scenes")
    @RequireAdminPermission("ops.observability.view")
    public ApiResponse<List<ApiSceneMetricRow>> apiScenes(
            @RequestParam(value = "limit", required = false) Integer limit) {
        int normalizedLimit = normalizeLimit(limit);
        Map<String, SceneMetricAccumulator> accumulatorMap = new LinkedHashMap<>();
        for (Counter counter : findCounters(ApiRequestMetricsRecorder.METRIC_REQUEST_TOTAL)) {
            String scene = normalizeTag(counter.getId(), "scene");
            String outcome = normalizeTag(counter.getId(), "outcome");
            SceneMetricAccumulator acc = accumulatorMap.computeIfAbsent(scene, SceneMetricAccumulator::new);
            long count = Math.round(counter.count());
            acc.total += count;
            if ("SUCCESS".equals(outcome)) {
                acc.success += count;
            } else if ("FAILURE".equals(outcome)) {
                acc.failure += count;
            }
        }
        for (Timer timer : findTimers(ApiRequestMetricsRecorder.METRIC_REQUEST_LATENCY)) {
            String scene = normalizeTag(timer.getId(), "scene");
            SceneMetricAccumulator acc = accumulatorMap.computeIfAbsent(scene, SceneMetricAccumulator::new);
            acc.latencyCount += timer.count();
            acc.latencyNanos += timer.totalTime(TimeUnit.NANOSECONDS);
            acc.maxLatencyNanos = Math.max(acc.maxLatencyNanos, (long) timer.max(TimeUnit.NANOSECONDS));
        }
        List<ApiSceneMetricRow> rows = accumulatorMap.values().stream()
                .sorted(Comparator.comparingLong((SceneMetricAccumulator item) -> item.total).reversed()
                        .thenComparing(item -> item.scene))
                .limit(normalizedLimit)
                .map(SceneMetricAccumulator::toRow)
                .toList();
        return ApiResponse.success(rows);
    }

    private MetricSummary buildMetricSummary() {
        long requestTotal = 0L;
        long requestSuccess = 0L;
        long requestFailure = 0L;
        for (Counter counter : findCounters(ApiRequestMetricsRecorder.METRIC_REQUEST_TOTAL)) {
            long count = Math.round(counter.count());
            requestTotal += count;
            String outcome = normalizeTag(counter.getId(), "outcome");
            if ("SUCCESS".equals(outcome)) {
                requestSuccess += count;
            } else if ("FAILURE".equals(outcome)) {
                requestFailure += count;
            }
        }

        long latencyCount = 0L;
        double latencyNanos = 0D;
        double maxLatencyNanos = 0D;
        for (Timer timer : findTimers(ApiRequestMetricsRecorder.METRIC_REQUEST_LATENCY)) {
            latencyCount += timer.count();
            latencyNanos += timer.totalTime(TimeUnit.NANOSECONDS);
            maxLatencyNanos = Math.max(maxLatencyNanos, timer.max(TimeUnit.NANOSECONDS));
        }
        BigDecimal avgLatencyMs = latencyCount <= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(latencyNanos / latencyCount / 1_000_000D).setScale(3, RoundingMode.HALF_UP);
        BigDecimal maxLatencyMs = BigDecimal.valueOf(maxLatencyNanos / 1_000_000D).setScale(3, RoundingMode.HALF_UP);
        return new MetricSummary(requestTotal, requestSuccess, requestFailure, avgLatencyMs, maxLatencyMs);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private List<Counter> findCounters(String meterName) {
        Search search = safeSearch(meterName);
        return search == null ? List.of() : safeList(search.counters());
    }

    private List<Timer> findTimers(String meterName) {
        Search search = safeSearch(meterName);
        return search == null ? List.of() : safeList(search.timers());
    }

    private Search safeSearch(String meterName) {
        if (meterRegistry == null || meterName == null || meterName.isBlank()) {
            return null;
        }
        return meterRegistry.find(meterName);
    }

    private <T> List<T> safeList(Collection<T> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        if (rows instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<T> list = (List<T>) rows;
            return list;
        }
        return List.copyOf(rows);
    }

    private String normalizeTag(Meter.Id meterId, String key) {
        String value = meterId == null ? null : meterId.getTag(key);
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return normalized.length() > 64 ? normalized.substring(0, 64) : normalized;
    }

    private static final class MetricSummary {
        private final long requestTotal;
        private final long requestSuccess;
        private final long requestFailure;
        private final BigDecimal avgLatencyMs;
        private final BigDecimal maxLatencyMs;

        private MetricSummary(long requestTotal,
                              long requestSuccess,
                              long requestFailure,
                              BigDecimal avgLatencyMs,
                              BigDecimal maxLatencyMs) {
            this.requestTotal = requestTotal;
            this.requestSuccess = requestSuccess;
            this.requestFailure = requestFailure;
            this.avgLatencyMs = avgLatencyMs;
            this.maxLatencyMs = maxLatencyMs;
        }
    }

    private static final class SceneMetricAccumulator {
        private final String scene;
        private long total;
        private long success;
        private long failure;
        private long latencyCount;
        private double latencyNanos;
        private long maxLatencyNanos;

        private SceneMetricAccumulator(String scene) {
            this.scene = scene;
        }

        private ApiSceneMetricRow toRow() {
            BigDecimal avgLatencyMs = latencyCount <= 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(latencyNanos / latencyCount / 1_000_000D).setScale(3, RoundingMode.HALF_UP);
            BigDecimal maxLatencyMs = BigDecimal.valueOf(maxLatencyNanos / 1_000_000D).setScale(3, RoundingMode.HALF_UP);
            return new ApiSceneMetricRow(scene, total, success, failure, avgLatencyMs, maxLatencyMs);
        }
    }

    /**
     * 可观测概览响应。
     */
    public record ObservabilityOverviewResponse(
            /** API 请求总量。 */
            long apiRequestTotal,
            /** API 请求成功量。 */
            long apiRequestSuccessTotal,
            /** API 请求失败量。 */
            long apiRequestFailureTotal,
            /** 平均耗时（毫秒）。 */
            BigDecimal apiAvgLatencyMs,
            /** 最大耗时（毫秒）。 */
            BigDecimal apiMaxLatencyMs,
            /** outbox 死信数量。 */
            long outboxDeadCount,
            /** outbox 重试待投递数量。 */
            long outboxRetryPendingCount,
            /** 支付待对账数量。 */
            long reconPendingPayCount,
            /** 今日失败交易数量。 */
            long tradeFailedTodayCount,
            /** 生成时间。 */
            LocalDateTime generatedAt
    ) {
    }

    /**
     * API 场景指标行。
     */
    public record ApiSceneMetricRow(
            /** 场景。 */
            String scene,
            /** 请求总量。 */
            long requestTotal,
            /** 成功量。 */
            long successTotal,
            /** 失败量。 */
            long failureTotal,
            /** 平均耗时（毫秒）。 */
            BigDecimal avgLatencyMs,
            /** 最大耗时（毫秒）。 */
            BigDecimal maxLatencyMs
    ) {
    }
}
