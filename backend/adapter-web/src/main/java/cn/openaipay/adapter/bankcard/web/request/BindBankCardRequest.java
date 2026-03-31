package cn.openaipay.adapter.bankcard.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.joda.money.Money;

/**
 * 绑定银行卡请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record BindBankCardRequest(
        /** 用户ID */
        @NotNull(message = "userId不能为空") Long userId,
        /** 银行卡号 */
        @NotBlank(message = "cardNo不能为空") String cardNo,
        /** 发卡行编码 */
        @NotBlank(message = "bankCode不能为空") String bankCode,
        /** 发卡行名称 */
        @NotBlank(message = "bankName不能为空") String bankName,
        /** 银行卡类型 */
        @NotBlank(message = "cardType不能为空") String cardType,
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
