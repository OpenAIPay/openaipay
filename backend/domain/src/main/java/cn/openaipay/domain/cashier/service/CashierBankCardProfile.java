package cn.openaipay.domain.cashier.service;

import org.joda.money.Money;

/**
 * 收银台可选银行卡画像。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record CashierBankCardProfile(
        /** 卡单号 */
        String cardNo,
        /** 银行编码 */
        String bankCode,
        /** 银行名称 */
        String bankName,
        /** 卡类型 */
        String cardType,
        /** 业务手机号 */
        String reservedMobile,
        /** 手机号单号 */
        String phoneTailNo,
        /** 卡信息 */
        boolean defaultCard,
        /** 单笔限额 */
        Money singleLimit,
        /** 单日限额 */
        Money dailyLimit
) {
}
