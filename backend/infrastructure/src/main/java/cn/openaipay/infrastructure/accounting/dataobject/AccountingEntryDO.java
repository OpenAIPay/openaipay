package cn.openaipay.infrastructure.accounting.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.joda.money.Money;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 会计分录DO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("acct_entry")
public class AccountingEntryDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 业务单号 */
    @TableField("voucher_no")
    private String voucherNo;

    /** 业务单号 */
    @TableField("line_no")
    private Integer lineNo;

    /** 科目编码 */
    @TableField("subject_code")
    private String subjectCode;

    /** DC标记 */
    @TableField("dc_flag")
    private String dcFlag;

    /** 交易金额 */
    @TableField("amount")
    private Money amount;

    /** 所属类型 */
    @TableField("owner_type")
    private String ownerType;

    /** 所属ID */
    @TableField("owner_id")
    private Long ownerId;

    /** 域信息 */
    @TableField("account_domain")
    private String accountDomain;

    /** 业务类型 */
    @TableField("account_type")
    private String accountType;

    /** 业务单号 */
    @TableField("account_no")
    private String accountNo;

    /** 业务角色信息 */
    @TableField("biz_role")
    private String bizRole;

    /** 业务单号 */
    @TableField("biz_order_no")
    private String bizOrderNo;

    /** 交易订单单号 */
    @TableField("trade_order_no")
    private String tradeOrderNo;

    /** 支付订单单号 */
    @TableField("pay_order_no")
    private String payOrderNo;

    /** 来源业务类型 */
    @TableField("source_biz_type")
    private String sourceBizType;

    /** 来源业务单号 */
    @TableField("source_biz_no")
    private String sourceBizNo;

    /** 业务单号 */
    @TableField("reference_no")
    private String referenceNo;

    /** 分录备注 */
    @TableField("entry_memo")
    private String entryMemo;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
