package cn.openaipay.domain.pay.client;

import cn.openaipay.domain.shared.number.FundAmount;

/**
 * PayFundFreezeResultSnapshot 记录模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record PayFundFreezeResultSnapshot(
        /** 资金编码 */
        String fundCode,
        /** 份额 */
        FundAmount share,
        /** NAV信息 */
        FundAmount nav
) {
}
