package cn.openaipay.infrastructure.accounting.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.joda.money.Money;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会计凭证DO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("acct_voucher")
public class AccountingVoucherDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 业务单号 */
    @TableField("voucher_no")
    private String voucherNo;

    /** 业务ID */
    @TableField("book_id")
    private String bookId;

    /** 业务类型 */
    @TableField("voucher_type")
    private String voucherType;

    /** 事件ID */
    @TableField("event_id")
    private String eventId;

    /** 来源业务类型 */
    @TableField("source_biz_type")
    private String sourceBizType;

    /** 来源业务单号 */
    @TableField("source_biz_no")
    private String sourceBizNo;

    /** 业务单号 */
    @TableField("biz_order_no")
    private String bizOrderNo;

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

    /** 当前状态编码 */
    @TableField("status")
    private String status;

    /** 总金额 */
    @TableField("total_debit_amount")
    private Money totalDebitAmount;

    /** 总信用金额 */
    @TableField("total_credit_amount")
    private Money totalCreditAmount;

    /** 业务时间 */
    @TableField("occurred_at")
    private LocalDateTime occurredAt;

    /** 业务日期 */
    @TableField("posting_date")
    private LocalDate postingDate;

    /** 业务单号 */
    @TableField("reversed_voucher_no")
    private String reversedVoucherNo;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
