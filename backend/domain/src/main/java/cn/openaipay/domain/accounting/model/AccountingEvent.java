package cn.openaipay.domain.accounting.model;

import org.joda.money.CurrencyUnit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 会计标准事件。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class AccountingEvent {

    /** 数据库主键ID */
    private final Long id;
    /** 事件ID */
    private final String eventId;
    /** 事件类型 */
    private final String eventType;
    /** 事件版本号 */
    private final Integer eventVersion;
    /** 业务ID */
    private final String bookId;
    /** 来源信息 */
    private final String sourceSystem;
    /** 来源业务类型 */
    private final String sourceBizType;
    /** 来源业务单号 */
    private final String sourceBizNo;
    /** 业务单号 */
    private final String bizOrderNo;
    /** 请求幂等号 */
    private final String requestNo;
    /** 交易订单单号 */
    private final String tradeOrderNo;
    /** 支付订单单号 */
    private final String payOrderNo;
    /** 业务场景编码 */
    private final String businessSceneCode;
    /** 业务域编码 */
    private final String businessDomainCode;
    /** 付款方用户ID */
    private final Long payerUserId;
    /** 收款方用户ID */
    private final Long payeeUserId;
    /** 单元信息 */
    private final CurrencyUnit currencyUnit;
    /** 业务时间 */
    private final LocalDateTime occurredAt;
    /** 业务日期 */
    private final LocalDate postingDate;
    /** 业务键 */
    private final String idempotencyKey;
    /** TXID */
    private final String globalTxId;
    /** 业务ID */
    private final String traceId;
    /** 消息载荷内容 */
    private final String payload;
    /** 当前状态编码 */
    private AccountingEventStatus status;
    /** 分录列表 */
    private final List<AccountingLeg> legs;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public AccountingEvent(Long id,
                           String eventId,
                           String eventType,
                           Integer eventVersion,
                           String bookId,
                           String sourceSystem,
                           String sourceBizType,
                           String sourceBizNo,
                           String bizOrderNo,
                           String requestNo,
                           String tradeOrderNo,
                           String payOrderNo,
                           String businessSceneCode,
                           String businessDomainCode,
                           Long payerUserId,
                           Long payeeUserId,
                           CurrencyUnit currencyUnit,
                           LocalDateTime occurredAt,
                           LocalDate postingDate,
                           String idempotencyKey,
                           String globalTxId,
                           String traceId,
                           String payload,
                           AccountingEventStatus status,
                           List<AccountingLeg> legs,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
        this.id = id;
        this.eventId = required(eventId, "eventId");
        this.eventType = required(eventType, "eventType");
        this.eventVersion = eventVersion == null ? 1 : eventVersion;
        this.bookId = required(bookId, "bookId");
        this.sourceSystem = required(sourceSystem, "sourceSystem");
        this.sourceBizType = required(sourceBizType, "sourceBizType");
        this.sourceBizNo = required(sourceBizNo, "sourceBizNo");
        this.bizOrderNo = optional(bizOrderNo);
        this.requestNo = optional(requestNo);
        this.tradeOrderNo = optional(tradeOrderNo);
        this.payOrderNo = optional(payOrderNo);
        this.businessSceneCode = optional(businessSceneCode);
        this.businessDomainCode = optional(businessDomainCode);
        this.payerUserId = payerUserId;
        this.payeeUserId = payeeUserId;
        this.currencyUnit = Objects.requireNonNull(currencyUnit, "currencyUnit must not be null");
        this.occurredAt = occurredAt == null ? LocalDateTime.now() : occurredAt;
        this.postingDate = postingDate == null ? this.occurredAt.toLocalDate() : postingDate;
        this.idempotencyKey = required(idempotencyKey, "idempotencyKey");
        this.globalTxId = optional(globalTxId);
        this.traceId = optional(traceId);
        this.payload = optional(payload);
        this.status = status == null ? AccountingEventStatus.NEW : status;
        this.legs = Collections.unmodifiableList(normalizeLegs(legs));
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;

        if (this.legs.isEmpty()) {
            throw new IllegalArgumentException("accounting event legs must not be empty");
        }
        for (AccountingLeg leg : this.legs) {
            if (!this.currencyUnit.equals(leg.getAmount().getCurrencyUnit())) {
                throw new IllegalArgumentException("all leg currency must equal event currency");
            }
        }
    }

    /**
     * 创建业务数据。
     */
    public static AccountingEvent create(String eventId,
                                         String eventType,
                                         String bookId,
                                         String sourceSystem,
                                         String sourceBizType,
                                         String sourceBizNo,
                                         String bizOrderNo,
                                         String requestNo,
                                         String tradeOrderNo,
                                         String payOrderNo,
                                         String businessSceneCode,
                                         String businessDomainCode,
                                         Long payerUserId,
                                         Long payeeUserId,
                                         CurrencyUnit currencyUnit,
                                         LocalDateTime occurredAt,
                                         String idempotencyKey,
                                         String globalTxId,
                                         String traceId,
                                         String payload,
                                         List<AccountingLeg> legs) {
        return new AccountingEvent(
                null,
                eventId,
                eventType,
                1,
                bookId,
                sourceSystem,
                sourceBizType,
                sourceBizNo,
                bizOrderNo,
                requestNo,
                tradeOrderNo,
                payOrderNo,
                businessSceneCode,
                businessDomainCode,
                payerUserId,
                payeeUserId,
                currencyUnit,
                occurredAt,
                occurredAt == null ? null : occurredAt.toLocalDate(),
                idempotencyKey,
                globalTxId,
                traceId,
                payload,
                AccountingEventStatus.NEW,
                legs,
                occurredAt,
                occurredAt
        );
    }

    /**
     * 标记业务数据。
     */
    public void markProcessing(LocalDateTime now) {
        this.status = AccountingEventStatus.PROCESSING;
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    /**
     * 标记业务数据。
     */
    public void markPosted(LocalDateTime now) {
        this.status = AccountingEventStatus.POSTED;
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    /**
     * 标记业务数据。
     */
    public void markFailed(LocalDateTime now) {
        this.status = AccountingEventStatus.FAILED;
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    /**
     * 标记业务数据。
     */
    public void markReversed(LocalDateTime now) {
        this.status = AccountingEventStatus.REVERSED;
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    /**
     * 获取ID。
     */
    public Long getId() { return id; }
    /**
     * 获取事件ID。
     */
    public String getEventId() { return eventId; }
    /**
     * 获取事件类型信息。
     */
    public String getEventType() { return eventType; }
    /**
     * 获取事件版本信息。
     */
    public Integer getEventVersion() { return eventVersion; }
    /**
     * 获取ID。
     */
    public String getBookId() { return bookId; }
    /**
     * 获取业务数据。
     */
    public String getSourceSystem() { return sourceSystem; }
    /**
     * 获取业务信息。
     */
    public String getSourceBizType() { return sourceBizType; }
    /**
     * 获取业务NO信息。
     */
    public String getSourceBizNo() { return sourceBizNo; }
    /**
     * 获取业务NO信息。
     */
    public String getBizOrderNo() { return bizOrderNo; }
    /**
     * 获取请求NO信息。
     */
    public String getRequestNo() { return requestNo; }
    /**
     * 获取交易订单NO信息。
     */
    public String getTradeOrderNo() { return tradeOrderNo; }
    /**
     * 获取支付订单NO信息。
     */
    public String getPayOrderNo() { return payOrderNo; }
    /**
     * 获取场景编码。
     */
    public String getBusinessSceneCode() { return businessSceneCode; }
    /**
     * 获取领域编码。
     */
    public String getBusinessDomainCode() { return businessDomainCode; }
    /**
     * 获取付款方用户ID。
     */
    public Long getPayerUserId() { return payerUserId; }
    /**
     * 获取收款方用户ID。
     */
    public Long getPayeeUserId() { return payeeUserId; }
    /**
     * 获取单元信息。
     */
    public CurrencyUnit getCurrencyUnit() { return currencyUnit; }
    /**
     * 获取AT信息。
     */
    public LocalDateTime getOccurredAt() { return occurredAt; }
    /**
     * 获取日期信息。
     */
    public LocalDate getPostingDate() { return postingDate; }
    /**
     * 获取KEY信息。
     */
    public String getIdempotencyKey() { return idempotencyKey; }
    /**
     * 获取TXID。
     */
    public String getGlobalTxId() { return globalTxId; }
    /**
     * 获取ID。
     */
    public String getTraceId() { return traceId; }
    /**
     * 获取业务数据。
     */
    public String getPayload() { return payload; }
    /**
     * 获取状态。
     */
    public AccountingEventStatus getStatus() { return status; }
    /**
     * 获取业务数据。
     */
    public List<AccountingLeg> getLegs() { return legs; }
    /**
     * 获取AT信息。
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
    /**
     * 获取AT信息。
     */
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    private static List<AccountingLeg> normalizeLegs(List<AccountingLeg> legs) {
        if (legs == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(legs);
    }

    private static String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
