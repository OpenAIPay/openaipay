package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.deliver.admin.DeliverAdminCommands;
import cn.openaipay.application.deliver.admin.DeliverAdminDTOs;
import cn.openaipay.application.deliver.admin.DeliverAdminFacade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 投放管理后台控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@RestController
@RequestMapping("/api/admin/deliver")
public class AdminDeliverController {

    /** 投放管理门面。 */
    private final DeliverAdminFacade deliverAdminFacade;

    /** 创建投放管理控制器并注入投放管理门面。 */
    public AdminDeliverController(DeliverAdminFacade deliverAdminFacade) {
        this.deliverAdminFacade = deliverAdminFacade;
    }

    /**
     * 查询控制台信息。
     */
    @GetMapping("/console")
    @RequireAdminPermission("deliver.view")
    public ApiResponse<DeliverAdminDTOs.Console> queryConsole() {
        return ApiResponse.success(deliverAdminFacade.queryConsole());
    }

    /**
     * 查询广告位信息列表。
     */
    @GetMapping("/positions")
    @RequireAdminPermission("deliver.view")
    public ApiResponse<List<DeliverAdminDTOs.PositionItem>> listPositions() {
        return ApiResponse.success(deliverAdminFacade.listPositions());
    }

    /**
     * 查询单元信息列表。
     */
    @GetMapping("/units")
    @RequireAdminPermission("deliver.view")
    public ApiResponse<List<DeliverAdminDTOs.UnitItem>> listUnits() {
        return ApiResponse.success(deliverAdminFacade.listUnits());
    }

    /**
     * 查询素材信息列表。
     */
    @GetMapping("/materials")
    @RequireAdminPermission("deliver.view")
    public ApiResponse<List<DeliverAdminDTOs.MaterialItem>> listMaterials() {
        return ApiResponse.success(deliverAdminFacade.listMaterials());
    }

    /**
     * 查询创意信息列表。
     */
    @GetMapping("/creatives")
    @RequireAdminPermission("deliver.view")
    public ApiResponse<List<DeliverAdminDTOs.CreativeItem>> listCreatives() {
        return ApiResponse.success(deliverAdminFacade.listCreatives());
    }

    /**
     * 查询关联关系信息列表。
     */
    @GetMapping("/relations")
    @RequireAdminPermission("deliver.view")
    public ApiResponse<List<DeliverAdminDTOs.RelationItem>> listRelations() {
        return ApiResponse.success(deliverAdminFacade.listRelations());
    }

    /**
     * 查询频控规则列表。
     */
    @GetMapping("/fatigue-rules")
    @RequireAdminPermission("deliver.view")
    public ApiResponse<List<DeliverAdminDTOs.FatigueRuleItem>> listFatigueRules() {
        return ApiResponse.success(deliverAdminFacade.listFatigueRules());
    }

    /**
     * 查询定向规则列表。
     */
    @GetMapping("/targeting-rules")
    @RequireAdminPermission("deliver.view")
    public ApiResponse<List<DeliverAdminDTOs.TargetingRuleItem>> listTargetingRules() {
        return ApiResponse.success(deliverAdminFacade.listTargetingRules());
    }

    /**
     * 创建广告位信息。
     */
    @PostMapping("/positions")
    @RequireAdminPermission("deliver.manage")
    public ApiResponse<DeliverAdminDTOs.PositionItem> createPosition(@Valid @RequestBody SavePositionRequest request) {
        return ApiResponse.success(deliverAdminFacade.savePosition(toPositionCommand(null, request)));
    }

    /**
     * 更新广告位信息。
     */
    @PutMapping("/positions/{positionId}")
    @RequireAdminPermission("deliver.manage")
    public ApiResponse<DeliverAdminDTOs.PositionItem> updatePosition(@PathVariable("positionId") Long positionId,
                                                                     @Valid @RequestBody SavePositionRequest request) {
        return ApiResponse.success(deliverAdminFacade.savePosition(toPositionCommand(positionId, request)));
    }

