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
 * 会话持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("conversation_session")
public class ConversationSessionDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 会话号。 */
    @TableField("conversation_no")
    private String conversationNo;

    /** 会话类型。 */
    @TableField("conversation_type")
    private String conversationType;

    /** 业务唯一键。 */
    @TableField("biz_key")
    private String bizKey;

    /** 最后一条消息ID。 */
    @TableField("last_message_id")
    private String lastMessageId;

    /** 最后一条消息预览。 */
    @TableField("last_message_preview")
    private String lastMessagePreview;

    /** 最后消息时间。 */
    @TableField("last_message_at")
    private LocalDateTime lastMessageAt;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
