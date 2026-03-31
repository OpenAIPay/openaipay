package cn.openaipay.infrastructure.trade.dataobject;

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
 * 信用业务交易扩展单持久化实体。
 *
 * 业务场景：爱花、爱借等信用业务需要记录账单号、还款计划号以及本金利息拆分，统一交易主单无法承载这些字段。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("trade_credit_order")
public class TradeCreditOrderDO {

    /** 信用业务交易扩展单主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 信用业务交易单号。 */
    @TableField("biz_order_no")
    private String bizOrderNo;

    /** 统一交易主单号。 */
    @TableField("trade_order_no")
    private String tradeOrderNo;

    /** 信用产品类型。 */
    @TableField("credit_product_type")
    private String creditProductType;

    /** 信用账户号。 */
    @TableField("credit_account_no")
    private String creditAccountNo;

    /** 账单号。 */
    @TableField("bill_no")
    private String billNo;

    /** 账单月份。 */
    @TableField("bill_month")
    private String billMonth;

    /** 还款计划号。 */
    @TableField("repayment_plan_no")
    private String repaymentPlanNo;

    /** 信用业务交易类型。 */
    @TableField("credit_trade_type")
    private String creditTradeType;

    /** 主体金额。 */
    @TableField("subject_amount")
    private Money subjectAmount;

    /** 本金金额。 */
    @TableField("principal_amount")
    private Money principalAmount;

    /** 利息金额。 */
    @TableField("interest_amount")
    private Money interestAmount;

    /** 费用金额。 */
    @TableField("fee_amount")
    private Money feeAmount;

    /** 交易对手名称。 */
    @TableField("counterparty_name")
    private String counterpartyName;

    /** 业务发生时间。 */
    @TableField("occurred_at")
    private LocalDateTime occurredAt;

    /** 记录创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