    /**
     * 创建单元信息。
     */
    @PostMapping("/units")
    @RequireAdminPermission("deliver.manage")
    public ApiResponse<DeliverAdminDTOs.UnitItem> createUnit(@Valid @RequestBody SaveUnitRequest request) {
        return ApiResponse.success(deliverAdminFacade.saveUnit(toUnitCommand(null, request)));
    }

    /**
     * 更新单元信息。
     */
    @PutMapping("/units/{unitId}")
    @RequireAdminPermission("deliver.manage")
    public ApiResponse<DeliverAdminDTOs.UnitItem> updateUnit(@PathVariable("unitId") Long unitId,
                                                             @Valid @RequestBody SaveUnitRequest request) {
        return ApiResponse.success(deliverAdminFacade.saveUnit(toUnitCommand(unitId, request)));
    }

    /**
     * 创建素材信息。
     */
    @PostMapping("/materials")
    @RequireAdminPermission("deliver.manage")
    public ApiResponse<DeliverAdminDTOs.MaterialItem> createMaterial(@Valid @RequestBody SaveMaterialRequest request) {
        return ApiResponse.success(deliverAdminFacade.saveMaterial(toMaterialCommand(null, request)));
    }

    /**
     * 更新素材信息。
     */
    @PutMapping("/materials/{materialId}")
    @RequireAdminPermission("deliver.manage")
    public ApiResponse<DeliverAdminDTOs.MaterialItem> updateMaterial(@PathVariable("materialId") Long materialId,
                                                                     @Valid @RequestBody SaveMaterialRequest request) {
        return ApiResponse.success(deliverAdminFacade.saveMaterial(toMaterialCommand(materialId, request)));
    }

    /**
     * 创建创意信息。
     */
    @PostMapping("/creatives")
    @RequireAdminPermission("deliver.manage")
    public ApiResponse<DeliverAdminDTOs.CreativeItem> createCreative(@Valid @RequestBody SaveCreativeRequest request) {
        return ApiResponse.success(deliverAdminFacade.saveCreative(toCreativeCommand(null, request)));
    }

    /**
     * 更新创意信息。
     */
    @PutMapping("/creatives/{creativeId}")
    @RequireAdminPermission("deliver.manage")
    public ApiResponse<DeliverAdminDTOs.CreativeItem> updateCreative(@PathVariable("creativeId") Long creativeId,
                                                                     @Valid @RequestBody SaveCreativeRequest request) {
        return ApiResponse.success(deliverAdminFacade.saveCreative(toCreativeCommand(creativeId, request)));
    }

    /**
     * 删除创意信息。
     */
    @DeleteMapping("/creatives/{creativeId}")
    @RequireAdminPermission("deliver.manage")
    public ApiResponse<Boolean> deleteCreative(@PathVariable("creativeId") Long creativeId) {
        deliverAdminFacade.deleteCreative(creativeId);
        return ApiResponse.success(Boolean.TRUE);
    }

    /**
     * 创建关联关系信息。
     */
    @PostMapping("/relations")
    @RequireAdminPermission("deliver.manage")
    public ApiResponse<DeliverAdminDTOs.RelationItem> createRelation(@Valid @RequestBody SaveRelationRequest request) {
        return ApiResponse.success(deliverAdminFacade.saveRelation(toRelationCommand(null, request)));
    }

    /**
     * 更新关联关系信息。
     */
    @PutMapping("/relations/{relationId}")
    @RequireAdminPermission("deliver.manage")
    public ApiResponse<DeliverAdminDTOs.RelationItem> updateRelation(@PathVariable("relationId") Long relationId,
                                                                     @Valid @RequestBody SaveRelationRequest request) {
        return ApiResponse.success(deliverAdminFacade.saveRelation(toRelationCommand(relationId, request)));
    }

    /**
     * 创建频控规则。
     */
    @PostMapping("/fatigue-rules")
    @RequireAdminPermission("deliver.manage")
    public ApiResponse<DeliverAdminDTOs.FatigueRuleItem> createFatigueRule(@Valid @RequestBody SaveFatigueRuleRequest request) {
        return ApiResponse.success(deliverAdminFacade.saveFatigueRule(toFatigueRuleCommand(null, request)));
    }

