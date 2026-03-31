package cn.openaipay.infrastructure.outbox;

import cn.openaipay.domain.outbox.model.OutboxMessage;
import cn.openaipay.domain.outbox.model.OutboxMessageStatus;
import cn.openaipay.domain.outbox.repository.OutboxMessageRepository;
import cn.openaipay.infrastructure.asyncmessage.dataobject.AsyncMessageDO;
import cn.openaipay.infrastructure.asyncmessage.mapper.AsyncMessageMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 标准 Outbox 消息仓储实现。
 * 说明：当前复用既有 async_message 表，后续可平滑切换到 MQ relay。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Repository
public class OutboxMessageRepositoryImpl implements OutboxMessageRepository {
    /** 异步消息信息 */
    private final AsyncMessageMapper asyncMessageMapper;
    /** 模板信息 */
    private final JdbcTemplate jdbcTemplate;

    public OutboxMessageRepositoryImpl(AsyncMessageMapper asyncMessageMapper, JdbcTemplate jdbcTemplate) {
        this.asyncMessageMapper = asyncMessageMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public OutboxMessage save(OutboxMessage outboxMessage) {
        AsyncMessageDO entity = new AsyncMessageDO();
        fillDO(entity, outboxMessage);
        if (outboxMessage.getId() != null) {
            entity.setId(outboxMessage.getId());
            return toDomain(asyncMessageMapper.save(entity));
        }

        try {
            asyncMessageMapper.insert(entity);
            return toDomain(entity);
        } catch (DuplicateKeyException duplicateKeyException) {
            UpdateWrapper<AsyncMessageDO> wrapper = new UpdateWrapper<>();
            wrapper.eq("topic", outboxMessage.getTopic())
                    .eq("message_key", outboxMessage.getMessageKey());
            int updatedRows = asyncMessageMapper.update(entity, wrapper);
            if (updatedRows <= 0) {
                throw duplicateKeyException;
            }
            return asyncMessageMapper.findByTopicAndMessageKey(outboxMessage.getTopic(), outboxMessage.getMessageKey())
                    .map(this::toDomain)
                    .orElseThrow(() -> duplicateKeyException);
        }
    }

    /**
     * 按主题与消息KEY查找记录。
     */
    @Override
    public Optional<OutboxMessage> findByTopicAndMessageKey(String topic, String messageKey) {
        return asyncMessageMapper.findByTopicAndMessageKey(topic, messageKey).map(this::toDomain);
    }

    @Override
    public Optional<OutboxMessage> findById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(asyncMessageMapper.selectById(id)).map(this::toDomain);
    }

    /**
     * 处理批量信息。
     */
    @Override
    @Transactional
    public List<OutboxMessage> claimNextBatch(int batchSize, LocalDateTime now, Duration processingTimeout) {
        int normalizedBatchSize = Math.max(1, batchSize);
        LocalDateTime claimTime = now == null ? LocalDateTime.now() : now;
        Duration timeout = processingTimeout == null ? Duration.ofMinutes(2) : processingTimeout;
        LocalDateTime staleBefore = claimTime.minus(timeout);

        List<Long> claimedIds = jdbcTemplate.query(
                """
                        SELECT id
                        FROM async_message
                        WHERE (
                            status = 'PENDING'
                            AND next_retry_at <= ?
                        ) OR (
                            status = 'PROCESSING'
                            AND processing_started_at IS NOT NULL
                            AND processing_started_at < ?
                        )
                        ORDER BY next_retry_at ASC, id ASC
                        LIMIT ?
                        FOR UPDATE SKIP LOCKED
                        """,
                (rs, rowNum) -> rs.getLong("id"),
                Timestamp.valueOf(claimTime),
                Timestamp.valueOf(staleBefore),
                normalizedBatchSize
        );
        if (claimedIds == null || claimedIds.isEmpty()) {
            return List.of();
        }

        jdbcTemplate.batchUpdate(
                "UPDATE async_message SET status = 'PROCESSING', processing_started_at = ?, updated_at = ? WHERE id = ?",
                claimedIds,
                claimedIds.size(),
                (ps, id) -> {
                    ps.setTimestamp(1, Timestamp.valueOf(claimTime));
                    ps.setTimestamp(2, Timestamp.valueOf(claimTime));
                    ps.setLong(3, id);
                }
        );

        Map<Long, Integer> orderIndex = new HashMap<>();
        for (int index = 0; index < claimedIds.size(); index++) {
            orderIndex.put(claimedIds.get(index), index);
        }

        return asyncMessageMapper.selectBatchIds(claimedIds)
                .stream()
                .sorted(Comparator.comparingInt(message -> orderIndex.getOrDefault(message.getId(), Integer.MAX_VALUE)))
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public boolean requeueDeadLetter(Long id, LocalDateTime nextRetryAt, String operator) {
        if (id == null || id <= 0) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime retryAt = nextRetryAt == null ? now : nextRetryAt;
        String normalizedOperator = normalizeOperator(operator);
        UpdateWrapper<AsyncMessageDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id).eq("status", "DEAD");
        AsyncMessageDO updated = new AsyncMessageDO();
        updated.setStatus("PENDING");
        updated.setNextRetryAt(retryAt);
        updated.setProcessingStartedAt(null);
        updated.setLastError("manual requeue by " + normalizedOperator);
        updated.setUpdatedAt(now);
        return asyncMessageMapper.update(updated, wrapper) > 0;
    }

