package cn.openaipay.application.agreement.dto;

import java.time.LocalDateTime;

/**
 * 签约并开通基金账户结果DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record OpenFundAccountWithAgreementResultDTO(
        /** 用户ID */
        Long userId,
        /** 资金编码 */
        String fundCode,
        /** 币种编码 */
        String currencyCode,
        /** 签约单号 */
        String signNo,
        /** 签约状态 */
        String signStatus,
        /** 业务时间 */
        LocalDateTime signedAt,
        /** 业务时间 */
        LocalDateTime openedAt,
        /** 功能开通标记 */
        boolean featureEnabled
) {
}
