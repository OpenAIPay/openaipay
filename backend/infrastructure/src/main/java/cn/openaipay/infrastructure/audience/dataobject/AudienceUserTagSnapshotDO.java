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
 * 用户标签快照持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("audience_user_tag_snapshot")
public class AudienceUserTagSnapshotDO {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 标签编码 */
    @TableField("tag_code")
    private String tagCode;

    /** 标签值 */
    @TableField("tag_value")
    private String tagValue;

    /** 数据来源 */
    @TableField("source")
    private String source;

    /** 标签值更新时间 */
    @TableField("value_updated_at")
    private LocalDateTime valueUpdatedAt;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
