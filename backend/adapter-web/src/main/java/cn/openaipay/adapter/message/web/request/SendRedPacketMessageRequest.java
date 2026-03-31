package cn.openaipay.adapter.message.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.joda.money.Money;

/**
 * 发送红包消息请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record SendRedPacketMessageRequest(
        /** 发送方用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long senderUserId,
        /** 接收方用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long receiverUserId,
        /** 金额 */
        @NotNull(message = "不能为空") Money amount,
        /** 支付方式编码 */
        String paymentMethod,
        /** 扩展载荷 */
        String extPayload
) {
}
