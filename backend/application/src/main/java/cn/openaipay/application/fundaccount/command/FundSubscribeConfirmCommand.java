package cn.openaipay.application.fundaccount.command;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * 基金申购Confirm命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundSubscribeConfirmCommand(
        /** 订单号 */
        String orderNo,
        /** 确认份额 */
        FundAmount confirmedShare,
        /** NAV信息 */
        FundAmount nav
) {
}
