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
 * DeliverCreativeDO 持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("deliver_creative")
public class DeliverCreativeDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;
    /** 创意编码。 */
    @TableField("creative_code")
    private String creativeCode;
    /** 创意名称。 */
    @TableField("creative_name")
    private String creativeName;
    /** 投放单元编码。 */
    @TableField("unit_code")
    private String unitCode;
    /** 素材编码。 */
    @TableField("material_code")
    private String materialCode;
    /** 落地页地址。 */
    @TableField("landing_url")
    private String landingUrl;
    /** 端内跳转 Schema 配置。 */
    @TableField("schema_json")
    private String schemaJson;
    /** 优先级。 */
    @TableField("priority")
    private Integer priority;
    /** 权重。 */
    @TableField("weight")
    private Integer weight;
    /** 是否兜底。 */
    @TableField("is_fallback")
    private Boolean fallback;
    /** 预览图地址。 */
    @TableField("preview_image")
    private String previewImage;
    /** 创意发布状态。 */
    @TableField("status")
    private String status;
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
