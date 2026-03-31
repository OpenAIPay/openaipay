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
 * FatigueControlRuleDO 持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("deliver_fatigue_control_rule")
public class FatigueControlRuleDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;
    /** 疲劳度规则编码。 */
    @TableField("fatigue_code")
    private String fatigueCode;
    /** 规则名称。 */
    @TableField("rule_name")
    private String ruleName;
    /** 作用实体类型。 */
    @TableField("entity_type")
    private String entityType;
    /** 作用实体编码。 */
    @TableField("entity_code")
    private String entityCode;
    /** 事件类型。 */
    @TableField("event_type")
    private String eventType;
    /** 统计时间窗口（分钟）。 */
    @TableField("time_window_minutes")
    private Integer timeWindowMinutes;
    /** 最大触发次数。 */
    @TableField("max_count")
    private Integer maxCount;
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
