package cn.openaipay.infrastructure.user.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户最近联系人持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("user_recent_contact")
public class UserRecentContactDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 联系人所有者用户ID */
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 最近互动联系人用户ID */
    @TableField("contact_user_id")
    private Long contactUserId;

    /** 最近互动场景编码 */
    @TableField("interaction_scene_code")
    private String interactionSceneCode;

    /** 最近互动备注 */
    @TableField("remark")
    private String remark;

    /** 最近互动时间 */
    @TableField("last_interaction_at")
    private LocalDateTime lastInteractionAt;

    /** 历史互动次数 */
    @TableField("interaction_count")
    private Long interactionCount;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
