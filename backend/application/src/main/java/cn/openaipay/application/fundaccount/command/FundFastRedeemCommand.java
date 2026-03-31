package cn.openaipay.application.fundaccount.command;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * 基金Fast赎回命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundFastRedeemCommand(
        /** 订单号 */
        String orderNo,
        /** 用户ID */
        Long userId,
        /** 资金编码 */
        String fundCode,
        /** 份额 */
        FundAmount share,
        /** 业务单号 */
        String businessNo,
        /** 转出去向：BALANCE/BANK_CARD */
        String redeemDestination,
        /** 转出银行卡名称 */
        String bankName
) {
}
