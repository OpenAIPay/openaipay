package cn.openaipay.application.cashier.dto;

import org.joda.money.Money;

/**
 * 收银台支付工具数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record CashierPayToolDTO(
        /** 支付工具类型 */
        String toolType,
        /** 支付工具编码 */
        String toolCode,
        /** 支付工具名称 */
        String toolName,
        /** 支付工具说明 */
        String toolDescription,
        /** 默认选中标记 */
        boolean defaultSelected,
        /** 单笔限额 */
        Money singleLimit,
        /** 单日限额 */
        Money dailyLimit,
        /** 发卡行编码 */
        String bankCode,
        /** 银行卡类型 */
        String cardType,
        /** 手机尾号 */
        String phoneTailNo
) {
}
