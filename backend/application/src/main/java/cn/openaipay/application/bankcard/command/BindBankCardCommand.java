package cn.openaipay.application.bankcard.command;

import org.joda.money.Money;

/**
 * 绑定银行卡命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record BindBankCardCommand(
        /** 用户ID */
        Long userId,
        /** 银行卡号 */
        String cardNo,
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
        /** 是否设置为默认卡 */
        Boolean defaultCard,
        /** 单笔限额 */
        Money singleLimit,
        /** 单日限额 */
        Money dailyLimit
) {
}
