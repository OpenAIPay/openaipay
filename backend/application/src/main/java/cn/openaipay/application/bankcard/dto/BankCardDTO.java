package cn.openaipay.application.bankcard.dto;

import org.joda.money.Money;

/**
 * 银行卡数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record BankCardDTO(
        /** 银行卡号 */
        String cardNo,
        /** 脱敏银行卡号 */
        String maskedCardNo,
        /** 用户ID */
        Long userId,
        /** 发卡行编码 */
        String bankCode,
        /** 发卡行名称 */
        String bankName,
        /** 银行卡类型 */
        String cardType,
        /** 持卡人姓名 */
        String cardHolderName,
        /** 银行预留手机号 */
        String reservedMobile,
        /** 预留手机号后四位 */
        String phoneTailNo,
        /** 银行卡状态 */
        String cardStatus,
        /** 是否默认卡 */
        boolean defaultCard,
        /** 单笔限额 */
        Money singleLimit,
        /** 单日限额 */
        Money dailyLimit
) {
}
