package cn.openaipay.application.adminmessage.dto;

import java.time.LocalDateTime;
import org.joda.money.Money;

/**
 * 红包记录行
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminRedPacketRowDTO(
        /** 红包单号 */
        String redPacketNo,
        /** 会话单号 */
        String conversationNo,
        /** 消息ID */
        String messageId,
        /** 发送方用户ID */
        Long senderUserId,
        /** 发送方展示名称 */
        String senderDisplayName,
        /** 接收方用户ID */
        Long receiverUserId,
        /** 接收方展示名称 */
        String receiverDisplayName,
        /** 金额 */
        Money amount,
        /** 币种编码 */
        String currencyCode,
        /** 支付方式编码 */
        String paymentMethod,
        /** 状态编码 */
        String status,
        /** 封面标题 */
        String coverTitle,
        /** 祝福语 */
        String blessingText,
        /** 资金交易单号 */
        String fundingTradeNo,
        /** 领取交易单号 */
        String claimTradeNo,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 领取时间 */
        LocalDateTime claimedAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
