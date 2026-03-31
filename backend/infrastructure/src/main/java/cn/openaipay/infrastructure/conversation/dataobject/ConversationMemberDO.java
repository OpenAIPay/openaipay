package cn.openaipay.infrastructure.conversation.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 会话成员持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("conversation_member")
public class ConversationMemberDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 会话号。 */
    @TableField("conversation_no")
    private String conversationNo;

    /** 用户ID。 */
    @TableField("user_id")
    private Long userId;

    /** 对端用户ID。 */
    @TableField("peer_user_id")
    private Long peerUserId;

    /** 未读消息数。 */
    @TableField("unread_count")
    private Long unreadCount;

    /** 最后已读消息ID。 */
    @TableField("last_read_message_id")
    private String lastReadMessageId;

    /** 最后已读时间。 */
    @TableField("last_read_at")
    private LocalDateTime lastReadAt;

    /** 免打扰标记。 */
    @TableField("mute_flag")
    private Boolean muteFlag;

    /** 置顶标记。 */
    @TableField("pin_flag")
    private Boolean pinFlag;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
