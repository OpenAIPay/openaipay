package cn.openaipay.application.message.dto;

import java.time.LocalDateTime;
import org.joda.money.Money;

/**
 * 红包详情数据传输对象。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record RedPacketDetailDTO(
        /** 红包单号 */
        String redPacketNo,
        /** 消息ID */
        String messageId,
        /** 会话单号 */
        String conversationNo,
        /** 发送方用户ID */
        Long senderUserId,
        /** 业务昵称 */
        String senderNickname,
        /** 头像地址 */
        String senderAvatarUrl,
        /** 接收方用户ID */
        Long receiverUserId,
        /** 业务昵称 */
        String receiverNickname,
        /** 头像地址 */
        String receiverAvatarUrl,
        /** 用户ID */
        Long holdingUserId,
        /** 金额 */
        Money amount,
        /** 支付方式编码 */
        String paymentMethod,
        /** 业务ID */
        String coverId,
        /** 封面标题 */
        String coverTitle,
        /** 祝福语 */
        String blessingText,
        /** 状态编码 */
        String status,
        /** 资金交易单号 */
        String fundingTradeNo,
        /** 领取交易单号 */
        String claimTradeNo,
        /** claimableBYviewer信息 */
        boolean claimableByViewer,
        /** claimedBYviewer信息 */
        boolean claimedByViewer,
        /** 领取时间 */
        LocalDateTime claimedAt,
        /** 记录创建时间 */
        LocalDateTime createdAt
) {
}
