package cn.openaipay.application.fundaccount.command;
/**
 * 基金赎回Cancel命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundRedeemCancelCommand(
        /** 订单号 */
        String orderNo
) {
}
