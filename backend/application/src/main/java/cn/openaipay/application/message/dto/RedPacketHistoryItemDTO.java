package cn.openaipay.application.message.dto;

import java.time.LocalDateTime;
import org.joda.money.Money;

/**
 * 红包记录条目数据传输对象
 *
 * 业务场景：用于红包记录页展示单条我发出的/我收到的红包记录。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record RedPacketHistoryItemDTO(
        /** 消息ID */
        String messageId,
        /** 会话单号 */
        String conversationNo,
        /** 方向 */
        String direction,
        /** 用户ID */
        Long counterpartyUserId,
        /** 业务昵称 */
        String counterpartyNickname,
        /** 头像地址 */
        String counterpartyAvatarUrl,
        /** 金额 */
        Money amount,
        /** 交易主单号 */
        String tradeOrderNo,
        /** 消息状态 */
        String messageStatus,
        /** 红包单号 */
        String redPacketNo,
        /** 红包状态 */
        String redPacketStatus,
        /** 记录创建时间 */
        LocalDateTime createdAt
) {
}
