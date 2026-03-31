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
 * 红包订单持久化实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("message_red_packet_order")
public class RedPacketOrderDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;
    /** 红包单号。 */
    @TableField("red_packet_no")
    private String redPacketNo;
    /** 消息ID。 */
    @TableField("message_id")
    private String messageId;
    /** 会话号。 */
    @TableField("conversation_no")
    private String conversationNo;
    /** 发红包用户ID。 */
    @TableField("sender_user_id")
    private Long senderUserId;
    /** 收红包用户ID。 */
    @TableField("receiver_user_id")
    private Long receiverUserId;
    /** 红包中间账户用户ID。 */
    @TableField("holding_user_id")
    private Long holdingUserId;
    /** 红包金额。 */
    @TableField("amount")
    private Money amount;
    /** 发出交易号。 */
    @TableField("funding_trade_no")
    private String fundingTradeNo;
    /** 领取交易号。 */
    @TableField("claim_trade_no")
    private String claimTradeNo;
    /** 付款方式。 */
    @TableField("payment_method")
    private String paymentMethod;
    /** 封面ID。 */
    @TableField("cover_id")
    private String coverId;
    /** 封面标题。 */
    @TableField("cover_title")
    private String coverTitle;
    /** 祝福语。 */
    @TableField("blessing_text")
    private String blessingText;
    /** 红包状态。 */
    @TableField("status")
    private String status;
    /** 领取时间。 */
    @TableField("claimed_at")
    private LocalDateTime claimedAt;
    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;
    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
