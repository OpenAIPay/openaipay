package cn.openaipay.application.agreement.dto;

import java.time.LocalDateTime;

/**
 * 签约并开通信用产品结果DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record OpenCreditProductWithAgreementResultDTO(
        /** 用户ID */
        Long userId,
        /** 产品编码 */
        String productCode,
        /** 业务单号 */
        String accountNo,
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
