package cn.openaipay.application.message.command;

import org.joda.money.Money;

/**
 * 发送红包消息命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record SendRedPacketMessageCommand(
        /** 发送方用户ID */
        Long senderUserId,
        /** 接收方用户ID */
        Long receiverUserId,
        /** 金额 */
        Money amount,
        /** 支付方式编码 */
        String paymentMethod,
        /** 扩展载荷 */
        String extPayload
) {
}
