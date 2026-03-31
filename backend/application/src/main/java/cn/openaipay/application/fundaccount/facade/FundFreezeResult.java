package cn.openaipay.application.fundaccount.facade;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * 基金FreezeResult记录
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundFreezeResult(
        /** 资金编码 */
        String fundCode,
        /** 份额 */
        FundAmount share,
        /** NAV信息 */
        FundAmount nav
) {
}
