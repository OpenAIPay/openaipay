package cn.openaipay.application.fundaccount.command;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * 基金切换Confirm命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundSwitchConfirmCommand(
        /** 订单号 */
        String orderNo,
        /** 来源NAV信息 */
        FundAmount sourceNav,
        /** 目标NAV信息 */
        FundAmount targetNav
) {
}
