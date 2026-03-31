package cn.openaipay.adapter.creditaccount.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.joda.money.Money;
/**
 * 创建信用账户请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record CreateCreditAccountRequest(
        /** 用户ID */
        @NotNull Long userId,
        /** 业务单号 */
        @NotBlank String accountNo,
        /** 总信息 */
        Money totalLimit,
        /** repayDAYOFmonth信息 */
        Integer repayDayOfMonth
) {
}
