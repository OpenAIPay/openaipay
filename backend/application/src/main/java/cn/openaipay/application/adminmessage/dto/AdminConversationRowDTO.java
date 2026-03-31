package cn.openaipay.application.adminmessage.dto;

import java.time.LocalDateTime;

/**
 * 会话列表行
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminConversationRowDTO(
        /** 会话单号 */
        String conversationNo,
        /** 会话类型 */
        String conversationType,
        /** 业务键 */
        String bizKey,
        /** 最近消息预览 */
        String lastMessagePreview,
        /** 最近消息时间 */
        LocalDateTime lastMessageAt,
        /** 成员数量 */
        Long memberCount,
        /** 未读数量 */
        Long unreadCount,
        /** 对端用户ID */
        Long peerUserId,
        /** 对端展示名称 */
        String peerDisplayName,
        /** 对端爱付UID */
        String peerAipayUid
) {
}
