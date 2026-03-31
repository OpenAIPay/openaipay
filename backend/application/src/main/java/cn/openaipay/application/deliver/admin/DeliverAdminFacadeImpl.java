package cn.openaipay.application.deliver.admin;

import cn.openaipay.domain.deliver.model.DeliverCreative;
import cn.openaipay.domain.deliver.model.DeliverEntityType;
import cn.openaipay.domain.deliver.model.DeliverEventType;
import cn.openaipay.domain.deliver.model.DeliverMaterial;
import cn.openaipay.domain.deliver.model.DeliverPositionType;
import cn.openaipay.domain.deliver.model.DeliverPublishStatus;
import cn.openaipay.domain.deliver.model.DeliverSortType;
import cn.openaipay.domain.deliver.model.DeliverTargetingOperator;
import cn.openaipay.domain.deliver.model.DeliverTargetingType;
import cn.openaipay.domain.deliver.model.DeliverUnit;
import cn.openaipay.domain.deliver.model.FatigueControlRule;
import cn.openaipay.domain.deliver.model.Position;
import cn.openaipay.domain.deliver.model.PositionUnitCreativeRelation;
import cn.openaipay.domain.deliver.model.TargetingRule;
import cn.openaipay.domain.deliver.repository.DeliverCreativeRepository;
import cn.openaipay.domain.deliver.repository.DeliverMaterialRepository;
import cn.openaipay.domain.deliver.repository.DeliverUnitRepository;
import cn.openaipay.domain.deliver.repository.FatigueControlRuleRepository;
import cn.openaipay.domain.deliver.repository.PositionRepository;
import cn.openaipay.domain.deliver.repository.PositionUnitCreativeRelationRepository;
import cn.openaipay.domain.deliver.repository.TargetingRuleRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 投放管理后台门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Service
public class DeliverAdminFacadeImpl implements DeliverAdminFacade {

    /** 位置信息 */
    private final PositionRepository positionRepository;
    /** 投放单元信息 */
    private final DeliverUnitRepository deliverUnitRepository;
    /** 投放信息 */
    private final DeliverMaterialRepository deliverMaterialRepository;
    /** 投放信息 */
    private final DeliverCreativeRepository deliverCreativeRepository;
    /** relation仓储信息 */
    private final PositionUnitCreativeRelationRepository relationRepository;
    /** 规则信息 */
    private final FatigueControlRuleRepository fatigueControlRuleRepository;
    /** 规则信息 */
    private final TargetingRuleRepository targetingRuleRepository;

    public DeliverAdminFacadeImpl(PositionRepository positionRepository,
                                  DeliverUnitRepository deliverUnitRepository,
                                  DeliverMaterialRepository deliverMaterialRepository,
                                  DeliverCreativeRepository deliverCreativeRepository,
                                  PositionUnitCreativeRelationRepository relationRepository,
                                  FatigueControlRuleRepository fatigueControlRuleRepository,
                                  TargetingRuleRepository targetingRuleRepository) {
        this.positionRepository = positionRepository;
        this.deliverUnitRepository = deliverUnitRepository;
        this.deliverMaterialRepository = deliverMaterialRepository;
        this.deliverCreativeRepository = deliverCreativeRepository;
        this.relationRepository = relationRepository;
        this.fatigueControlRuleRepository = fatigueControlRuleRepository;
        this.targetingRuleRepository = targetingRuleRepository;
    }

    /**
     * 查询广告位信息列表。
     */
    @Override
    public List<DeliverAdminDTOs.PositionItem> listPositions() {
        return queryConsole().positions();
    }

    /**
     * 查询单元信息列表。
     */
    @Override
    public List<DeliverAdminDTOs.UnitItem> listUnits() {
        return queryConsole().units();
    }

    /**
     * 查询素材信息列表。
     */
    @Override
    public List<DeliverAdminDTOs.MaterialItem> listMaterials() {
        return queryConsole().materials();
    }

    /**
     * 查询创意信息列表。
     */
    @Override
    public List<DeliverAdminDTOs.CreativeItem> listCreatives() {
        return queryConsole().creatives();
    }