    @Override
    @Transactional
    public int requeueDeadLetters(String topic, int limit, LocalDateTime nextRetryAt, String operator) {
        int normalizedLimit = Math.max(1, Math.min(limit, 200));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime retryAt = nextRetryAt == null ? now : nextRetryAt;
        QueryWrapper<AsyncMessageDO> wrapper = new QueryWrapper<>();
        wrapper.select("id")
                .eq("status", "DEAD")
                .orderByAsc("updated_at")
                .orderByAsc("id")
                .last("LIMIT " + normalizedLimit);
        if (topic != null && !topic.isBlank()) {
            wrapper.eq("topic", topic.trim());
        }
        List<Long> ids = asyncMessageMapper.selectList(wrapper).stream()
                .map(AsyncMessageDO::getId)
                .filter(item -> item != null && item > 0)
                .toList();
        if (ids.isEmpty()) {
            return 0;
        }
        String normalizedOperator = normalizeOperator(operator);
        int[][] affectedRows = jdbcTemplate.batchUpdate(
                """
                        UPDATE async_message
                           SET status = 'PENDING',
                               next_retry_at = ?,
                               processing_started_at = NULL,
                               last_error = ?,
                               updated_at = ?
                         WHERE id = ?
                           AND status = 'DEAD'
                        """,
                ids,
                ids.size(),
                (ps, id) -> {
                    ps.setTimestamp(1, Timestamp.valueOf(retryAt));
                    ps.setString(2, "manual batch requeue by " + normalizedOperator);
                    ps.setTimestamp(3, Timestamp.valueOf(now));
                    ps.setLong(4, id);
                }
        );
        int success = 0;
        for (int[] batch : affectedRows) {
            for (int row : batch) {
                if (row > 0) {
                    success += row;
                }
            }
        }
        return success;
    }

    private AsyncMessageDO fillDO(AsyncMessageDO entity, OutboxMessage outboxMessage) {
        entity.setTopic(outboxMessage.getTopic());
        entity.setMessageKey(outboxMessage.getMessageKey());
        entity.setPayload(outboxMessage.getPayload());
        entity.setStatus(outboxMessage.getStatus().name());
        entity.setRetryCount(outboxMessage.getRetryCount());
        entity.setMaxRetryCount(outboxMessage.getMaxRetryCount());
        entity.setNextRetryAt(outboxMessage.getNextRetryAt());
        entity.setProcessingStartedAt(outboxMessage.getProcessingStartedAt());
        entity.setLastError(outboxMessage.getLastError());
        entity.setCreatedAt(outboxMessage.getCreatedAt());
        entity.setUpdatedAt(outboxMessage.getUpdatedAt());
        return entity;
    }

    private String normalizeOperator(String operator) {
        if (operator == null || operator.isBlank()) {
            return "system";
        }
        return operator.trim();
    }

    private OutboxMessage toDomain(AsyncMessageDO entity) {
        return new OutboxMessage(
                entity.getId(),
                entity.getTopic(),
                entity.getMessageKey(),
                entity.getPayload(),
                OutboxMessageStatus.from(entity.getStatus()),
                entity.getRetryCount() == null ? 0 : entity.getRetryCount(),
                entity.getMaxRetryCount() == null ? 16 : entity.getMaxRetryCount(),
                entity.getNextRetryAt(),
                entity.getProcessingStartedAt(),
                entity.getLastError(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
