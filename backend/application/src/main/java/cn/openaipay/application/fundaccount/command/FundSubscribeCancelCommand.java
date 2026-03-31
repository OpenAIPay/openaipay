package cn.openaipay.application.fundaccount.command;
/**
 * 基金申购Cancel命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundSubscribeCancelCommand(
        /** 订单号 */
        String orderNo
) {
}
