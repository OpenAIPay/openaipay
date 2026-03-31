package cn.openaipay.application.coupon.dto;

import org.joda.money.Money;
import java.time.LocalDateTime;
/**
 * 优惠券模板数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record CouponTemplateDTO(
        /** 模板ID */
        Long templateId,
        /** 模板编码 */
        String templateCode,
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
        /** 业务次数 */
        Integer claimedCount,
        /** PER用户限额信息 */
        Integer perUserLimit,
        /** 业务时间 */
        LocalDateTime claimStartTime,
        /** END时间 */
        LocalDateTime claimEndTime,
        /** USE时间 */
        LocalDateTime useStartTime,
        /** USEEND时间 */
        LocalDateTime useEndTime,
        /** 来源信息 */
        String fundingSource,
        /** 规则载荷 */
        String rulePayload,
        /** 状态编码 */
        String status,
        /** 创建BY信息 */
        String createdBy,
        /** 更新BY信息 */
        String updatedBy,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
