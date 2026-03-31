package cn.openaipay.domain.pay.client;

import org.joda.money.Money;

/**
 * PayBankCardSnapshot 记录模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record PayBankCardSnapshot(
        /** 卡单号 */
        String cardNo,
        /** 卡单号 */
        String maskedCardNo,
        /** 用户ID */
        Long userId,
        /** 银行编码 */
        String bankCode,
        /** 银行名称 */
        String bankName,
        /** 卡类型 */
        String cardType,
        /** 卡名称 */
        String cardHolderName,
        /** 业务手机号 */
        String reservedMobile,
        /** 手机号单号 */
        String phoneTailNo,
        /** 卡状态 */
        String cardStatus,
        /** 卡信息 */
        boolean defaultCard,
        /** 单笔限额 */
        Money singleLimit,
        /** 单日限额 */
        Money dailyLimit
) {
}
