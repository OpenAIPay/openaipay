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
 * DeliverEventRecordDO 持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("deliver_event_record")
public class DeliverEventRecordDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;
    /** 客户端ID。 */
    @TableField("client_id")
    private String clientId;
    /** 用户ID。 */
    @TableField("user_id")
    private Long userId;
    /** 作用实体类型。 */
    @TableField("entity_type")
    private String entityType;
    /** 作用实体编码。 */
    @TableField("entity_code")
    private String entityCode;
    /** 展位编码。 */
    @TableField("position_code")
    private String positionCode;
    /** 投放单元编码。 */
    @TableField("unit_code")
    private String unitCode;
    /** 创意编码。 */
    @TableField("creative_code")
    private String creativeCode;
    /** 事件类型。 */
    @TableField("event_type")
    private String eventType;
    /** 场景编码。 */
    @TableField("scene_code")
    private String sceneCode;
    /** 渠道编码。 */
    @TableField("channel")
    private String channel;
    /** 事件发生时间。 */
    @TableField("event_time")
    private LocalDateTime eventTime;
    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

}
