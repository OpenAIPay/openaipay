package cn.openaipay.application.adminmessage.dto;

import java.time.LocalDateTime;

/**
 * 会话成员行
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminConversationMemberRowDTO(
        /** 会话单号 */
        String conversationNo,
        /** 用户ID */
        Long userId,
        /** 用户展示名称 */
        String userDisplayName,
        /** 用户爱付UID */
        String userAipayUid,
        /** 对端用户ID */
        Long peerUserId,
        /** 对端展示名称 */
        String peerDisplayName,
        /** 对端爱付UID */
        String peerAipayUid,
        /** 未读数量 */
        Long unreadCount,
        /** 最近已读消息ID */
        String lastReadMessageId,
        /** 最近已读时间 */
        LocalDateTime lastReadAt,
        /** 免打扰标记 */
        Boolean muteFlag,
        /** 置顶标记 */
        Boolean pinFlag,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
