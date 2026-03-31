package cn.openaipay.infrastructure.pricing.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.joda.money.Money;
import cn.openaipay.domain.shared.number.RateValue;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pricing报价持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("pricing_quote")
public class PricingQuoteDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 报价单号 */
    @TableField("quote_no")
    private String quoteNo;

    /** 请求流水号 */
    @TableField("request_no")
    private String requestNo;

    /** 规则标识 */
    @TableField("rule_id")
    private Long ruleId;

    /** 计费规则编码 */
    @TableField("rule_code")
    private String ruleCode;

    /** 规则名称 */
    @TableField("rule_name")
    private String ruleName;

    /** 业务场景编码 */
    @TableField("business_scene_code")
    private String businessSceneCode;

    /** 支付方式 */
    @TableField("payment_method")
    private String paymentMethod;

    /** 原始金额 */
    @TableField("original_amount")
    private Money originalAmount;

    /** 手续费金额 */
    @TableField("fee_amount")
    private Money feeAmount;

    /** 应付金额 */
    @TableField("payable_amount")
    private Money payableAmount;

    /** 结算金额 */
    @TableField("settle_amount")
    private Money settleAmount;

    /** 计费模式 */
    @TableField("fee_mode")
    private String feeMode;

    /** 手续费承担方 */
    @TableField("fee_bearer")
    private String feeBearer;

    /** 手续费费率 */
    @TableField("fee_rate")
    private RateValue feeRate;

    /** 固定费用 */
    @TableField("fixed_fee")
    private Money fixedFee;

    /** 规则载荷 */
    @TableField("rule_payload")
    private String rulePayload;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
