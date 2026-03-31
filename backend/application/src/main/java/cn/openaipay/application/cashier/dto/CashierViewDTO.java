package cn.openaipay.application.cashier.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收银台页面数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record CashierViewDTO(
        /** 用户ID */
        Long userId,
        /** 当前返回的收银台场景编码。 */
        String sceneCode,
        /** 当前场景的渠道与银行卡准入配置。 */
        CashierSceneConfigurationDTO sceneConfig,
        /** 当前场景可用的支付工具集合。 */
        List<CashierPayToolDTO> payTools,
        /** 收银台数据生成时间。 */
        LocalDateTime generatedAt
) {
}
