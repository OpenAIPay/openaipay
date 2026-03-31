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
 * DeliverPositionDO 持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("deliver_position")
public class DeliverPositionDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;
    /** 展位编码。 */
    @TableField("position_code")
    private String positionCode;
    /** 展位名称。 */
    @TableField("position_name")
    private String positionName;
    /** 展位类型。 */
    @TableField("position_type")
    private String positionType;
    /** 预览图地址。 */
    @TableField("preview_image")
    private String previewImage;
    /** 轮播间隔。 */
    @TableField("slide_interval")
    private Integer slideInterval;
    /** 最大展示数量。 */
    @TableField("max_display_count")
    private Integer maxDisplayCount;
    /** 排序策略。 */
    @TableField("sort_type")
    private String sortType;
    /** 排序规则。 */
    @TableField("sort_rule")
    private String sortRule;
    /** 是否需要兜底。 */
    @TableField("need_fallback")
    private Boolean needFallback;
    /** 展位发布状态。 */
    @TableField("status")
    private String status;
    /** 备注。 */
    @TableField("memo")
    private String memo;
    /** 发布时间。 */
    @TableField("published_at")
    private LocalDateTime publishedAt;
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
