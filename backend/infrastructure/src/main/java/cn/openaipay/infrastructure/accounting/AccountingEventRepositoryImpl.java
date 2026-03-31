package cn.openaipay.infrastructure.accounting;

import cn.openaipay.domain.accounting.model.AccountingAmountDirection;
import cn.openaipay.domain.accounting.model.AccountingEvent;
import cn.openaipay.domain.accounting.model.AccountingEventQuery;
import cn.openaipay.domain.accounting.model.AccountingEventStatus;
import cn.openaipay.domain.accounting.model.AccountingLeg;
import cn.openaipay.domain.accounting.repository.AccountingEventRepository;
import cn.openaipay.infrastructure.accounting.dataobject.AccountingEventJournalDO;
import cn.openaipay.infrastructure.accounting.mapper.AccountingEventJournalMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 会计事件仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Repository
public class AccountingEventRepositoryImpl implements AccountingEventRepository {

    /** object映射器信息 */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** 事件信息 */
    private final AccountingEventJournalMapper accountingEventJournalMapper;

    public AccountingEventRepositoryImpl(AccountingEventJournalMapper accountingEventJournalMapper) {
        this.accountingEventJournalMapper = accountingEventJournalMapper;
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public AccountingEvent save(AccountingEvent event) {
        AccountingEventJournalDO entity = event.getId() == null
                ? accountingEventJournalMapper.findByEventId(event.getEventId()).orElse(new AccountingEventJournalDO())
                : accountingEventJournalMapper.findById(event.getId()).orElse(new AccountingEventJournalDO());
        fillDO(entity, event);
        return toDomain(accountingEventJournalMapper.save(entity));
    }

    /**
     * 按事件ID查找记录。
     */
    @Override
    public Optional<AccountingEvent> findByEventId(String eventId) {
        return accountingEventJournalMapper.findByEventId(eventId).map(this::toDomain);
    }

    /**
     * 按KEY查找记录。
     */
    @Override
    public Optional<AccountingEvent> findByIdempotencyKey(String idempotencyKey) {
        return accountingEventJournalMapper.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    /**
     * 查询业务数据。
     */
    @Override
    public List<AccountingEvent> list(AccountingEventQuery query) {
        QueryWrapper<AccountingEventJournalDO> wrapper = new QueryWrapper<>();
        if (query != null) {
            eqIfPresent(wrapper, "event_id", query.eventId());
            eqIfPresent(wrapper, "event_type", query.eventType());
            eqIfPresent(wrapper, "source_biz_type", query.sourceBizType());
            eqIfPresent(wrapper, "source_biz_no", query.sourceBizNo());
            eqIfPresent(wrapper, "biz_order_no", query.bizOrderNo());
            eqIfPresent(wrapper, "trade_order_no", query.tradeOrderNo());
            eqIfPresent(wrapper, "pay_order_no", query.payOrderNo());
            if (query.status() != null) {
                wrapper.eq("process_status", query.status().name());
            }
            wrapper.orderByDesc("updated_at", "id");
            wrapper.last("LIMIT " + normalizeLimit(query.limit()));
        }
        return accountingEventJournalMapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    private AccountingEvent toDomain(AccountingEventJournalDO entity) {
        return new AccountingEvent(
                entity.getId(),
                entity.getEventId(),
                entity.getEventType(),
                entity.getEventVersion(),
                entity.getBookId(),
                entity.getSourceSystem(),
                entity.getSourceBizType(),
                entity.getSourceBizNo(),
                entity.getBizOrderNo(),
                entity.getRequestNo(),
                entity.getTradeOrderNo(),
                entity.getPayOrderNo(),
                entity.getBusinessSceneCode(),
                entity.getBusinessDomainCode(),
                entity.getPayerUserId(),
                entity.getPayeeUserId(),
                CurrencyUnit.of(defaultValue(entity.getCurrencyCode(), "CNY")),
                entity.getOccurredAt(),
                entity.getPostingDate(),
                entity.getIdempotencyKey(),
                entity.getGlobalTxId(),
                entity.getTraceId(),
                entity.getPayloadJson(),
                parseStatus(entity.getProcessStatus()),
                parseLegs(entity.getLegsJson()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillDO(AccountingEventJournalDO entity, AccountingEvent event) {
        LocalDateTime now = LocalDateTime.now();
        entity.setEventId(event.getEventId());
        entity.setEventType(event.getEventType());
        entity.setEventVersion(event.getEventVersion());
        entity.setBookId(event.getBookId());
        entity.setSourceSystem(event.getSourceSystem());
        entity.setSourceBizType(event.getSourceBizType());
        entity.setSourceBizNo(event.getSourceBizNo());
        entity.setBizOrderNo(event.getBizOrderNo());
        entity.setRequestNo(event.getRequestNo());
        entity.setTradeOrderNo(event.getTradeOrderNo());
        entity.setPayOrderNo(event.getPayOrderNo());
        entity.setBusinessSceneCode(event.getBusinessSceneCode());
        entity.setBusinessDomainCode(event.getBusinessDomainCode());
        entity.setPayerUserId(event.getPayerUserId());
        entity.setPayeeUserId(event.getPayeeUserId());
        entity.setCurrencyCode(event.getCurrencyUnit().getCode());
        entity.setOccurredAt(event.getOccurredAt());
        entity.setPostingDate(event.getPostingDate());
        entity.setIdempotencyKey(event.getIdempotencyKey());
        entity.setGlobalTxId(event.getGlobalTxId());
        entity.setTraceId(event.getTraceId());
        entity.setPayloadJson(event.getPayload());
        entity.setLegsJson(writeLegs(event.getLegs()));
        entity.setProcessStatus(event.getStatus().name());
        entity.setRetryCount(entity.getRetryCount() == null ? 0 : entity.getRetryCount());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(event.getCreatedAt() == null ? now : event.getCreatedAt());
        }
        entity.setUpdatedAt(event.getUpdatedAt() == null ? now : event.getUpdatedAt());
    }

    private List<AccountingLeg> parseLegs(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<AccountingLegSnapshot> snapshots = OBJECT_MAPPER.readValue(raw, new TypeReference<>() { });
            return snapshots.stream()
                    .map(snapshot -> new AccountingLeg(
                            snapshot.legNo(),
                            snapshot.accountDomain(),
                            snapshot.accountType(),
                            snapshot.accountNo(),
                            snapshot.ownerType(),
                            snapshot.ownerId(),
                            Money.of(CurrencyUnit.of(defaultValue(snapshot.currencyCode(), "CNY")), new BigDecimal(snapshot.amount())),
                            parseDirection(snapshot.direction()),
                            snapshot.bizRole(),
                            snapshot.subjectHint(),
                            snapshot.referenceNo(),
                            snapshot.metadata()
                    ))
                    .toList();
        } catch (Exception ex) {
            throw new IllegalStateException("failed to parse accounting legs", ex);
        }
    }

    private String writeLegs(List<AccountingLeg> legs) {
        try {
            return OBJECT_MAPPER.writeValueAsString((legs == null ? List.<AccountingLeg>of() : legs).stream()
                    .map(leg -> new AccountingLegSnapshot(
                            leg.getLegNo(),
                            leg.getAccountDomain(),
                            leg.getAccountType(),
                            leg.getAccountNo(),
                            leg.getOwnerType(),
                            leg.getOwnerId(),
                            leg.getAmount().getAmount().toPlainString(),
                            leg.getAmount().getCurrencyUnit().getCode(),
                            leg.getDirection().name(),
                            leg.getBizRole(),
                            leg.getSubjectHint(),
                            leg.getReferenceNo(),
                            leg.getMetadata()
                    ))
                    .toList());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("failed to serialize accounting legs", ex);
        }
    }

    private AccountingEventStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return AccountingEventStatus.NEW;
        }
        try {
            return AccountingEventStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ignore) {
            return AccountingEventStatus.NEW;
        }
    }

    private AccountingAmountDirection parseDirection(String raw) {
        if (raw == null || raw.isBlank()) {
            return AccountingAmountDirection.OUT;
        }
        try {
            return AccountingAmountDirection.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ignore) {
            return AccountingAmountDirection.OUT;
        }
    }

    private void eqIfPresent(QueryWrapper<AccountingEventJournalDO> wrapper, String column, String value) {
        if (value != null && !value.isBlank()) {
            wrapper.eq(column, value.trim());
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 50;
        }
        return Math.min(limit, 200);
    }

    private String defaultValue(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private record AccountingLegSnapshot(
            /** LEG单号 */
            Integer legNo,
            /** account域信息 */
            String accountDomain,
            /** account类型 */
            String accountType,
            /** account单号 */
            String accountNo,
            /** 所属类型 */
            String ownerType,
            /** 所属ID */
            Long ownerId,
            /** 金额 */
            String amount,
            /** 币种编码 */
            String currencyCode,
            /** 方向 */
            String direction,
            /** 业务角色信息 */
            String bizRole,
            /** 科目hint信息 */
            String subjectHint,
            /** reference单号 */
            String referenceNo,
            /** 扩展信息 */
            String metadata
    ) {
    }
}
