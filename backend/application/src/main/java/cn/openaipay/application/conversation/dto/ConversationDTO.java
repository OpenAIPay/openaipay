package cn.openaipay.application.conversation.dto;

import java.time.LocalDateTime;

/**
 * 会话数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record ConversationDTO(
        /** 会话单号 */
        String conversationNo,
        /** 会话类型 */
        String conversationType,
        /** 用户ID */
        Long userId,
        /** 对端用户ID */
        Long peerUserId,
        /** 对端爱支付UID */
        String peerAipayUid,
        /** 业务昵称 */
        String peerNickname,
        /** 头像地址 */
        String peerAvatarUrl,
        /** 未读数量 */
        long unreadCount,
        /** 最近一次消息ID */
        String lastMessageId,
        /** 最近一次消息信息 */
        String lastMessagePreview,
        /** 最近一次消息时间 */
        LocalDateTime lastMessageAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
