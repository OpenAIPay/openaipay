package cn.openaipay.infrastructure.deliver;

import cn.openaipay.domain.deliver.model.DeliverCreative;
import cn.openaipay.domain.deliver.model.DeliverEntityType;
import cn.openaipay.domain.deliver.model.DeliverEventRecord;
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
import cn.openaipay.domain.deliver.repository.DeliverEventRecordRepository;
import cn.openaipay.domain.deliver.repository.DeliverMaterialRepository;
import cn.openaipay.domain.deliver.repository.DeliverUnitRepository;
import cn.openaipay.domain.deliver.repository.FatigueControlRuleRepository;
import cn.openaipay.domain.deliver.repository.PositionRepository;
import cn.openaipay.domain.deliver.repository.PositionUnitCreativeRelationRepository;
import cn.openaipay.domain.deliver.repository.TargetingRuleRepository;
import cn.openaipay.infrastructure.deliver.dataobject.DeliverCreativeDO;
import cn.openaipay.infrastructure.deliver.dataobject.DeliverEventRecordDO;
import cn.openaipay.infrastructure.deliver.dataobject.DeliverMaterialDO;
import cn.openaipay.infrastructure.deliver.dataobject.DeliverPositionDO;
import cn.openaipay.infrastructure.deliver.dataobject.DeliverUnitDO;
import cn.openaipay.infrastructure.deliver.dataobject.FatigueControlRuleDO;
import cn.openaipay.infrastructure.deliver.dataobject.PositionUnitCreativeRelationDO;
import cn.openaipay.infrastructure.deliver.dataobject.TargetingRuleDO;
import cn.openaipay.infrastructure.deliver.mapper.DeliverCreativeMapper;
import cn.openaipay.infrastructure.deliver.mapper.DeliverCreativeSnapshotRow;
import cn.openaipay.infrastructure.deliver.mapper.DeliverEventRecordMapper;
import cn.openaipay.infrastructure.deliver.mapper.DeliverMaterialMapper;
import cn.openaipay.infrastructure.deliver.mapper.DeliverPositionMapper;
import cn.openaipay.infrastructure.deliver.mapper.DeliverUnitMapper;
import cn.openaipay.infrastructure.deliver.mapper.FatigueControlRuleMapper;
import cn.openaipay.infrastructure.deliver.mapper.PositionUnitCreativeRelationMapper;
import cn.openaipay.infrastructure.deliver.mapper.TargetingRuleMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * DeliverRepositoryImpl 仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Repository
public class DeliverRepositoryImpl implements PositionRepository,
        DeliverUnitRepository,
        DeliverCreativeRepository,
        PositionUnitCreativeRelationRepository,
        DeliverMaterialRepository,
        FatigueControlRuleRepository,
        TargetingRuleRepository,
        DeliverEventRecordRepository {

    /** 投放位置信息 */
    private final DeliverPositionMapper deliverPositionMapper;
    /** 投放单元信息 */
    private final DeliverUnitMapper deliverUnitMapper;
    /** 投放信息 */
    private final DeliverCreativeMapper deliverCreativeMapper;
    /** 位置单元信息 */
    private final PositionUnitCreativeRelationMapper positionUnitCreativeRelationMapper;
    /** 投放信息 */
    private final DeliverMaterialMapper deliverMaterialMapper;
    /** 规则信息 */
    private final FatigueControlRuleMapper fatigueControlRuleMapper;
    /** 规则信息 */
    private final TargetingRuleMapper targetingRuleMapper;
    /** 投放事件记录信息 */
    private final DeliverEventRecordMapper deliverEventRecordMapper;

    public DeliverRepositoryImpl(DeliverPositionMapper deliverPositionMapper,
                                 DeliverUnitMapper deliverUnitMapper,
                                 DeliverCreativeMapper deliverCreativeMapper,
                                 PositionUnitCreativeRelationMapper positionUnitCreativeRelationMapper,
                                 DeliverMaterialMapper deliverMaterialMapper,
                                 FatigueControlRuleMapper fatigueControlRuleMapper,
                                 TargetingRuleMapper targetingRuleMapper,
                                 DeliverEventRecordMapper deliverEventRecordMapper) {
        this.deliverPositionMapper = deliverPositionMapper;
        this.deliverUnitMapper = deliverUnitMapper;
        this.deliverCreativeMapper = deliverCreativeMapper;
        this.positionUnitCreativeRelationMapper = positionUnitCreativeRelationMapper;
        this.deliverMaterialMapper = deliverMaterialMapper;
        this.fatigueControlRuleMapper = fatigueControlRuleMapper;
        this.targetingRuleMapper = targetingRuleMapper;
        this.deliverEventRecordMapper = deliverEventRecordMapper;
    }

    /**
     * 按编码查找记录。
     */
    @Override
    public Optional<Position> findPublishedByCode(String positionCode, LocalDateTime now) {
        return deliverPositionMapper.findPublishedByCode(positionCode, now).map(this::toDomainPosition);
    }

    /**
     * 按ID查找广告位信息。
     */
    @Override
    public Optional<Position> findPositionById(Long id) {
        return deliverPositionMapper.findById(id).map(this::toDomainPosition);
    }

    /**
     * 查找ALL广告位信息。
     */
    @Override
    public List<Position> findAllPositions() {
        return deliverPositionMapper.findAll().stream().map(this::toDomainPosition).toList();
    }

    /**
     * 保存广告位信息。
     */
    @Override
    public Position savePosition(Position position) {
        DeliverPositionDO entity = toDO(position);
        deliverPositionMapper.save(entity);
        return toDomainPosition(entity);
    }

    /**
     * 按ID查找单元信息。
     */
    @Override
    public Optional<DeliverUnit> findUnitById(Long id) {
        return deliverUnitMapper.findById(id).map(this::toDomainUnit);
    }

    /**
     * 查找ALL单元信息。
     */
    @Override
    public List<DeliverUnit> findAllUnits() {
        return deliverUnitMapper.findAll().stream().map(this::toDomainUnit).toList();
    }

    /**
     * 保存单元信息。
     */
    @Override
    public DeliverUnit saveUnit(DeliverUnit unit) {
        DeliverUnitDO entity = toDO(unit);
        deliverUnitMapper.save(entity);
        return toDomainUnit(entity);
    }

    /**
     * 按ID查找创意信息。
     */
    @Override
    public Optional<DeliverCreative> findCreativeById(Long id) {
        return deliverCreativeMapper.findById(id).map(this::toDomainCreative);
    }

    /**
     * 查找ALL创意信息。
     */
    @Override
    public List<DeliverCreative> findAllCreatives() {
        return deliverCreativeMapper.findAll().stream().map(this::toDomainCreative).toList();
    }

    /**
     * 保存创意信息。
     */
    @Override
    public DeliverCreative saveCreative(DeliverCreative creative) {
        DeliverCreativeDO entity = toDO(creative);
        deliverCreativeMapper.save(entity);
        return toDomainCreative(entity);
    }

    /**
     * 按ID删除创意信息。
     */
    @Override
    public void deleteCreativeById(Long creativeId) {
        if (creativeId == null || creativeId <= 0) {
            return;
        }
        deliverCreativeMapper.deleteById(creativeId);
    }

    /**
     * 查询广告位创意列表。
     */
    @Override
    public List<DeliverCreative> queryPositionCreativeList(Long positionId, boolean fallback, LocalDateTime now) {
        return positionUnitCreativeRelationMapper.selectDeliverCreativeSnapshots(positionId, fallback, now)
                .stream()
                .map(this::toDomainCreative)
                .toList();
    }

    /**
     * 按广告位ID查找记录。
     */
    @Override
    public List<PositionUnitCreativeRelation> findByPositionId(Long positionId) {
        return positionUnitCreativeRelationMapper.findByPositionId(positionId)
                .stream()
                .map(this::toDomainRelation)
                .toList();
    }

    /**
     * 按ID查找关联关系信息。
     */
    @Override
    public Optional<PositionUnitCreativeRelation> findRelationById(Long id) {
        return positionUnitCreativeRelationMapper.findById(id).map(this::toDomainRelation);
    }

    /**
     * 查找ALL关联关系信息。
     */
    @Override
    public List<PositionUnitCreativeRelation> findAllRelations() {
        return positionUnitCreativeRelationMapper.findAll().stream().map(this::toDomainRelation).toList();
    }

    /**
     * 保存关联关系信息。
     */
    @Override
    public PositionUnitCreativeRelation saveRelation(PositionUnitCreativeRelation relation) {
        PositionUnitCreativeRelationDO entity = toDO(relation);
        positionUnitCreativeRelationMapper.save(entity);
        return toDomainRelation(entity);
    }

    /**
     * 按创意ID删除关联关系信息。
     */
    @Override
    public void deleteByCreativeId(Long creativeId) {
        if (creativeId == null || creativeId <= 0) {
            return;
        }
        QueryWrapper<PositionUnitCreativeRelationDO> wrapper = new QueryWrapper<>();
        wrapper.eq("creative_id", creativeId);
        positionUnitCreativeRelationMapper.delete(wrapper);
    }

    /**
     * 按编码查找记录。
     */
    @Override
    public Map<String, DeliverMaterial> findByCodes(Collection<String> materialCodes) {
        return deliverMaterialMapper.findByCodes(materialCodes)
                .stream()
                .map(this::toDomainMaterial)
                .collect(Collectors.toMap(DeliverMaterial::materialCode, Function.identity(), (left, right) -> right));
    }

    /**
     * 按ID查找素材信息。
     */
    @Override
    public Optional<DeliverMaterial> findMaterialById(Long id) {
        return deliverMaterialMapper.findById(id).map(this::toDomainMaterial);
    }

    /**
     * 查找ALL素材信息。
     */
    @Override
    public List<DeliverMaterial> findAllMaterials() {
        return deliverMaterialMapper.findAll().stream().map(this::toDomainMaterial).toList();
    }

    /**
     * 保存素材信息。
     */
    @Override
    public DeliverMaterial saveMaterial(DeliverMaterial material) {
        DeliverMaterialDO entity = toDO(material);
        deliverMaterialMapper.save(entity);
        return toDomainMaterial(entity);
    }

    /**
     * 按与编码查找频控规则。
     */
    @Override
    public List<FatigueControlRule> findFatigueRulesByEntityTypeAndCodes(DeliverEntityType entityType, Collection<String> entityCodes) {
        return fatigueControlRuleMapper.findEnabledByEntityTypeAndCodes(entityType.name(), entityCodes)
                .stream()
                .map(this::toDomainFatigueRule)
                .toList();
    }

    /**
     * 按ID查找频控规则。
     */
    @Override
    public Optional<FatigueControlRule> findFatigueRuleById(Long id) {
        return fatigueControlRuleMapper.findById(id).map(this::toDomainFatigueRule);
    }

    /**
     * 查找ALL频控规则。
     */
    @Override
    public List<FatigueControlRule> findAllFatigueRules() {
        return fatigueControlRuleMapper.findAll().stream().map(this::toDomainFatigueRule).toList();
    }

    /**
     * 保存频控规则。
     */
    @Override
    public FatigueControlRule saveFatigueRule(FatigueControlRule rule) {
        FatigueControlRuleDO entity = toDO(rule);
        fatigueControlRuleMapper.save(entity);
        return toDomainFatigueRule(entity);
    }

    /**
     * 按与编码查找定向规则。
     */
    @Override
    public List<TargetingRule> findTargetingRulesByEntityTypeAndCodes(DeliverEntityType entityType, Collection<String> entityCodes) {
        return targetingRuleMapper.findEnabledByEntityTypeAndCodes(entityType.name(), entityCodes)
                .stream()
                .map(this::toDomainTargetingRule)
                .toList();
    }

    /**
     * 按ID查找定向规则。
     */
    @Override
    public Optional<TargetingRule> findTargetingRuleById(Long id) {
        return targetingRuleMapper.findById(id).map(this::toDomainTargetingRule);
    }

    /**
     * 查找ALL定向规则。
     */
    @Override
    public List<TargetingRule> findAllTargetingRules() {
        return targetingRuleMapper.findAll().stream().map(this::toDomainTargetingRule).toList();
    }

    /**
     * 保存定向规则。
     */
    @Override
    public TargetingRule saveTargetingRule(TargetingRule rule) {
        TargetingRuleDO entity = toDO(rule);
        targetingRuleMapper.save(entity);
        return toDomainTargetingRule(entity);
    }

    /**
     * 处理数量信息。
     */
    @Override
    public long countRecent(String clientId,
                            Long userId,
                            DeliverEntityType entityType,
                            String entityCode,
                            DeliverEventType eventType,
                            LocalDateTime fromInclusive,
                            LocalDateTime toExclusive) {
        return deliverEventRecordMapper.countRecent(
                clientId,
                userId,
                entityType.name(),
                entityCode,
                eventType.name(),
                fromInclusive,
                toExclusive
        );
    }

    /**
     * 保存ALL信息。
     */
    @Override
    public void saveAll(Collection<DeliverEventRecord> eventRecords) {
        if (eventRecords == null || eventRecords.isEmpty()) {
            return;
        }
        deliverEventRecordMapper.saveAll(eventRecords.stream().map(this::toDO).toList());
    }

    private Position toDomainPosition(DeliverPositionDO entity) {
        return new Position(
                entity.getId(),
                entity.getPositionCode(),
                entity.getPositionName(),
                enumOrDefault(entity.getPositionType(), DeliverPositionType.class, DeliverPositionType.BANNER),
                entity.getPreviewImage(),
                entity.getSlideInterval(),
                entity.getMaxDisplayCount(),
                enumOrDefault(entity.getSortType(), DeliverSortType.class, DeliverSortType.PRIORITY),
                entity.getSortRule(),
                Boolean.TRUE.equals(entity.getNeedFallback()),
                enumOrDefault(entity.getStatus(), DeliverPublishStatus.class, DeliverPublishStatus.OFFLINE),
                entity.getMemo(),
                entity.getPublishedAt(),
                entity.getActiveFrom(),
                entity.getActiveTo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                List.of()
        );
    }

    private DeliverUnit toDomainUnit(DeliverUnitDO entity) {
        return new DeliverUnit(
                entity.getId(),
                entity.getUnitCode(),
                entity.getUnitName(),
                entity.getPriority(),
                enumOrDefault(entity.getStatus(), DeliverPublishStatus.class, DeliverPublishStatus.OFFLINE),
                entity.getActiveFrom(),
                entity.getActiveTo(),
                entity.getMemo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private DeliverCreative toDomainCreative(DeliverCreativeSnapshotRow row) {
        return new DeliverCreative(
                row.getCreativeId(),
                row.getCreativeCode(),
                row.getCreativeName(),
                row.getUnitCode(),
                row.getMaterialCode(),
                row.getLandingUrl(),
                row.getSchemaJson(),
                row.getPriority(),
                row.getWeight(),
                row.getDisplayOrder(),
                Boolean.TRUE.equals(row.getFallback()),
                row.getPreviewImage(),
                enumOrDefault(row.getStatus(), DeliverPublishStatus.class, DeliverPublishStatus.OFFLINE),
                row.getActiveFrom(),
                row.getActiveTo(),
                row.getCreatedAt(),
                row.getUpdatedAt()
        );
    }

    private DeliverCreative toDomainCreative(DeliverCreativeDO entity) {
        return new DeliverCreative(
                entity.getId(),
                entity.getCreativeCode(),
                entity.getCreativeName(),
                entity.getUnitCode(),
                entity.getMaterialCode(),
                entity.getLandingUrl(),
                entity.getSchemaJson(),
                entity.getPriority(),
                entity.getWeight(),
                null,
                Boolean.TRUE.equals(entity.getFallback()),
                entity.getPreviewImage(),
                enumOrDefault(entity.getStatus(), DeliverPublishStatus.class, DeliverPublishStatus.OFFLINE),
                entity.getActiveFrom(),
                entity.getActiveTo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private PositionUnitCreativeRelation toDomainRelation(PositionUnitCreativeRelationDO entity) {
        return new PositionUnitCreativeRelation(
                entity.getId(),
                entity.getPositionId(),
                entity.getUnitId(),
                entity.getCreativeId(),
                entity.getDisplayOrder(),
                Boolean.TRUE.equals(entity.getFallback()),
                Boolean.TRUE.equals(entity.getEnabled()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private DeliverMaterial toDomainMaterial(DeliverMaterialDO entity) {
        return new DeliverMaterial(
                entity.getId(),
                entity.getMaterialCode(),
                entity.getMaterialName(),
                entity.getMaterialType(),
                entity.getTitle(),
                entity.getImageUrl(),
                entity.getLandingUrl(),
                entity.getSchemaJson(),
                entity.getPreviewImage(),
                enumOrDefault(entity.getStatus(), DeliverPublishStatus.class, DeliverPublishStatus.OFFLINE),
                entity.getActiveFrom(),
                entity.getActiveTo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private FatigueControlRule toDomainFatigueRule(FatigueControlRuleDO entity) {
        return new FatigueControlRule(
                entity.getId(),
                entity.getFatigueCode(),
                entity.getRuleName(),
                enumOrDefault(entity.getEntityType(), DeliverEntityType.class, DeliverEntityType.CREATIVE),
                entity.getEntityCode(),
                enumOrDefault(entity.getEventType(), DeliverEventType.class, DeliverEventType.DISPLAY),
                entity.getTimeWindowMinutes(),
                entity.getMaxCount(),
                Boolean.TRUE.equals(entity.getEnabled()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private TargetingRule toDomainTargetingRule(TargetingRuleDO entity) {
        return new TargetingRule(
                entity.getId(),
                entity.getRuleCode(),
                enumOrDefault(entity.getEntityType(), DeliverEntityType.class, DeliverEntityType.CREATIVE),
                entity.getEntityCode(),
                enumOrDefault(entity.getTargetingType(), DeliverTargetingType.class, DeliverTargetingType.USER_TAG),
                enumOrDefault(entity.getOperator(), DeliverTargetingOperator.class, DeliverTargetingOperator.IN),
                entity.getTargetingValue(),
                Boolean.TRUE.equals(entity.getEnabled()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private DeliverPositionDO toDO(Position position) {
        DeliverPositionDO entity = new DeliverPositionDO();
        applyId(entity, position.getId());
        entity.setPositionCode(position.getPositionCode());
        entity.setPositionName(position.getPositionName());
        entity.setPositionType(enumName(position.getPositionType(), DeliverPositionType.BANNER));
        entity.setPreviewImage(position.getPreviewImage());
        entity.setSlideInterval(position.getSlideInterval());
        entity.setMaxDisplayCount(position.getMaxDisplayCount());
        entity.setSortType(enumName(position.getSortType(), DeliverSortType.PRIORITY));
        entity.setSortRule(position.getSortRule());
        entity.setNeedFallback(position.isNeedFallback());
        entity.setStatus(enumName(position.getStatus(), DeliverPublishStatus.EDITING));
        entity.setMemo(position.getMemo());
        entity.setPublishedAt(position.getPublishedAt());
        entity.setActiveFrom(position.getActiveFrom());
        entity.setActiveTo(position.getActiveTo());
        entity.setCreatedAt(position.getCreatedAt());
        entity.setUpdatedAt(position.getUpdatedAt());
        return entity;
    }

    private DeliverUnitDO toDO(DeliverUnit unit) {
        DeliverUnitDO entity = new DeliverUnitDO();
        applyId(entity, unit.id());
        entity.setUnitCode(unit.unitCode());
        entity.setUnitName(unit.unitName());
        entity.setPriority(unit.priority());
        entity.setStatus(enumName(unit.status(), DeliverPublishStatus.EDITING));
        entity.setMemo(unit.memo());
        entity.setActiveFrom(unit.activeFrom());
        entity.setActiveTo(unit.activeTo());
        entity.setCreatedAt(unit.createdAt());
        entity.setUpdatedAt(unit.updatedAt());
        return entity;
    }

    private DeliverCreativeDO toDO(DeliverCreative creative) {
        DeliverCreativeDO entity = new DeliverCreativeDO();
        applyId(entity, creative.getId());
        entity.setCreativeCode(creative.getCreativeCode());
        entity.setCreativeName(creative.getCreativeName());
        entity.setUnitCode(creative.getUnitCode());
        entity.setMaterialCode(creative.getMaterialCode());
        entity.setLandingUrl(creative.getLandingUrl());
        entity.setSchemaJson(creative.getSchemaJson());
        entity.setPriority(creative.getPriority());
        entity.setWeight(creative.getWeight());
        entity.setFallback(creative.isFallback());
        entity.setPreviewImage(creative.getPreviewImage());
        entity.setStatus(enumName(creative.getStatus(), DeliverPublishStatus.EDITING));
        entity.setActiveFrom(creative.getActiveFrom());
        entity.setActiveTo(creative.getActiveTo());
        entity.setCreatedAt(creative.getCreatedAt());
        entity.setUpdatedAt(creative.getUpdatedAt());
        return entity;
    }

    private PositionUnitCreativeRelationDO toDO(PositionUnitCreativeRelation relation) {
        PositionUnitCreativeRelationDO entity = new PositionUnitCreativeRelationDO();
        applyId(entity, relation.id());
        entity.setPositionId(relation.positionId());
        entity.setUnitId(relation.unitId());
        entity.setCreativeId(relation.creativeId());
        entity.setDisplayOrder(relation.displayOrder());
        entity.setFallback(relation.fallback());
        entity.setEnabled(relation.enabled());
        entity.setCreatedAt(relation.createdAt());
        entity.setUpdatedAt(relation.updatedAt());
        return entity;
    }

    private DeliverMaterialDO toDO(DeliverMaterial material) {
        DeliverMaterialDO entity = new DeliverMaterialDO();
        applyId(entity, material.id());
        entity.setMaterialCode(material.materialCode());
        entity.setMaterialName(material.materialName());
        entity.setMaterialType(material.materialType());
        entity.setTitle(material.title());
        entity.setImageUrl(material.imageUrl());
        entity.setLandingUrl(material.landingUrl());
        entity.setSchemaJson(material.schemaJson());
        entity.setPreviewImage(material.previewImage());
        entity.setStatus(enumName(material.status(), DeliverPublishStatus.EDITING));
        entity.setActiveFrom(material.activeFrom());
        entity.setActiveTo(material.activeTo());
        entity.setCreatedAt(material.createdAt());
        entity.setUpdatedAt(material.updatedAt());
        return entity;
    }

    private FatigueControlRuleDO toDO(FatigueControlRule rule) {
        FatigueControlRuleDO entity = new FatigueControlRuleDO();
        applyId(entity, rule.id());
        entity.setFatigueCode(rule.fatigueCode());
        entity.setRuleName(rule.ruleName());
        entity.setEntityType(enumName(rule.entityType(), DeliverEntityType.CREATIVE));
        entity.setEntityCode(rule.entityCode());
        entity.setEventType(enumName(rule.eventType(), DeliverEventType.DISPLAY));
        entity.setTimeWindowMinutes(rule.timeWindowMinutes());
        entity.setMaxCount(rule.maxCount());
        entity.setEnabled(rule.enabled());
        entity.setCreatedAt(rule.createdAt());
        entity.setUpdatedAt(rule.updatedAt());
        return entity;
    }

    private TargetingRuleDO toDO(TargetingRule rule) {
        TargetingRuleDO entity = new TargetingRuleDO();
        applyId(entity, rule.id());
        entity.setRuleCode(rule.ruleCode());
        entity.setEntityType(enumName(rule.entityType(), DeliverEntityType.CREATIVE));
        entity.setEntityCode(rule.entityCode());
        entity.setTargetingType(enumName(rule.targetingType(), DeliverTargetingType.USER_TAG));
        entity.setOperator(enumName(rule.operator(), DeliverTargetingOperator.IN));
        entity.setTargetingValue(rule.targetingValue());
        entity.setEnabled(rule.enabled());
        entity.setCreatedAt(rule.createdAt());
        entity.setUpdatedAt(rule.updatedAt());
        return entity;
    }

    private DeliverEventRecordDO toDO(DeliverEventRecord record) {
        DeliverEventRecordDO entity = new DeliverEventRecordDO();
        applyId(entity, record.id());
        entity.setClientId(record.clientId());
        entity.setUserId(record.userId());
        entity.setEntityType(record.entityType().name());
        entity.setEntityCode(record.entityCode());
        entity.setPositionCode(record.positionCode());
        entity.setUnitCode(record.unitCode());
        entity.setCreativeCode(record.creativeCode());
        entity.setEventType(record.eventType().name());
        entity.setSceneCode(record.sceneCode());
        entity.setChannel(record.channel());
        entity.setEventTime(record.eventTime());
        entity.setCreatedAt(record.createdAt() == null ? record.eventTime() : record.createdAt());
        return entity;
    }

    private void applyId(Object target, Long id) {
        if (target == null || id == null) {
            return;
        }
        Class<?> current = target.getClass();
        while (current != null && current != Object.class) {
            try {
                Field field = current.getDeclaredField("id");
                field.setAccessible(true);
                field.set(target, id);
                return;
            } catch (NoSuchFieldException ignore) {
                current = current.getSuperclass();
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("unable to set id field", exception);
            }
        }
    }

    private <T extends Enum<T>> T enumOrDefault(String raw, Class<T> enumType, T defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumType, raw.trim().toUpperCase());
        } catch (IllegalArgumentException ignore) {
            return defaultValue;
        }
    }

    private <T extends Enum<T>> String enumName(T value, T defaultValue) {
        return value == null ? defaultValue.name() : value.name();
    }
}
