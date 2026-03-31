package cn.openaipay.infrastructure.risk.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 风控规则持久化实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("risk_rule")
public class RiskRuleDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /** 规则编码。 */
    @TableField("rule_code")
    private String ruleCode;
    /** 场景编码。 */
    @TableField("scene_code")
    private String sceneCode;
    /** 规则类型。 */
    @TableField("rule_type")
    private String ruleType;
    /** 作用域类型。 */
    @TableField("scope_type")
    private String scopeType;
    /** 作用域值。 */
    @TableField("scope_value")
    private String scopeValue;
    /** 阈值金额。 */
    @TableField("threshold_amount")
    private BigDecimal thresholdAmount;
    /** 币种编码。 */
    @TableField("currency_code")
    private String currencyCode;
    /** 优先级。 */
    @TableField("priority")
    private Integer priority;
    /** 状态。 */
    @TableField("status")
    private String status;
    /** 描述。 */
    @TableField("rule_desc")
    private String ruleDesc;
    /** 更新人。 */
    @TableField("updated_by")
    private String updatedBy;
    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;
    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

