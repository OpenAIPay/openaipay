package cn.openaipay.domain.conversation.model;

import java.time.LocalDateTime;

/**
 * 会话列表摘要模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record ConversationSummary(
        /** 会话号。 */
        String conversationNo,
        /** 会话类型。 */
        String conversationType,
        /** 用户ID */
        Long userId,
        /** 对端用户ID */
        Long peerUserId,
        /** 对端爱支付UID */
        String peerAipayUid,
        /** 对端昵称。 */
        String peerNickname,
        /** 对端头像地址。 */
        String peerAvatarUrl,
        /** 未读数量 */
        long unreadCount,
        /** 最后一条消息ID。 */
        String lastMessageId,
        /** 最后一条消息预览。 */
        String lastMessagePreview,
        /** 最后消息时间。 */
        LocalDateTime lastMessageAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
