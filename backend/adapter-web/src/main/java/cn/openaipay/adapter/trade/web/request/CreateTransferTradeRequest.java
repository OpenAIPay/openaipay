package cn.openaipay.adapter.trade.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建Transfer交易请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record CreateTransferTradeRequest(
        /** 请求幂等号 */
        @NotBlank(message = "不能为空") String requestNo,
        /** 业务场景编码 */
        String businessSceneCode,
        /** 付款方用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long payerUserId,
        /** 收款方用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long payeeUserId,
        /** 支付方式编码 */
        String paymentMethod,
        /** 币种编码 */
        String currencyCode,
        /** 金额 */
        @NotNull(message = "不能为空")
                                        @DecimalMin(value = "0.01", message = "必须大于0") BigDecimal amount,
        /** 钱包金额 */
        @DecimalMin(value = "0.00", message = "不能小于0") BigDecimal walletDebitAmount,
        /** 资金金额 */
        @DecimalMin(value = "0.00", message = "不能小于0") BigDecimal fundDebitAmount,
        /** 信用金额 */
        @DecimalMin(value = "0.00", message = "不能小于0") BigDecimal creditDebitAmount,
        /** 入金金额 */
        @DecimalMin(value = "0.00", message = "不能小于0") BigDecimal inboundDebitAmount,
        /** 业务编码 */
        String paymentToolCode,
        /** 扩展信息 */
        String metadata
) {
}
