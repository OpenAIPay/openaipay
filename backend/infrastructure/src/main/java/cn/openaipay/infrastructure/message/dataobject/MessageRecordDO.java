package cn.openaipay.infrastructure.message.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import org.joda.money.Money;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

/**
 * 消息持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("message_record")
public class MessageRecordDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 消息ID。 */
    @TableField("message_id")
    private String messageId;

    /** 会话号。 */
    @TableField("conversation_no")
    private String conversationNo;

    /** 发送方用户ID。 */
    @TableField("sender_user_id")
    private Long senderUserId;

    /** 接收方用户ID。 */
    @TableField("receiver_user_id")
    private Long receiverUserId;

    /** 消息类型。 */
    @TableField("message_type")
    private String messageType;

    /** 文本内容。 */
    @TableField("content_text")
    private String contentText;

    /** 媒体资源ID。 */
    @TableField("media_id")
    private String mediaId;

    /** 金额。 */
    @TableField("amount")
    private Money amount;

    /** 交易号。 */
    @TableField("trade_order_no")
    private String tradeOrderNo;

    /** 扩展载荷。 */
    @TableField("ext_payload")
    private String extPayload;

    /** 消息状态。 */
    @TableField("message_status")
    private String messageStatus;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
