package cn.openaipay.application.adminfund.dto;

import java.time.LocalDateTime;
import org.joda.money.Money;

/**
 * 银行卡行
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminBankCardRowDTO(
        /** 用户ID */
        Long userId,
        /** 用户展示名称 */
        String userDisplayName,
        /** 爱付UID */
        String aipayUid,
        /** 卡号 */
        String cardNo,
        /** 银行编码 */
        String bankCode,
        /** 银行名称 */
        String bankName,
        /** 卡类型 */
        String cardType,
        /** 持卡人姓名 */
        String cardHolderName,
        /** 预留手机号 */
        String reservedMobile,
        /** 手机尾号 */
        String phoneTailNo,
        /** 卡状态 */
        String cardStatus,
        /** 是否默认卡 */
        Boolean isDefault,
        /** 单笔限额 */
        Money singleLimit,
        /** 单日限额 */
        Money dailyLimit,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
