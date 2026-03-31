package cn.openaipay.infrastructure.outbound.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出金订单持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("outbound_order")
public class OutboundOrderDO {
    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 出金订单号。 */
    @TableField("outbound_id")
    private String outboundId;

    /** 机构ID。 */
    @TableField("inst_id")
    private String instId;

    /** 财务处理编码。 */
    @TableField("inst_channel_code")
    private String instChannelCode;

    /** 出金订单号。 */
    @TableField("outbound_order_no")
    private String outboundOrderNo;

    /** 收款账号。 */
    @TableField("payee_account_no")
    private String payeeAccountNo;

    /** 处理金额。 */
    @TableField("outbound_amount")
    private BigDecimal outboundAmount;

    /** 处理状态。 */
    @TableField("outbound_status")
    private String outboundStatus;

    /** 结果码。 */
    @TableField("result_code")
    private String resultCode;

    /** 结果说明。 */
    @TableField("result_description")
    private String resultDescription;

    /** 请求标识。 */
    @TableField("request_identify")
    private String requestIdentify;

    /** 业务请求号。 */
    @TableField("request_biz_no")
    private String requestBizNo;

    /** 业务单号。 */
    @TableField("biz_order_no")
    private String bizOrderNo;

    /** 交易单号。 */
    @TableField("trade_order_no")
    private String tradeOrderNo;

    /** 支付单号。 */
    @TableField("pay_order_no")
    private String payOrderNo;

    /** 支付渠道编码。 */
    @TableField("pay_channel_code")
    private String payChannelCode;

    /** 业务身份标识。 */
    @TableField("biz_identity")
    private String bizIdentity;

    /** 提交时间。 */
    @TableField("gmt_submit")
    private LocalDateTime gmtSubmit;

    /** 响应时间。 */
    @TableField("gmt_resp")
    private LocalDateTime gmtResp;

    /** 结算时间。 */
    @TableField("gmt_settle")
    private LocalDateTime gmtSettle;

    /** 创建时间。 */
    @TableField("gmt_create")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("gmt_modified")
    private LocalDateTime updatedAt;

}
