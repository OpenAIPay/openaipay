package cn.openaipay.application.adminmessage.dto;

/**
 * 消息中心概览
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminMessageOverviewDTO(
        /** 会话数量 */
        long conversationCount,
        /** 消息数量 */
        long messageCount,
        /** 红包数量 */
        long redPacketCount,
        /** 待处理好友申请数 */
        long pendingContactRequestCount,
        /** 好友关系数量 */
        long friendshipCount,
        /** 黑名单数量 */
        long blacklistCount
) {
}
