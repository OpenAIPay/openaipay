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
 * PositionUnitCreativeRelationDO 持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("deliver_position_unit_creative_relation")
public class PositionUnitCreativeRelationDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;
    /** 展位ID。 */
    @TableField("position_id")
    private Long positionId;
    /** 投放单元ID。 */
    @TableField("unit_id")
    private Long unitId;
    /** 创意ID。 */
    @TableField("creative_id")
    private Long creativeId;
    /** 展示顺序。 */
    @TableField("display_order")
    private Integer displayOrder;
    /** 是否兜底。 */
    @TableField("is_fallback")
    private Boolean fallback;
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
