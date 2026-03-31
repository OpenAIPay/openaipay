package cn.openaipay.application.fundaccount.command;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * 基金申购命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundSubscribeCommand(
        /** 订单号 */
        String orderNo,
        /** 用户ID */
        Long userId,
        /** 资金编码 */
        String fundCode,
        /** 金额 */
        FundAmount amount,
        /** 业务单号 */
        String businessNo
) {
}
