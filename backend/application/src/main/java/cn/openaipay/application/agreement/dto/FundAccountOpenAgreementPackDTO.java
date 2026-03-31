package cn.openaipay.application.agreement.dto;

import java.util.List;

/**
 * 基金账户开通协议包DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record FundAccountOpenAgreementPackDTO(
        /** 用户ID */
        Long userId,
        /** 业务类型 */
        String bizType,
        /** 资金编码 */
        String fundCode,
        /** 币种编码 */
        String currencyCode,
        /** 协议列表 */
        List<AgreementTemplateDTO> agreements
) {
}
