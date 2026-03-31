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
 * DeliverUnitDO 持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("deliver_unit")
public class DeliverUnitDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;
    /** 投放单元编码。 */
    @TableField("unit_code")
    private String unitCode;
    /** 投放单元名称。 */
    @TableField("unit_name")
    private String unitName;
    /** 优先级。 */
    @TableField("priority")
    private Integer priority;
    /** 投放单元发布状态。 */
    @TableField("status")
    private String status;
    /** 备注。 */
    @TableField("memo")
    private String memo;
    /** 生效开始时间。 */
    @TableField("active_from")
    private LocalDateTime activeFrom;
    /** 生效结束时间。 */
    @TableField("active_to")
    private LocalDateTime activeTo;
    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;
    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
