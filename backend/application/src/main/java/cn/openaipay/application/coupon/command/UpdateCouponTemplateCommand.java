package cn.openaipay.application.coupon.command;

import org.joda.money.Money;
/**
 * 更新优惠券模板命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record UpdateCouponTemplateCommand(
        /** 模板ID */
        Long templateId,
        /** 模板名称 */
        String templateName,
        /** 场景类型 */
        String sceneType,
        /** 值类型 */
        String valueType,
        /** 金额 */
        Money amount,
        /** MIN金额 */
        Money minAmount,
        /** 最大金额 */
        Money maxAmount,
        /** 业务金额 */
        Money thresholdAmount,
        /** 总信息 */
        Money totalBudget,
        /** 总信息 */
        Integer totalStock,
        /** PER用户限额信息 */
        Integer perUserLimit,
        /** 业务时间 */
        String claimStartTime,
        /** END时间 */
        String claimEndTime,
        /** USE时间 */
        String useStartTime,
        /** USEEND时间 */
        String useEndTime,
        /** 来源信息 */
        String fundingSource,
        /** 规则载荷 */
        String rulePayload,
        /** 操作人 */
        String operator
) {
}
