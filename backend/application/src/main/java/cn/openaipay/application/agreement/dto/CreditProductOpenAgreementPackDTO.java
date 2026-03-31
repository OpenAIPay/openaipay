package cn.openaipay.application.agreement.dto;

import java.util.List;

/**
 * 信用产品开通协议包DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record CreditProductOpenAgreementPackDTO(
        /** 用户ID */
        Long userId,
        /** 业务类型 */
        String bizType,
        /** 产品编码 */
        String productCode,
        /** 协议列表 */
        List<AgreementTemplateDTO> agreements
) {
}
