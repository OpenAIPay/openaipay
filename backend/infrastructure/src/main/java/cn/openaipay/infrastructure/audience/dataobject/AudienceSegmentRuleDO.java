package cn.openaipay.infrastructure.audience.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 人群规则持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("audience_segment_rule")
public class AudienceSegmentRuleDO {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 规则编码 */
    @TableField("rule_code")
    private String ruleCode;

    /** 人群编码 */
    @TableField("segment_code")
    private String segmentCode;

    /** 标签编码 */
    @TableField("tag_code")
    private String tagCode;

    /** 操作符 */
    @TableField("operator")
    private String operator;

    /** 目标值 */
    @TableField("target_value")
    private String targetValue;

    /** 规则归属 */
    @TableField("relation_type")
    private String relationType;

    /** 是否启用 */
    @TableField("enabled")
    private Boolean enabled;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
