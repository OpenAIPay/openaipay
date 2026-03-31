package cn.openaipay.application.cashier.dto;

import java.math.BigDecimal;
import java.util.List;
import org.joda.money.Money;

/**
 * 收银台计费试算结果
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record CashierPricingPreviewDTO(
        /** 用户ID */
        Long userId,
        /** 场景编码 */
        String sceneCode,
        /** 计费场景编码 */
        String pricingSceneCode,
        /** 支付方式编码 */
        String paymentMethod,
        /** 业务单号 */
        String quoteNo,
        /** 规则编码 */
        String ruleCode,
        /** 规则名称 */
        String ruleName,
        /** 原始金额 */
        Money originalAmount,
        /** 手续费金额 */
        Money feeAmount,
        /** 应付金额 */
        Money payableAmount,
        /** 结算金额 */
        Money settleAmount,
        /** 手续费费率 */
        BigDecimal feeRate,
        /** 手续费信息 */
        String feeBearer,
        /** 可用红包数 */
        Integer availableCouponCount,
        /** 推荐红包券号 */
        String recommendedCouponNo,
        /** 推荐红包金额 */
        Money recommendedCouponAmount,
        /** 红包抵扣金额 */
        Money couponDeductAmount,
        /** 抵扣后应付金额 */
        Money payableAfterCoupon,
        /** 可用红包列表 */
        List<CashierCouponCandidateDTO> availableCoupons
) {
}
