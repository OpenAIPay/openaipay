package cn.openaipay.application.fundaccount.command;
/**
 * 基金赎回Confirm命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundRedeemConfirmCommand(
        /** 订单号 */
        String orderNo
) {
}
