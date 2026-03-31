package cn.openaipay.application.asset.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资产总览数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record AssetOverviewDTO(
        /** 用户ID */
        String userId,
        /** 币种编码 */
        String currencyCode,
        /** 业务金额 */
        BigDecimal availableAmount,
        /** 业务金额 */
        BigDecimal reservedAmount,
        /** 总金额 */
        BigDecimal totalAmount,
        /** 业务状态 */
        String accountStatus,
        /** 业务时间 */
        LocalDateTime generatedAt
) {
}
