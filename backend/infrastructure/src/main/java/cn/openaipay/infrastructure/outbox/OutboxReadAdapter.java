package cn.openaipay.infrastructure.outbox;

import cn.openaipay.application.outbox.dto.OutboxMessageDTO;
import cn.openaipay.application.outbox.dto.OutboxOverviewDTO;
import cn.openaipay.application.outbox.dto.OutboxTopicDistributionDTO;
import cn.openaipay.application.outbox.port.OutboxReadPort;
import cn.openaipay.infrastructure.asyncmessage.dataobject.AsyncMessageDO;
import cn.openaipay.infrastructure.asyncmessage.mapper.AsyncMessageMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Outbox 只读查询适配器
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Component
public class OutboxReadAdapter implements OutboxReadPort {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 200;

    private final AsyncMessageMapper asyncMessageMapper;

    public OutboxReadAdapter(AsyncMessageMapper asyncMessageMapper) {
        this.asyncMessageMapper = asyncMessageMapper;
    }

    @Override
    public OutboxOverviewDTO getOverview(long processingTimeoutSeconds) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime staleBefore = now.minusSeconds(Math.max(1L, processingTimeoutSeconds));

        long totalCount = safeCount(asyncMessageMapper.selectCount(new QueryWrapper<>()));
        long pendingCount = safeCount(asyncMessageMapper.selectCount(new QueryWrapper<AsyncMessageDO>().eq("status", "PENDING")));
        long processingCount = safeCount(asyncMessageMapper.selectCount(new QueryWrapper<AsyncMessageDO>().eq("status", "PROCESSING")));
        long succeededCount = safeCount(asyncMessageMapper.selectCount(new QueryWrapper<AsyncMessageDO>().eq("status", "SUCCEEDED")));
        long deadCount = safeCount(asyncMessageMapper.selectCount(new QueryWrapper<AsyncMessageDO>().eq("status", "DEAD")));
        long retriedCount = safeCount(asyncMessageMapper.selectCount(new QueryWrapper<AsyncMessageDO>().gt("retry_count", 0)));
        long retryPendingCount = safeCount(asyncMessageMapper.selectCount(
                new QueryWrapper<AsyncMessageDO>().eq("status", "PENDING").gt("retry_count", 0)
        ));
        long readyDispatchCount = safeCount(asyncMessageMapper.selectCount(
                new QueryWrapper<AsyncMessageDO>().eq("status", "PENDING").le("next_retry_at", now)
        ));
        long staleProcessingCount = safeCount(asyncMessageMapper.selectCount(
                new QueryWrapper<AsyncMessageDO>()
                        .eq("status", "PROCESSING")
                        .isNotNull("processing_started_at")
                        .lt("processing_started_at", staleBefore)
        ));
        return new OutboxOverviewDTO(
                totalCount,
                pendingCount,
                processingCount,
                succeededCount,
                deadCount,
                retriedCount,
                retryPendingCount,
                readyDispatchCount,
                staleProcessingCount
        );
    }

    @Override
    public List<OutboxTopicDistributionDTO> listTopicDistribution(Integer limit) {
        Map<String, TopicAccumulator> accMap = new LinkedHashMap<>();
        List<Map<String, Object>> groupedByTopicAndStatus = safeMapList(asyncMessageMapper.selectMaps(
                new QueryWrapper<AsyncMessageDO>()
                        .select("topic", "status", "COUNT(*) AS cnt")
                        .groupBy("topic", "status")
        ));
        for (Map<String, Object> row : groupedByTopicAndStatus) {
            String topic = normalizeText(readMapValue(row, "topic"));
            if (topic == null) {
                continue;
            }
            long count = toLong(readMapValue(row, "cnt"));
            String status = normalizeStatus(readMapValue(row, "status"));
            TopicAccumulator acc = accMap.computeIfAbsent(topic, TopicAccumulator::new);
            acc.totalCount += count;
            if ("PENDING".equals(status)) {
                acc.pendingCount += count;
            } else if ("PROCESSING".equals(status)) {
                acc.processingCount += count;
            } else if ("SUCCEEDED".equals(status)) {
                acc.succeededCount += count;
            } else if ("DEAD".equals(status)) {
                acc.deadCount += count;
            }
        }

        List<Map<String, Object>> retriedRows = safeMapList(asyncMessageMapper.selectMaps(
                new QueryWrapper<AsyncMessageDO>()
                        .select("topic", "COUNT(*) AS cnt")
                        .gt("retry_count", 0)
                        .groupBy("topic")
        ));
        for (Map<String, Object> row : retriedRows) {
            String topic = normalizeText(readMapValue(row, "topic"));
            if (topic == null) {
                continue;
            }
            TopicAccumulator acc = accMap.computeIfAbsent(topic, TopicAccumulator::new);
            acc.retriedCount = toLong(readMapValue(row, "cnt"));
        }

        return accMap.values().stream()
                .sorted(Comparator.comparingLong((TopicAccumulator item) -> item.totalCount).reversed()
                        .thenComparing(item -> item.topic))
                .limit(normalizeLimit(limit))
                .map(TopicAccumulator::toDTO)
                .toList();
    }

    @Override
    public List<OutboxMessageDTO> listMessages(String topic,
                                               String status,
                                               String keyword,
                                               Boolean onlyRetried,
                                               Integer limit,
                                               Boolean includePayload) {
        QueryWrapper<AsyncMessageDO> wrapper = buildListWrapper(topic, status, keyword, onlyRetried, limit);
        boolean includeRawPayload = Boolean.TRUE.equals(includePayload);
        return safeList(asyncMessageMapper.selectList(wrapper)).stream()
                .map(item -> toDTO(item, includeRawPayload))
                .toList();
    }

    @Override
    public OutboxMessageDTO getMessage(Long id) {
        AsyncMessageDO message = asyncMessageMapper.findById(requirePositive(id, "id"))
                .orElseThrow(() -> new NoSuchElementException("outbox message not found: " + id));
        return toDTO(message, true);
    }

    private QueryWrapper<AsyncMessageDO> buildListWrapper(String topic,
                                                          String status,
                                                          String keyword,
                                                          Boolean onlyRetried,
                                                          Integer limit) {
        QueryWrapper<AsyncMessageDO> wrapper = new QueryWrapper<>();
        String normalizedTopic = normalizeText(topic);
        if (normalizedTopic != null) {
            wrapper.eq("topic", normalizedTopic);
        }
        String normalizedStatus = normalizeStatus(status);
        if (normalizedStatus != null) {
            wrapper.eq("status", normalizedStatus);
        }
        String normalizedKeyword = normalizeText(keyword);
        if (normalizedKeyword != null) {
            wrapper.and(query -> query.like("message_key", normalizedKeyword)
                    .or()
                    .like("last_error", normalizedKeyword)
                    .or()
                    .like("payload", normalizedKeyword));
        }
        if (Boolean.TRUE.equals(onlyRetried)) {
            wrapper.gt("retry_count", 0);
        }
        wrapper.orderByDesc("updated_at", "id");
        wrapper.last("LIMIT " + normalizeLimit(limit));
        return wrapper;
    }

    private OutboxMessageDTO toDTO(AsyncMessageDO item, boolean includeRawPayload) {
        String payload = item.getPayload();
        String payloadPreview = preview(payload, 240);
        String payloadValue = includeRawPayload ? payload : null;
        Integer retryCount = item.getRetryCount() == null ? 0 : item.getRetryCount();
        Integer maxRetryCount = item.getMaxRetryCount() == null ? 0 : item.getMaxRetryCount();
        boolean canRetry = retryCount + 1 < maxRetryCount;
        return new OutboxMessageDTO(
                item.getId(),
                item.getTopic(),
                item.getMessageKey(),
                item.getStatus(),
                retryCount,
                maxRetryCount,
                canRetry,
                item.getNextRetryAt(),
                item.getProcessingStartedAt(),
                item.getLastError(),
                payloadPreview,
                payloadValue,
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private Long requirePositive(Long value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0");
        }
        return value;
    }

    private String preview(String raw, int limit) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim();
        if (normalized.length() <= limit) {
            return normalized;
        }
        return normalized.substring(0, limit) + "...";
    }

    private String normalizeText(Object raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = String.valueOf(raw).trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeStatus(Object raw) {
        String normalized = normalizeText(raw);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private long safeCount(Long value) {
        return value == null ? 0L : value;
    }

    private <T> List<T> safeList(List<T> rows) {
        return rows == null ? List.of() : rows;
    }

    private List<Map<String, Object>> safeMapList(List<Map<String, Object>> rows) {
        return rows == null ? List.of() : rows;
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return 0L;
        }
        return Long.parseLong(text);
    }

    private Object readMapValue(Map<String, Object> row, String key) {
        if (row == null || row.isEmpty()) {
            return null;
        }
        if (row.containsKey(key)) {
            return row.get(key);
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static final class TopicAccumulator {
        private final String topic;
        private long totalCount;
        private long pendingCount;
        private long processingCount;
        private long succeededCount;
        private long deadCount;
        private long retriedCount;

        private TopicAccumulator(String topic) {
            this.topic = topic;
        }

        private OutboxTopicDistributionDTO toDTO() {
            return new OutboxTopicDistributionDTO(
                    topic,
                    totalCount,
                    pendingCount,
                    processingCount,
                    succeededCount,
                    deadCount,
                    retriedCount
            );
        }
    }
}
