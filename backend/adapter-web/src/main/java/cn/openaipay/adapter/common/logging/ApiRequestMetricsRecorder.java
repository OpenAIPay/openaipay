package cn.openaipay.adapter.common.logging;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * 接口可观测性指标记录器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Component
public class ApiRequestMetricsRecorder {

    /** 请求总量指标。 */
    public static final String METRIC_REQUEST_TOTAL = "aipay.api.request.total";
    /** 请求耗时指标。 */
    public static final String METRIC_REQUEST_LATENCY = "aipay.api.request.latency";

    private final MeterRegistry meterRegistry;

    public ApiRequestMetricsRecorder(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 记录一次接口请求结果。
     */
    public void record(String scene, boolean success, long elapsedNanos) {
        String normalizedScene = normalizeScene(scene);
        String outcome = success ? "SUCCESS" : "FAILURE";
        Counter.builder(METRIC_REQUEST_TOTAL)
                .description("AiPay API request total")
                .tag("scene", normalizedScene)
                .tag("outcome", outcome)
                .register(meterRegistry)
                .increment();

        Timer.builder(METRIC_REQUEST_LATENCY)
                .description("AiPay API request latency")
                .tag("scene", normalizedScene)
                .tag("outcome", outcome)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(Math.max(0L, elapsedNanos), TimeUnit.NANOSECONDS);
    }

    private String normalizeScene(String scene) {
        if (scene == null || scene.isBlank()) {
            return "UNKNOWN";
        }
        String normalized = scene.trim().toUpperCase(Locale.ROOT);
        return normalized.length() > 64 ? normalized.substring(0, 64) : normalized;
    }
}
