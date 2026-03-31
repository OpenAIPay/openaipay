package cn.openaipay.application.fundaccount.command;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * 基金收益Settle命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundIncomeSettleCommand(
        /** 订单号 */
        String orderNo,
        /** 用户ID */
        Long userId,
        /** 资金编码 */
        String fundCode,
        /** 收益金额 */
        FundAmount incomeAmount,
        /** NAV信息 */
        FundAmount nav,
        /** 业务单号 */
        String businessNo
) {
}
