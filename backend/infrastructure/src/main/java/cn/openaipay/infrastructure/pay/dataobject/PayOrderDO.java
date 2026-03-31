package cn.openaipay.infrastructure.pay.dataobject;

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
 * 支付订单持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("pay_order")
public class PayOrderDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 支付单号 */
    @TableField("pay_order_no")
    private String payOrderNo;

    /** 交易单号 */
    @TableField("trade_order_no")
    private String tradeOrderNo;

    /** 业务订单编号 */
    @TableField("biz_order_no")
    private String bizOrderNo;

    /** 来源业务类型 */
    @TableField("source_biz_type")
    private String sourceBizType;

    /** 来源业务单号 */
    @TableField("source_biz_no")
    private String sourceBizNo;

    /** 来源业务下的支付尝试序号 */
    @TableField("attempt_no")
    private Integer attemptNo;

    /** 来源业务执行快照 */
    @TableField("source_biz_snapshot")
    private String sourceBizSnapshot;

    /** 业务场景编码 */
    @TableField("business_scene_code")
    private String businessSceneCode;

    /** 付款方用户ID */
    @TableField("payer_user_id")
    private Long payerUserId;

    /** 收款方用户ID */
    @TableField("payee_user_id")
    private Long payeeUserId;

    /** 原始金额 */
    @TableField("original_amount")
    private Money originalAmount;

    /** 优惠金额 */
    @TableField("discount_amount")
    private Money discountAmount;

    /** 应付金额 */
    @TableField("payable_amount")
    private Money payableAmount;

    /** 实付金额 */
    @TableField("actual_paid_amount")
    private Money actualPaidAmount;

    /** 扣款拆分计划快照 */
    @TableField("split_plan_snapshot")
    private String splitPlanSnapshot;

    /** 券编号 */
    @TableField("coupon_no")
    private String couponNo;

    /** 结算计划快照载荷 */
    @TableField("settlement_plan_snapshot")
    private String settlementPlanSnapshot;

    /** 全局事务号 */
    @TableField("global_tx_id")
    private String globalTxId;

    /** 业务状态值 */
    @TableField("status")
    private String status;

    /** 支付状态版本号 */
    @TableField("status_version")
    private Integer statusVersion;

    /** 支付结果码 */
    @TableField("result_code")
    private String resultCode;

    /** 支付结果描述 */
    @TableField("result_message")
    private String resultMessage;

    /** 失败原因描述 */
    @TableField("failure_reason")
    private String failureReason;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
