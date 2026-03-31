package cn.openaipay.application.cashier.dto;

import java.util.List;

/**
 * 收银台场景配置数据传输对象，用于把后端判定好的可用渠道与银行卡准入策略返回给前端。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public record CashierSceneConfigurationDTO(
        /** 当前收银台允许展示的付款渠道编码集合。 */
        List<String> supportedChannels,
        /** 当前场景的银行卡准入策略编码。 */
        String bankCardPolicy,
        /** 当银行卡不可用时前端应展示的提示文案。 */
        String emptyBankCardText
) {
}
