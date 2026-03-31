package cn.openaipay.infrastructure.deliver.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

/**
 * TargetingRuleDO 持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("deliver_targeting_rule")
public class TargetingRuleDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;
    /** 规则编码。 */
    @TableField("rule_code")
    private String ruleCode;
    /** 作用实体类型。 */
    @TableField("entity_type")
    private String entityType;
    /** 作用实体编码。 */
    @TableField("entity_code")
    private String entityCode;
    /** 定向类型。 */
    @TableField("targeting_type")
    private String targetingType;
    /** 定向操作符。 */
    @TableField("operator")
    private String operator;
    /** 定向规则值。 */
    @TableField("targeting_value")
    private String targetingValue;
    /** 是否启用。 */
    @TableField("enabled")
    private Boolean enabled;
    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;
    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
