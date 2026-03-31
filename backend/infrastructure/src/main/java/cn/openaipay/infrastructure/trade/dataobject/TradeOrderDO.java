package cn.openaipay.infrastructure.trade.dataobject;

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
 * 统一交易主单持久化实体。
 *
 * 业务场景：交易系统负责计费与支付编排，因此主单只保留编排公共字段；业务特有字段通过扩展单表承接。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("trade_order")
public class TradeOrderDO {

    /** 统一交易主单主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 统一交易主单号。 */
    @TableField("trade_order_no")
    private String tradeOrderNo;

    /** 请求幂等号。 */
    @TableField("request_no")
    private String requestNo;

    /** 交易类型，例如 PAY、TRANSFER、REFUND。 */
    @TableField("trade_type")
    private String tradeType;

    /** 交易编排场景编码，例如 TRADE_PAY、TRADE_TRANSFER。 */
    @TableField("business_scene_code")
    private String businessSceneCode;

    /** 业务查询域编码，例如 AICREDIT、AILOAN、AICASH。 */
    @TableField("business_domain_code")
    private String businessDomainCode;

    /** 业务交易单号，在对应业务域内唯一。 */
    @TableField("biz_order_no")
    private String bizOrderNo;

    /** 原交易单号，退款等逆向场景使用。 */
    @TableField("original_trade_order_no")
    private String originalTradeOrderNo;

    /** 付款方用户ID。 */
    @TableField("payer_user_id")
    private Long payerUserId;

    /** 收款方用户ID。 */
    @TableField("payee_user_id")
    private Long payeeUserId;

    /** 支付方式编码。 */
    @TableField("payment_method")
    private String paymentMethod;

    /** 原始交易金额。 */
    @TableField("original_amount")
    private Money originalAmount;

    /** 手续费金额。 */
    @TableField("fee_amount")
    private Money feeAmount;

    /** 应付金额。 */
    @TableField("payable_amount")
    private Money payableAmount;

    /** 结算金额。 */
    @TableField("settle_amount")
    private Money settleAmount;

    /** 支付拆分快照载荷。 */
    @TableField("split_plan_snapshot")
    private String splitPlanSnapshot;

    /** 计费报价单号。 */
    @TableField("pricing_quote_no")
    private String pricingQuoteNo;

    /** 当前生效支付单号。 */
    @TableField("pay_order_no")
    private String payOrderNo;

    /** 最近一次已应用的支付状态版本号。 */
    @TableField("last_pay_status_version")
    private Integer lastPayStatusVersion;

    /** 支付结果码。 */
    @TableField("pay_result_code")
    private String payResultCode;

    /** 支付结果描述。 */
    @TableField("pay_result_message")
    private String payResultMessage;

    /** 统一交易状态。 */
    @TableField("status")
    private String status;

    /** 失败原因。 */
    @TableField("failure_reason")
    private String failureReason;

    /** 交易扩展信息快照。 */
    @TableField("metadata")
    private String metadata;

    /** 支付工具列表快照。 */
    @TableField("payment_tool_snapshot")
    private String paymentToolSnapshot;

    /** 记录创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
