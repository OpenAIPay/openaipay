package cn.openaipay.application.adminfund.dto;

import java.time.LocalDateTime;
import org.joda.money.Money;

/**
 * 钱包账户行
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminWalletAccountRowDTO(
        /** 用户ID */
        Long userId,
        /** 用户展示名称 */
        String userDisplayName,
        /** 爱付UID */
        String aipayUid,
        /** 币种编码 */
        String currencyCode,
        /** 可用余额 */
        Money availableBalance,
        /** 冻结余额 */
        Money reservedBalance,
        /** 账号状态 */
        String accountStatus,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
