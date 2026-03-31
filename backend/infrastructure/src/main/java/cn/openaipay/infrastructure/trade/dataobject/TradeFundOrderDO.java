package cn.openaipay.infrastructure.trade.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.joda.money.Money;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 基金业务交易扩展单持久化实体。
 *
 * 业务场景：爱存真实交易除了金额，还要记录基金账户号、账单号、份额和净值日期，方便后台按业务域排查。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("trade_fund_order")
public class TradeFundOrderDO {

    /** 基金业务交易扩展单主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 基金业务交易单号。 */
    @TableField("biz_order_no")
    private String bizOrderNo;

    /** 统一交易主单号。 */
    @TableField("trade_order_no")
    private String tradeOrderNo;

    /** 基金产品类型。 */
    @TableField("fund_product_type")
    private String fundProductType;

    /** 基金账户号。 */
    @TableField("fund_account_no")
    private String fundAccountNo;

    /** 基金账单号。 */
    @TableField("bill_no")
    private String billNo;

    /** 基金账单月份。 */
    @TableField("bill_month")
    private String billMonth;

    /** 基金业务交易类型。 */
    @TableField("fund_trade_type")
    private String fundTradeType;

    /** 份额变化量。 */
    @TableField("share_amount")
    private BigDecimal shareAmount;

    /** 确认金额。 */
    @TableField("confirm_amount")
    private Money confirmAmount;

    /** 净值日期。 */
    @TableField("nav_date")
    private LocalDate navDate;

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
