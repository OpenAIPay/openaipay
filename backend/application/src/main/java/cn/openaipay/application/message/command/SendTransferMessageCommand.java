package cn.openaipay.application.message.command;

import org.joda.money.Money;

/**
 * 发送转账消息命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record SendTransferMessageCommand(
        /** 发送方用户ID */
        Long senderUserId,
        /** 接收方用户ID */
        Long receiverUserId,
        /** 金额 */
        Money amount,
        /** 支付方式编码 */
        String paymentMethod,
        /** 业务编码 */
        String paymentToolCode,
        /** 备注 */
        String remark,
        /** 扩展载荷 */
        String extPayload
) {
}
