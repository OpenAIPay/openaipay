package cn.openaipay.domain.deliver.model;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 投放上下文。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public record DeliverContext(
        /** 客户端ID。 */
        String clientId,
        /** 用户ID */
        Long userId,
        /** 场景编码。 */
        String sceneCode,
        /** 渠道编码。 */
        String channel,
        /** 用户标签集合。 */
        Set<String> userTags,
        /** 用户命中的人群编码。 */
        Set<String> audienceSegmentCodes,
        /** 请求时间。 */
        LocalDateTime requestTime,
        /** 兜底模式 */
        boolean fallbackMode
) {

    public DeliverContext {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId must not be blank");
        }
        sceneCode = normalize(sceneCode);
        channel = normalize(channel);
        userTags = normalizeTags(userTags);
        audienceSegmentCodes = normalizeTags(audienceSegmentCodes);
        requestTime = requestTime == null ? LocalDateTime.now() : requestTime;
    }

    /**
     * 处理业务数据。
     */
    public DeliverContext withFallbackMode(boolean targetFallbackMode) {
        return new DeliverContext(clientId, userId, sceneCode, channel, userTags, audienceSegmentCodes, requestTime, targetFallbackMode);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static Set<String> normalizeTags(Set<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> normalized = rawTags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return normalized.isEmpty() ? Set.of() : Set.copyOf(normalized);
    }
}
