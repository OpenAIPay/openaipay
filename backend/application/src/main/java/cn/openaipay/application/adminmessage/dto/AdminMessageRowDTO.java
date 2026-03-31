package cn.openaipay.application.adminmessage.dto;

import java.time.LocalDateTime;
import org.joda.money.Money;

/**
 * 消息记录行
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminMessageRowDTO(
        /** 消息ID */
        String messageId,
        /** 会话单号 */
        String conversationNo,
        /** 发送方用户ID */
        Long senderUserId,
        /** 发送方展示名称 */
        String senderDisplayName,
        /** 接收方用户ID */
        Long receiverUserId,
        /** 接收方展示名称 */
        String receiverDisplayName,
        /** 消息类型 */
        String messageType,
        /** 文本内容 */
        String contentText,
        /** 媒体资源ID */
        String mediaId,
        /** 金额 */
        Money amount,
        /** 交易主单号 */
        String tradeOrderNo,
        /** 消息状态 */
        String messageStatus,
        /** 扩展载荷 */
        String extPayload,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
