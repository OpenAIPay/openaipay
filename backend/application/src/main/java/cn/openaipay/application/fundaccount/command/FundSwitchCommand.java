package cn.openaipay.application.fundaccount.command;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * 基金切换命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundSwitchCommand(
        /** 订单号 */
        String orderNo,
        /** 用户ID */
        Long userId,
        /** 来源资金编码 */
        String sourceFundCode,
        /** 目标资金编码 */
        String targetFundCode,
        /** 来源信息 */
        FundAmount sourceShare,
        /** 业务单号 */
        String businessNo
) {
}
