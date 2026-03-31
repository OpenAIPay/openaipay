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
 * 人群定义持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("audience_segment")
public class AudienceSegmentDO {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 人群编码 */
    @TableField("segment_code")
    private String segmentCode;

    /** 人群名称 */
    @TableField("segment_name")
    private String segmentName;

    /** 人群描述 */
    @TableField("description")
    private String description;

    /** 场景编码 */
    @TableField("scene_code")
    private String sceneCode;

    /** 人群状态 */
    @TableField("status")
    private String status;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
