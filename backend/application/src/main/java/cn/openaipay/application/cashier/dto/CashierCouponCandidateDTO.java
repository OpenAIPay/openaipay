package cn.openaipay.application.cashier.dto;

import java.time.LocalDateTime;
import org.joda.money.Money;

/**
 * 收银台可用红包候选
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
public record CashierCouponCandidateDTO(
        /** 红包券号 */
        String couponNo,
        /** 红包金额 */
        Money couponAmount,
        /** 过期时间 */
        LocalDateTime expireAt
) {
}
