package cn.openaipay.infrastructure.accounting.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 会计事件日志DO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("acct_event_journal")
public class AccountingEventJournalDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 事件ID */
    @TableField("event_id")
    private String eventId;

    /** 事件类型 */
    @TableField("event_type")
    private String eventType;

    /** 事件版本号 */
    @TableField("event_version")
    private Integer eventVersion;

    /** 业务ID */
    @TableField("book_id")
    private String bookId;

    /** 来源信息 */
    @TableField("source_system")
    private String sourceSystem;

    /** 来源业务类型 */
    @TableField("source_biz_type")
    private String sourceBizType;

    /** 来源业务单号 */
    @TableField("source_biz_no")
    private String sourceBizNo;

    /** 业务单号 */
    @TableField("biz_order_no")
    private String bizOrderNo;

    /** 请求幂等号 */
    @TableField("request_no")
    private String requestNo;

    /** 交易订单单号 */
    @TableField("trade_order_no")
    private String tradeOrderNo;

    /** 支付订单单号 */
    @TableField("pay_order_no")
    private String payOrderNo;

    /** 业务场景编码 */
    @TableField("business_scene_code")
    private String businessSceneCode;

    /** 业务域编码 */
    @TableField("business_domain_code")
    private String businessDomainCode;

    /** 付款方用户ID */
    @TableField("payer_user_id")
    private Long payerUserId;

    /** 收款方用户ID */
    @TableField("payee_user_id")
    private Long payeeUserId;

    /** 业务编码 */
    @TableField("currency_code")
    private String currencyCode;

    /** 业务时间 */
    @TableField("occurred_at")
    private LocalDateTime occurredAt;

    /** 业务日期 */
    @TableField("posting_date")
    private LocalDate postingDate;

    /** 业务键 */
    @TableField("idempotency_key")
    private String idempotencyKey;

    /** TXID */
    @TableField("global_tx_id")
    private String globalTxId;

    /** 业务ID */
    @TableField("trace_id")
    private String traceId;

    /** 业务JSON */
    @TableField("payload_json")
    private String payloadJson;

    /** 业务JSON */
    @TableField("legs_json")
    private String legsJson;

    /** 业务状态 */
    @TableField("process_status")
    private String processStatus;

    /** 当前重试次数 */
    @TableField("retry_count")
    private Integer retryCount;

    /** 失败原因 */
    @TableField("failure_reason")
    private String failureReason;

    /** 业务单号 */
    @TableField("posted_voucher_no")
    private String postedVoucherNo;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
