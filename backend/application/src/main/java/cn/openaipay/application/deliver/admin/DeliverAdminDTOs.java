package cn.openaipay.application.deliver.admin;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 投放管理后台DTO集合。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public final class DeliverAdminDTOs {

    private DeliverAdminDTOs() {
    }

    public record Overview(
            /** 位置数量 */
            int positionCount,
            /** 单元数量 */
            int unitCount,
            /** material数量 */
            int materialCount,
            /** creative数量 */
            int creativeCount,
            /** relation数量 */
            int relationCount,
            /** fatiguerule数量 */
            int fatigueRuleCount,
            /** targetingrule数量 */
            int targetingRuleCount
    ) {
    }

    public record Console(
            /** overview信息 */
            Overview overview,
            /** positions信息 */
            List<PositionItem> positions,
            /** units信息 */
            List<UnitItem> units,
            /** materials信息 */
            List<MaterialItem> materials,
            /** creatives信息 */
            List<CreativeItem> creatives,
            /** relations信息 */
            List<RelationItem> relations,
            /** fatiguerules信息 */
            List<FatigueRuleItem> fatigueRules,
            /** targetingrules信息 */
            List<TargetingRuleItem> targetingRules
    ) {
    }

    public record PositionItem(
            /** 数据库主键ID */
            Long id,
            /** 位置编码 */
            String positionCode,
            /** 位置名称 */
            String positionName,
            /** 位置类型 */
            String positionType,
            /** 预览图地址 */
            String previewImage,
            /** 轮播间隔秒数 */
            Integer slideInterval,
            /** 最大展示数量 */
            Integer maxDisplayCount,
            /** sort类型 */
            String sortType,
            /** sortrule信息 */
            String sortRule,
            /** need兜底信息 */
            boolean needFallback,
            /** 状态编码 */
            String status,
            /** 备注 */
            String memo,
            /** 发布时间 */
            LocalDateTime publishedAt,
            /** 生效开始时间 */
            LocalDateTime activeFrom,
            /** 生效结束时间 */
            LocalDateTime activeTo,
            /** 记录创建时间 */
            LocalDateTime createdAt,
            /** 记录更新时间 */
            LocalDateTime updatedAt
    ) {
    }

    public record UnitItem(
            /** 数据库主键ID */
            Long id,
            /** 单元编码 */
            String unitCode,
            /** 单元名称 */
            String unitName,
            /** 优先级信息 */
            Integer priority,
            /** 状态编码 */
            String status,
            /** 备注 */
            String memo,
            /** 生效开始时间 */
            LocalDateTime activeFrom,
            /** 生效结束时间 */
            LocalDateTime activeTo,
            /** 记录创建时间 */
            LocalDateTime createdAt,
            /** 记录更新时间 */
            LocalDateTime updatedAt
    ) {
    }

    public record MaterialItem(
            /** 数据库主键ID */
            Long id,
            /** material编码 */
            String materialCode,
            /** material名称 */
            String materialName,
            /** material类型 */
            String materialType,
            /** 标题 */
            String title,
            /** 图片地址 */
            String imageUrl,
            /** landing地址 */
            String landingUrl,
            /** schemaJSON */
            String schemaJson,
            /** 预览图地址 */
            String previewImage,
            /** 状态编码 */
            String status,
            /** 生效开始时间 */
            LocalDateTime activeFrom,
            /** 生效结束时间 */
            LocalDateTime activeTo,
            /** 记录创建时间 */
            LocalDateTime createdAt,
            /** 记录更新时间 */
            LocalDateTime updatedAt
    ) {
    }

    public record CreativeItem(
            /** 数据库主键ID */
            Long id,
            /** creative编码 */
            String creativeCode,
            /** creative名称 */
            String creativeName,
            /** 单元编码 */
            String unitCode,
            /** material编码 */
            String materialCode,
            /** landing地址 */
            String landingUrl,
            /** schemaJSON */
            String schemaJson,
            /** 优先级信息 */
            Integer priority,
            /** 权重信息 */
            Integer weight,
            /** 兜底标记 */
            boolean fallback,
            /** 预览图地址 */
            String previewImage,
            /** 状态编码 */
            String status,
            /** 生效开始时间 */
            LocalDateTime activeFrom,
            /** 生效结束时间 */
            LocalDateTime activeTo,
            /** 记录创建时间 */
            LocalDateTime createdAt,
            /** 记录更新时间 */
            LocalDateTime updatedAt
    ) {
    }

    public record RelationItem(
            /** 数据库主键ID */
            Long id,
            /** 位置ID */
            Long positionId,
            /** 单元ID */
            Long unitId,
            /** creativeID */
            Long creativeId,
            /** 展示订单信息 */
            Integer displayOrder,
            /** 兜底标记 */
            boolean fallback,
            /** 启用标记 */
            boolean enabled,
            /** 记录创建时间 */
            LocalDateTime createdAt,
            /** 记录更新时间 */
            LocalDateTime updatedAt
    ) {
    }

    public record FatigueRuleItem(
            /** 数据库主键ID */
            Long id,
            /** fatigue编码 */
            String fatigueCode,
            /** rule名称 */
            String ruleName,
            /** entity类型 */
            String entityType,
            /** entity编码 */
            String entityCode,
            /** 事件类型 */
            String eventType,
            /** 时间windowminutes信息 */
            Integer timeWindowMinutes,
            /** 最大数量 */
            Integer maxCount,
            /** 启用标记 */
            boolean enabled,
            /** 记录创建时间 */
            LocalDateTime createdAt,
            /** 记录更新时间 */
            LocalDateTime updatedAt
    ) {
    }

    public record TargetingRuleItem(
            /** 数据库主键ID */
            Long id,
            /** rule编码 */
            String ruleCode,
            /** entity类型 */
            String entityType,
            /** entity编码 */
            String entityCode,
            /** targeting类型 */
            String targetingType,
            /** 操作人 */
            String operator,
            /** targeting值信息 */
            String targetingValue,
            /** 启用标记 */
            boolean enabled,
            /** 记录创建时间 */
            LocalDateTime createdAt,
            /** 记录更新时间 */
            LocalDateTime updatedAt
    ) {
    }
}