    /**
     * 更新频控规则。
     */
    @PutMapping("/fatigue-rules/{ruleId}")
    @RequireAdminPermission("deliver.manage")
    public ApiResponse<DeliverAdminDTOs.FatigueRuleItem> updateFatigueRule(@PathVariable("ruleId") Long ruleId,
                                                                           @Valid @RequestBody SaveFatigueRuleRequest request) {
        return ApiResponse.success(deliverAdminFacade.saveFatigueRule(toFatigueRuleCommand(ruleId, request)));
    }

    /**
     * 创建定向规则。
     */
    @PostMapping("/targeting-rules")
    @RequireAdminPermission("deliver.manage")
    public ApiResponse<DeliverAdminDTOs.TargetingRuleItem> createTargetingRule(@Valid @RequestBody SaveTargetingRuleRequest request) {
        return ApiResponse.success(deliverAdminFacade.saveTargetingRule(toTargetingRuleCommand(null, request)));
    }

    /**
     * 更新定向规则。
     */
    @PutMapping("/targeting-rules/{ruleId}")
    @RequireAdminPermission("deliver.manage")
    public ApiResponse<DeliverAdminDTOs.TargetingRuleItem> updateTargetingRule(@PathVariable("ruleId") Long ruleId,
                                                                               @Valid @RequestBody SaveTargetingRuleRequest request) {
        return ApiResponse.success(deliverAdminFacade.saveTargetingRule(toTargetingRuleCommand(ruleId, request)));
    }

    private DeliverAdminCommands.UpsertPositionCommand toPositionCommand(Long id, SavePositionRequest request) {
        return new DeliverAdminCommands.UpsertPositionCommand(
                id,
                request.positionCode(),
                request.positionName(),
                request.positionType(),
                request.previewImage(),
                request.slideInterval(),
                request.maxDisplayCount(),
                request.sortType(),
                request.sortRule(),
                request.needFallback(),
                request.status(),
                request.memo(),
                request.publishedAt(),
                request.activeFrom(),
                request.activeTo()
        );
    }

    private DeliverAdminCommands.UpsertUnitCommand toUnitCommand(Long id, SaveUnitRequest request) {
        return new DeliverAdminCommands.UpsertUnitCommand(
                id,
                request.unitCode(),
                request.unitName(),
                request.priority(),
                request.status(),
                request.memo(),
                request.activeFrom(),
                request.activeTo()
        );
    }

    private DeliverAdminCommands.UpsertMaterialCommand toMaterialCommand(Long id, SaveMaterialRequest request) {
        return new DeliverAdminCommands.UpsertMaterialCommand(
                id,
                request.materialCode(),
                request.materialName(),
                request.materialType(),
                request.title(),
                request.imageUrl(),
                request.landingUrl(),
                request.schemaJson(),
                request.previewImage(),
                request.status(),
                request.activeFrom(),
                request.activeTo()
        );
    }

    private DeliverAdminCommands.UpsertCreativeCommand toCreativeCommand(Long id, SaveCreativeRequest request) {
        return new DeliverAdminCommands.UpsertCreativeCommand(
                id,
                request.creativeCode(),
                request.creativeName(),
                request.unitCode(),
                request.materialCode(),
                request.landingUrl(),
                request.schemaJson(),
                request.priority(),
                request.weight(),
                request.fallback(),
                request.previewImage(),
                request.status(),
                request.activeFrom(),
                request.activeTo()
        );
    }

    private DeliverAdminCommands.UpsertRelationCommand toRelationCommand(Long id, SaveRelationRequest request) {
        return new DeliverAdminCommands.UpsertRelationCommand(
                id,
                request.positionId(),
                request.unitId(),
                request.creativeId(),
                request.displayOrder(),
                request.fallback(),
                request.enabled()
        );
    }

    private DeliverAdminCommands.UpsertFatigueRuleCommand toFatigueRuleCommand(Long id, SaveFatigueRuleRequest request) {
        return new DeliverAdminCommands.UpsertFatigueRuleCommand(
                id,
                request.fatigueCode(),
                request.ruleName(),
                request.entityType(),
                request.entityCode(),
                request.eventType(),
                request.timeWindowMinutes(),
                request.maxCount(),
                request.enabled()
        );
    }

