package cn.openaipay.infrastructure.pay.dataobject;

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
 * 支付资金明细摘要持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("pay_fund_detail_summary")
public class PayFundDetailSummaryDO {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 支付单号 */
    @TableField("pay_order_no")
    private String payOrderNo;

    /** 支付工具 */
    @TableField("pay_tool")
    private String payTool;

    /** 归属方 */
    @TableField("detail_owner")
    private String detailOwner;

    /** 金额 */
    @TableField("amount")
    private Money amount;

    /** 累计退款金额 */
    @TableField("cumulative_refund_amount")
    private Money cumulativeRefundAmount;

    /** 明细类型 */
    @TableField("detail_type")
    private String detailType;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
