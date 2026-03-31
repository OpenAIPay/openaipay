package cn.openaipay.infrastructure.inbound.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.money.Money;

/**
 * 入金订单持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("inbound_order")
public class InboundOrderDO {
    @TableId(value = "id", type = IdType.AUTO)
    /** 主键ID。 */
    private Long id;

    @TableField("inbound_id")
    /** 入金订单号。 */
    private String inboundId;

    @TableField(exist = false)
    /** 原始入金订单号。 */
    private String orgInboundId;

    @TableField("inst_id")
    /** 机构ID。 */
    private String instId;

    @TableField("inst_channel_code")
    /** 财务处理编码。 */
    private String instChannelCode;

    @TableField("inbound_order_no")
    /** 入金订单号。 */
    private String inboundOrderNo;

    @TableField("payer_account_no")
    /** 付款账号。 */
    private String payerAccountNo;

    @TableField("inbound_amount")
    /** 处理金额。 */
    private Money inboundAmount;

    @TableField("account_amount")
    /** 账户金额。 */
    private Money accountAmount;

    @TableField("settle_amount")
    /** 结算金额。 */
    private Money settleAmount;

    @TableField("inbound_status")
    /** 处理状态。 */
    private String inboundStatus;

    @TableField("result_code")
    /** 结果码。 */
    private String resultCode;

    @TableField("result_description")
    /** 结果说明。 */
    private String resultDescription;

    @TableField(exist = false)
    /** 恢复标记。 */
    private String recoverFlag;

    @TableField(exist = false)
    /** 对账标记。 */
    private String reconFlag;

    @TableField(exist = false)
    /** 冲正标记。 */
    private String negativeFlag;

    @TableField(exist = false)
    /** 冲正处理类型。 */
    private String negativeInboundType;

    @TableField("request_identify")
    /** 请求标识。 */
    private String requestIdentify;

    @TableField("request_biz_no")
    /** 业务请求号。 */
    private String requestBizNo;

    @TableField("biz_order_no")
    /** 业务单号。 */
    private String bizOrderNo;

    @TableField("trade_order_no")
    /** 交易单号。 */
    private String tradeOrderNo;

    @TableField("pay_order_no")
    /** 支付单号。 */
    private String payOrderNo;

    @TableField("pay_channel_code")
    /** 支付渠道编码。 */
    private String payChannelCode;

    @TableField("biz_identity")
    /** 业务身份标识。 */
    private String bizIdentity;

    @TableField("gmt_submit")
    /** 提交时间。 */
    private LocalDateTime gmtSubmit;

    @TableField("gmt_resp")
    /** 响应时间。 */
    private LocalDateTime gmtResp;

    @TableField("gmt_settle")
    /** 结算时间。 */
    private LocalDateTime gmtSettle;

    @TableField("gmt_create")
    /** 创建时间。 */
    private LocalDateTime createdAt;

    @TableField("gmt_modified")
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
