package cn.openaipay.infrastructure.pay.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.joda.money.Money;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 银行卡资金明细持久化实体
 *
 * 业务场景：记录支付资金明细中银行卡出资的渠道、银行订单和渠道手续费等扩展字段，
 * 通过summary_id与支付资金明细摘要表关联。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("pay_bank_card_fund_detail")
public class PayBankCardFundDetailDO {

    /** 关联支付资金明细摘要ID */
    @TableId(value = "summary_id", type = IdType.INPUT)
    private Long summaryId;

    /** 支付渠道标识 */
    @TableField("channel")
    private String channel;

    /** 机构ID（银行机构码） */
    @TableField("inst_id")
    private String instId;

    /** 机构渠道编码 */
    @TableField("inst_channel_code")
    private String instChannelCode;

    /** 支付渠道编码 */
    @TableField("pay_channel_code")
    private String payChannelCode;

    /** 银行编码 */
    @TableField("bank_code")
    private String bankCode;

    /** 银行名称 */
    @TableField("bank_name")
    private String bankName;

    /** 卡类型 */
    @TableField("card_type")
    private String cardType;

    /** 持卡人姓名 */
    @TableField("card_holder_name")
    private String cardHolderName;

    /** 卡尾号 */
    @TableField("card_tail_no")
    private String cardTailNo;

    /** 支付工具快照 */
    @TableField("tool_snapshot")
    private String toolSnapshot;

    /** 银行侧订单号 */
    @TableField("bank_order_no")
    private String bankOrderNo;

    /** 银行卡号 */
    @TableField("bank_card_no")
    private String bankCardNo;

    /** 渠道手续费金额 */
    @TableField("channel_fee_amount")
    private Money channelFeeAmount;

    /** 充值订单号 */
    @TableField("deposit_order_no")
    private String depositOrderNo;

}
