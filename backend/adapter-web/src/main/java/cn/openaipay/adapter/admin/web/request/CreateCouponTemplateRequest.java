package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.joda.money.Money;
/**
 * 创建优惠券模板请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record CreateCouponTemplateRequest(
        /** 模板编码 */
        @NotBlank(message = "不能为空") String templateCode,
        /** 模板名称 */
        @NotBlank(message = "不能为空") String templateName,
        /** 场景类型 */
        @NotBlank(message = "不能为空") String sceneType,
        /** 值类型 */
        @NotBlank(message = "不能为空") String valueType,
        /** 金额 */
        Money amount,
        /** MIN金额 */
        Money minAmount,
        /** 最大金额 */
        Money maxAmount,
        /** 业务金额 */
        Money thresholdAmount,
        /** 总信息 */
        @NotNull(message = "不能为空") Money totalBudget,
        /** 总信息 */
        @NotNull(message = "不能为空")
                                        @Min(value = 1, message = "必须大于0") Integer totalStock,
        /** PER用户限额信息 */
        @Min(value = 1, message = "必须大于0") Integer perUserLimit,
        /** 业务时间 */
        @NotBlank(message = "不能为空") String claimStartTime,
        /** END时间 */
        @NotBlank(message = "不能为空") String claimEndTime,
        /** USE时间 */
        @NotBlank(message = "不能为空") String useStartTime,
        /** USEEND时间 */
        @NotBlank(message = "不能为空") String useEndTime,
        /** 来源信息 */
        @NotBlank(message = "不能为空") String fundingSource,
        /** 规则载荷 */
        String rulePayload,
        /** 业务状态 */
        String initialStatus,
        /** 操作人 */
        String operator
) {
}
