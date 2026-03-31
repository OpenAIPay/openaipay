package cn.openaipay.adapter.trade.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.joda.money.Money;

/**
 * 创建Refund交易请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record CreateRefundTradeRequest(
        /** 请求幂等号 */
        @NotBlank(message = "不能为空") String requestNo,
        /** 业务场景编码 */
        String businessSceneCode,
        /** 原始交易订单单号 */
        @NotBlank(message = "不能为空") String originalTradeOrderNo,
        /** 付款方用户ID */
        @Min(value = 1, message = "必须大于0") Long payerUserId,
        /** 收款方用户ID */
        @Min(value = 1, message = "必须大于0") Long payeeUserId,
        /** 支付方式编码 */
        String paymentMethod,
        /** 金额 */
        @NotNull(message = "不能为空")
                                        @DecimalMin(value = "0.01", message = "必须大于0") Money amount,
        /** 钱包金额 */
        @DecimalMin(value = "0.00", message = "不能小于0") Money walletDebitAmount,
        /** 资金金额 */
        @DecimalMin(value = "0.00", message = "不能小于0") Money fundDebitAmount,
        /** 信用金额 */
        @DecimalMin(value = "0.00", message = "不能小于0") Money creditDebitAmount,
        /** 扩展信息 */
        String metadata
) {
}