    /**
     * 查询关联关系信息列表。
     */
    @Override
    public List<DeliverAdminDTOs.RelationItem> listRelations() {
        return queryConsole().relations();
    }

    /**
     * 查询频控规则列表。
     */
    @Override
    public List<DeliverAdminDTOs.FatigueRuleItem> listFatigueRules() {
        return queryConsole().fatigueRules();
    }

    /**
     * 查询定向规则列表。
     */
    @Override
    public List<DeliverAdminDTOs.TargetingRuleItem> listTargetingRules() {
        return queryConsole().targetingRules();
    }

    /**
     * 查询控制台信息。
     */
    @Override
    public DeliverAdminDTOs.Console queryConsole() {
        List<DeliverAdminDTOs.PositionItem> positions = positionRepository.findAllPositions().stream()
                .sorted(Comparator.comparing(Position::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toPositionItem)
                .toList();
        List<DeliverAdminDTOs.UnitItem> units = deliverUnitRepository.findAllUnits().stream()
                .sorted(Comparator.comparing(DeliverUnit::id, Comparator.nullsLast(Long::compareTo)))
                .map(this::toUnitItem)
                .toList();
        List<DeliverAdminDTOs.MaterialItem> materials = deliverMaterialRepository.findAllMaterials().stream()
                .sorted(Comparator.comparing(DeliverMaterial::id, Comparator.nullsLast(Long::compareTo)))
                .map(this::toMaterialItem)
                .toList();
        List<DeliverAdminDTOs.CreativeItem> creatives = deliverCreativeRepository.findAllCreatives().stream()
                .sorted(Comparator.comparing(DeliverCreative::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toCreativeItem)
                .toList();
        List<DeliverAdminDTOs.RelationItem> relations = relationRepository.findAllRelations().stream()
                .sorted(Comparator.comparing(PositionUnitCreativeRelation::id, Comparator.nullsLast(Long::compareTo)))
                .map(this::toRelationItem)
                .toList();
        List<DeliverAdminDTOs.FatigueRuleItem> fatigueRules = fatigueControlRuleRepository.findAllFatigueRules().stream()
                .sorted(Comparator.comparing(FatigueControlRule::id, Comparator.nullsLast(Long::compareTo)))
                .map(this::toFatigueRuleItem)
                .toList();
        List<DeliverAdminDTOs.TargetingRuleItem> targetingRules = targetingRuleRepository.findAllTargetingRules().stream()
                .sorted(Comparator.comparing(TargetingRule::id, Comparator.nullsLast(Long::compareTo)))
                .map(this::toTargetingRuleItem)
                .toList();

        return new DeliverAdminDTOs.Console(
                new DeliverAdminDTOs.Overview(
                        positions.size(),
                        units.size(),
                        materials.size(),
                        creatives.size(),
                        relations.size(),
                        fatigueRules.size(),
                        targetingRules.size()
                ),
                positions,
                units,
                materials,
                creatives,
                relations,
                fatigueRules,
                targetingRules
        );
    }

    /**
     * 保存广告位信息。
     */
    @Override
    @Transactional
    public DeliverAdminDTOs.PositionItem savePosition(DeliverAdminCommands.UpsertPositionCommand command) {
        Position existing = command.id() == null ? null : positionRepository.findPositionById(command.id()).orElseThrow(() -> new IllegalArgumentException("position not found"));
        DeliverPublishStatus status = parseEnum(command.status(), DeliverPublishStatus.class, existing == null ? DeliverPublishStatus.EDITING : existing.getStatus());
        Position saved = positionRepository.savePosition(new Position(
                command.id(),
                required(command.positionCode(), "positionCode"),
                required(command.positionName(), "positionName"),
                parseEnum(command.positionType(), DeliverPositionType.class, DeliverPositionType.BANNER),
                trimToNull(command.previewImage()),
                defaultInt(command.slideInterval(), 5),
                defaultInt(command.maxDisplayCount(), 1),
                parseEnum(command.sortType(), DeliverSortType.class, DeliverSortType.MANUAL),
                trimToNull(command.sortRule()),
                Boolean.TRUE.equals(command.needFallback()),
                status,
                trimToNull(command.memo()),
                resolvePublishedAt(existing == null ? null : existing.getPublishedAt(), command.publishedAt(), status),
                command.activeFrom(),
                command.activeTo(),
                existing == null ? null : existing.getCreatedAt(),
                null,
                List.of()
        ));
        return toPositionItem(saved);
    }

    /**
     * 保存单元信息。
     */
    @Override
    @Transactional
    public DeliverAdminDTOs.UnitItem saveUnit(DeliverAdminCommands.UpsertUnitCommand command) {
        DeliverUnit existing = command.id() == null ? null : deliverUnitRepository.findUnitById(command.id()).orElseThrow(() -> new IllegalArgumentException("unit not found"));
        DeliverUnit saved = deliverUnitRepository.saveUnit(new DeliverUnit(
                command.id(),
                required(command.unitCode(), "unitCode"),
                required(command.unitName(), "unitName"),
                defaultInt(command.priority(), 100),
                parseEnum(command.status(), DeliverPublishStatus.class, existing == null ? DeliverPublishStatus.EDITING : existing.status()),
                command.activeFrom(),
                command.activeTo(),
                trimToNull(command.memo()),
                existing == null ? null : existing.createdAt(),
                null
        ));
        return toUnitItem(saved);
    }

    /**
     * 保存素材信息。
     */
    @Override
    @Transactional
    public DeliverAdminDTOs.MaterialItem saveMaterial(DeliverAdminCommands.UpsertMaterialCommand command) {
        DeliverMaterial existing = command.id() == null ? null : deliverMaterialRepository.findMaterialById(command.id()).orElseThrow(() -> new IllegalArgumentException("material not found"));
        DeliverMaterial saved = deliverMaterialRepository.saveMaterial(new DeliverMaterial(
                command.id(),
                required(command.materialCode(), "materialCode"),
                required(command.materialName(), "materialName"),
                required(command.materialType(), "materialType"),
                trimToNull(command.title()),
                required(command.imageUrl(), "imageUrl"),
                trimToNull(command.landingUrl()),
                trimToNull(command.schemaJson()),
                trimToNull(command.previewImage()),
                parseEnum(command.status(), DeliverPublishStatus.class, existing == null ? DeliverPublishStatus.EDITING : existing.status()),
                command.activeFrom(),
                command.activeTo(),
                existing == null ? null : existing.createdAt(),
                null
        ));
        return toMaterialItem(saved);
    }

    /**
     * 保存创意信息。
     */
    @Override
    @Transactional
    public DeliverAdminDTOs.CreativeItem saveCreative(DeliverAdminCommands.UpsertCreativeCommand command) {
        DeliverCreative existing = command.id() == null ? null : deliverCreativeRepository.findCreativeById(command.id()).orElseThrow(() -> new IllegalArgumentException("creative not found"));
        DeliverCreative saved = deliverCreativeRepository.saveCreative(new DeliverCreative(
                command.id(),
                required(command.creativeCode(), "creativeCode"),
                required(command.creativeName(), "creativeName"),
                required(command.unitCode(), "unitCode"),
                required(command.materialCode(), "materialCode"),
                trimToNull(command.landingUrl()),
                trimToNull(command.schemaJson()),
                defaultInt(command.priority(), 100),
                defaultInt(command.weight(), 0),
                null,
                Boolean.TRUE.equals(command.fallback()),
                trimToNull(command.previewImage()),
                parseEnum(command.status(), DeliverPublishStatus.class, existing == null ? DeliverPublishStatus.EDITING : existing.getStatus()),
                command.activeFrom(),
                command.activeTo(),
                existing == null ? null : existing.getCreatedAt(),
                null
        ));
        return toCreativeItem(saved);
    }

    /**
     * 删除创意信息。
     */
    @Override
    @Transactional
    public void deleteCreative(Long creativeId) {
        Long targetCreativeId = requirePositive(creativeId, "creativeId");
        deliverCreativeRepository.findCreativeById(targetCreativeId)
                .orElseThrow(() -> new IllegalArgumentException("creative not found"));
        relationRepository.deleteByCreativeId(targetCreativeId);
        deliverCreativeRepository.deleteCreativeById(targetCreativeId);
    }

    /**
     * 保存关联关系信息。
     */
    @Override
    @Transactional
    public DeliverAdminDTOs.RelationItem saveRelation(DeliverAdminCommands.UpsertRelationCommand command) {
        PositionUnitCreativeRelation existing = command.id() == null ? null : relationRepository.findRelationById(command.id()).orElseThrow(() -> new IllegalArgumentException("relation not found"));
        PositionUnitCreativeRelation saved = relationRepository.saveRelation(new PositionUnitCreativeRelation(
                command.id(),
                requirePositive(command.positionId(), "positionId"),
                requirePositive(command.unitId(), "unitId"),
                requirePositive(command.creativeId(), "creativeId"),
                defaultInt(command.displayOrder(), 100),
                Boolean.TRUE.equals(command.fallback()),
                command.enabled() == null || command.enabled(),
                existing == null ? null : existing.createdAt(),
                null
        ));
        return toRelationItem(saved);
    }

    /**
     * 保存频控规则。
     */
    @Override
    @Transactional
    public DeliverAdminDTOs.FatigueRuleItem saveFatigueRule(DeliverAdminCommands.UpsertFatigueRuleCommand command) {
        FatigueControlRule existing = command.id() == null ? null : fatigueControlRuleRepository.findFatigueRuleById(command.id()).orElseThrow(() -> new IllegalArgumentException("fatigue rule not found"));
        FatigueControlRule saved = fatigueControlRuleRepository.saveFatigueRule(new FatigueControlRule(
                command.id(),
                required(command.fatigueCode(), "fatigueCode"),
                required(command.ruleName(), "ruleName"),
                parseEnum(command.entityType(), DeliverEntityType.class, DeliverEntityType.CREATIVE),
                required(command.entityCode(), "entityCode"),
                parseEnum(command.eventType(), DeliverEventType.class, DeliverEventType.DISPLAY),
                defaultInt(command.timeWindowMinutes(), 60),
                defaultInt(command.maxCount(), 1),
                command.enabled() == null || command.enabled(),
                existing == null ? null : existing.createdAt(),
                null
        ));
        return toFatigueRuleItem(saved);
    }

    /**
     * 保存定向规则。
     */
    @Override
    @Transactional
    public DeliverAdminDTOs.TargetingRuleItem saveTargetingRule(DeliverAdminCommands.UpsertTargetingRuleCommand command) {
        TargetingRule existing = command.id() == null ? null : targetingRuleRepository.findTargetingRuleById(command.id()).orElseThrow(() -> new IllegalArgumentException("targeting rule not found"));
        TargetingRule saved = targetingRuleRepository.saveTargetingRule(new TargetingRule(
                command.id(),
                required(command.ruleCode(), "ruleCode"),
                parseEnum(command.entityType(), DeliverEntityType.class, DeliverEntityType.CREATIVE),
                required(command.entityCode(), "entityCode"),
                parseEnum(command.targetingType(), DeliverTargetingType.class, DeliverTargetingType.USER_TAG),
                parseEnum(command.operator(), DeliverTargetingOperator.class, DeliverTargetingOperator.IN),
                required(command.targetingValue(), "targetingValue"),
                command.enabled() == null || command.enabled(),
                existing == null ? null : existing.createdAt(),
                null
        ));
        return toTargetingRuleItem(saved);
    }

    private DeliverAdminDTOs.PositionItem toPositionItem(Position position) {
        return new DeliverAdminDTOs.PositionItem(
                position.getId(),
                position.getPositionCode(),
                position.getPositionName(),
                position.getPositionType() == null ? null : position.getPositionType().name(),
                position.getPreviewImage(),
                position.getSlideInterval(),
                position.getMaxDisplayCount(),
                position.getSortType() == null ? null : position.getSortType().name(),
                position.getSortRule(),
                position.isNeedFallback(),
                position.getStatus() == null ? null : position.getStatus().name(),
                position.getMemo(),
                position.getPublishedAt(),
                position.getActiveFrom(),
                position.getActiveTo(),
                position.getCreatedAt(),
                position.getUpdatedAt()
        );
    }

    private DeliverAdminDTOs.UnitItem toUnitItem(DeliverUnit unit) {
        return new DeliverAdminDTOs.UnitItem(
                unit.id(),
                unit.unitCode(),
                unit.unitName(),
                unit.priority(),
                unit.status() == null ? null : unit.status().name(),
                unit.memo(),
                unit.activeFrom(),
                unit.activeTo(),
                unit.createdAt(),
                unit.updatedAt()
        );
    }

    private DeliverAdminDTOs.MaterialItem toMaterialItem(DeliverMaterial material) {
        return new DeliverAdminDTOs.MaterialItem(
                material.id(),
                material.materialCode(),
                material.materialName(),
                material.materialType(),
                material.title(),
                material.imageUrl(),
                material.landingUrl(),
                material.schemaJson(),
                material.previewImage(),
                material.status() == null ? null : material.status().name(),
                material.activeFrom(),
                material.activeTo(),
                material.createdAt(),
                material.updatedAt()
        );
    }

    private DeliverAdminDTOs.CreativeItem toCreativeItem(DeliverCreative creative) {
        return new DeliverAdminDTOs.CreativeItem(
                creative.getId(),
                creative.getCreativeCode(),
                creative.getCreativeName(),
                creative.getUnitCode(),
                creative.getMaterialCode(),
                creative.getLandingUrl(),
                creative.getSchemaJson(),
                creative.getPriority(),
                creative.getWeight(),
                creative.isFallback(),
                creative.getPreviewImage(),
                creative.getStatus() == null ? null : creative.getStatus().name(),
                creative.getActiveFrom(),
                creative.getActiveTo(),
                creative.getCreatedAt(),
                creative.getUpdatedAt()
        );
    }

    private DeliverAdminDTOs.RelationItem toRelationItem(PositionUnitCreativeRelation relation) {
        return new DeliverAdminDTOs.RelationItem(
                relation.id(),
                relation.positionId(),
                relation.unitId(),
                relation.creativeId(),
                relation.displayOrder(),
                relation.fallback(),
                relation.enabled(),
                relation.createdAt(),
                relation.updatedAt()
        );
    }

    private DeliverAdminDTOs.FatigueRuleItem toFatigueRuleItem(FatigueControlRule rule) {
        return new DeliverAdminDTOs.FatigueRuleItem(
                rule.id(),
                rule.fatigueCode(),
                rule.ruleName(),
                rule.entityType() == null ? null : rule.entityType().name(),
                rule.entityCode(),
                rule.eventType() == null ? null : rule.eventType().name(),
                rule.timeWindowMinutes(),
                rule.maxCount(),
                rule.enabled(),
                rule.createdAt(),
                rule.updatedAt()
        );
    }

    private DeliverAdminDTOs.TargetingRuleItem toTargetingRuleItem(TargetingRule rule) {
        return new DeliverAdminDTOs.TargetingRuleItem(
                rule.id(),
                rule.ruleCode(),
                rule.entityType() == null ? null : rule.entityType().name(),
                rule.entityCode(),
                rule.targetingType() == null ? null : rule.targetingType().name(),
                rule.operator() == null ? null : rule.operator().name(),
                rule.targetingValue(),
                rule.enabled(),
                rule.createdAt(),
                rule.updatedAt()
        );
    }

    private LocalDateTime resolvePublishedAt(LocalDateTime existingPublishedAt,
                                             LocalDateTime requestedPublishedAt,
                                             DeliverPublishStatus status) {
        if (requestedPublishedAt != null) {
            return requestedPublishedAt;
        }
        if (status == DeliverPublishStatus.PUBLISHED) {
            return existingPublishedAt == null ? LocalDateTime.now() : existingPublishedAt;
        }
        return existingPublishedAt;
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String required(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private <T extends Enum<T>> T parseEnum(String raw, Class<T> enumType, T defaultValue) {
        String normalized = trimToNull(raw);
        if (normalized == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumType, normalized.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("invalid enum value: " + raw);
        }
    }
}
