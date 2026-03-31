package cn.openaipay.infrastructure.pricing.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.joda.money.Money;
import cn.openaipay.domain.shared.number.RateValue;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

/**
 * Pricing规则持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("pricing_rule")
public class PricingRuleDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

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

    /** 币种编码 */
    @TableField("currency_code")
    private String currencyCode;

    /** 计费模式 */
    @TableField("fee_mode")
    private String feeMode;

    /** 手续费费率 */
    @TableField("fee_rate")
    private RateValue feeRate;

    /** 固定费用 */
    @TableField("fixed_fee")
    private Money fixedFee;

    /** 最低费用 */
    @TableField("min_fee")
    private Money minFee;

    /** 最高费用 */
    @TableField("max_fee")
    private Money maxFee;

    /** 手续费承担方 */
    @TableField("fee_bearer")
    private String feeBearer;

    /** 优先级 */
    @TableField("priority")
    private Integer priority;

    /** 业务状态值 */
    @TableField("status")
    private String status;

    /** 生效开始 */
    @TableField("valid_from")
    private LocalDateTime validFrom;

    /** 生效结束 */
    @TableField("valid_to")
    private LocalDateTime validTo;

    /** 规则载荷 */
    @TableField("rule_payload")
    private String rulePayload;

    /** 创建人 */
    @TableField("created_by")
    private String createdBy;

    /** 更新人 */
    @TableField("updated_by")
    private String updatedBy;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
