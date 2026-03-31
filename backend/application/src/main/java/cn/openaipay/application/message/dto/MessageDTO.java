package cn.openaipay.application.message.dto;

import java.time.LocalDateTime;
import org.joda.money.Money;

/**
 * 消息数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record MessageDTO(
        /** 消息ID */
        String messageId,
        /** 会话单号 */
        String conversationNo,
        /** 发送方用户ID */
        Long senderUserId,
        /** 接收方用户ID */
        Long receiverUserId,
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
        /** 扩展载荷 */
        String extPayload,
        /** 消息状态 */
        String messageStatus,
        /** 记录创建时间 */
        LocalDateTime createdAt
) {
}
