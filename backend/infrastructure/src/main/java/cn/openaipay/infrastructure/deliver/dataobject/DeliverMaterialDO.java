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
 * DeliverMaterialDO 持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("deliver_material")
public class DeliverMaterialDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;
    /** 素材编码。 */
    @TableField("material_code")
    private String materialCode;
    /** 素材名称。 */
    @TableField("material_name")
    private String materialName;
    /** 素材类型。 */
    @TableField("material_type")
    private String materialType;
    /** 素材标题。 */
    @TableField("title")
    private String title;
    /** 图片地址。 */
    @TableField("image_url")
    private String imageUrl;
    /** 落地页地址。 */
    @TableField("landing_url")
    private String landingUrl;
    /** 端内跳转 Schema 配置。 */
    @TableField("schema_json")
    private String schemaJson;
    /** 预览图地址。 */
    @TableField("preview_image")
    private String previewImage;
    /** 素材发布状态。 */
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