    private DeliverAdminCommands.UpsertTargetingRuleCommand toTargetingRuleCommand(Long id, SaveTargetingRuleRequest request) {
        return new DeliverAdminCommands.UpsertTargetingRuleCommand(
                id,
                request.ruleCode(),
                request.entityType(),
                request.entityCode(),
                request.targetingType(),
                request.operator(),
                request.targetingValue(),
                request.enabled()
        );
    }
}

record SavePositionRequest(
        /** 位置编码 */
        @NotBlank String positionCode,
        /** 位置名称 */
        @NotBlank String positionName,
        /** 位置类型 */
        @NotBlank String positionType,
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
        Boolean needFallback,
        /** 状态编码 */
        String status,
        /** 备注 */
        String memo,
        /** 发布时间 */
        LocalDateTime publishedAt,
        /** 生效开始时间 */
        LocalDateTime activeFrom,
        /** 生效结束时间 */
        LocalDateTime activeTo
) {
}

record SaveUnitRequest(
        /** 单元编码 */
        @NotBlank String unitCode,
        /** 单元名称 */
        @NotBlank String unitName,
        /** 优先级信息 */
        Integer priority,
        /** 状态编码 */
        String status,
        /** 备注 */
        String memo,
        /** 生效开始时间 */
        LocalDateTime activeFrom,
        /** 生效结束时间 */
        LocalDateTime activeTo
) {
}

record SaveMaterialRequest(
        /** material编码 */
        @NotBlank String materialCode,
        /** material名称 */
        @NotBlank String materialName,
        /** material类型 */
        @NotBlank String materialType,
        /** 标题 */
        String title,
        /** 图片地址 */
        @NotBlank String imageUrl,
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
        LocalDateTime activeTo
) {
}

record SaveCreativeRequest(
        /** creative编码 */
        @NotBlank String creativeCode,
        /** creative名称 */
        @NotBlank String creativeName,
        /** 单元编码 */
        @NotBlank String unitCode,
        /** material编码 */
        @NotBlank String materialCode,
        /** landing地址 */
        String landingUrl,
        /** schemaJSON */
        String schemaJson,
        /** 优先级信息 */
        Integer priority,
        /** 权重信息 */
        Integer weight,
        /** 兜底标记 */
        Boolean fallback,
        /** 预览图地址 */
        String previewImage,
        /** 状态编码 */
        String status,
        /** 生效开始时间 */
        LocalDateTime activeFrom,
        /** 生效结束时间 */
        LocalDateTime activeTo
) {
}

record SaveRelationRequest(
        /** 位置ID */
        @NotNull Long positionId,
        /** 单元ID */
        @NotNull Long unitId,
        /** creativeID */
        @NotNull Long creativeId,
        /** 展示订单信息 */
        Integer displayOrder,
        /** 兜底标记 */
        Boolean fallback,
        /** 启用标记 */
        Boolean enabled
) {
}

record SaveFatigueRuleRequest(
        /** fatigue编码 */
        @NotBlank String fatigueCode,
        /** rule名称 */
        @NotBlank String ruleName,
        /** entity类型 */
        @NotBlank String entityType,
        /** entity编码 */
        @NotBlank String entityCode,
        /** 事件类型 */
        String eventType,
        /** 时间windowminutes信息 */
        Integer timeWindowMinutes,
        /** 最大数量 */
        Integer maxCount,
        /** 启用标记 */
        Boolean enabled
) {
}

record SaveTargetingRuleRequest(
        /** rule编码 */
        @NotBlank String ruleCode,
        /** entity类型 */
        @NotBlank String entityType,
        /** entity编码 */
        @NotBlank String entityCode,
        /** targeting类型 */
        @NotBlank String targetingType,
        /** 操作人 */
        String operator,
        /** targeting值信息 */
        @NotBlank String targetingValue,
        /** 启用标记 */
        Boolean enabled
) {
}
