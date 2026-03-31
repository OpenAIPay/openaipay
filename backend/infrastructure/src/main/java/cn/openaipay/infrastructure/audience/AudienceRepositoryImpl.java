package cn.openaipay.infrastructure.audience;

import cn.openaipay.domain.audience.model.AudienceRuleOperator;
import cn.openaipay.domain.audience.model.AudienceRuleRelation;
import cn.openaipay.domain.audience.model.AudienceSegment;
import cn.openaipay.domain.audience.model.AudienceSegmentRule;
import cn.openaipay.domain.audience.model.AudienceSegmentStatus;
import cn.openaipay.domain.audience.model.AudienceTagDefinition;
import cn.openaipay.domain.audience.model.AudienceTagType;
import cn.openaipay.domain.audience.model.AudienceUserTagSnapshot;
import cn.openaipay.domain.audience.repository.AudienceRepository;
import cn.openaipay.infrastructure.audience.dataobject.AudienceSegmentDO;
import cn.openaipay.infrastructure.audience.dataobject.AudienceSegmentRuleDO;
import cn.openaipay.infrastructure.audience.dataobject.AudienceTagDefinitionDO;
import cn.openaipay.infrastructure.audience.dataobject.AudienceUserTagSnapshotDO;
import cn.openaipay.infrastructure.audience.mapper.AudienceSegmentMapper;
import cn.openaipay.infrastructure.audience.mapper.AudienceSegmentRuleMapper;
import cn.openaipay.infrastructure.audience.mapper.AudienceTagDefinitionMapper;
import cn.openaipay.infrastructure.audience.mapper.AudienceUserTagSnapshotMapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 人群仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Repository
public class AudienceRepositoryImpl implements AudienceRepository {

    /** 标签定义持久化接口 */
    private final AudienceTagDefinitionMapper audienceTagDefinitionMapper;
    /** 用户标签快照持久化接口 */
    private final AudienceUserTagSnapshotMapper audienceUserTagSnapshotMapper;
    /** 人群定义持久化接口 */
    private final AudienceSegmentMapper audienceSegmentMapper;
    /** 人群规则持久化接口 */
    private final AudienceSegmentRuleMapper audienceSegmentRuleMapper;

    public AudienceRepositoryImpl(AudienceTagDefinitionMapper audienceTagDefinitionMapper,
                                  AudienceUserTagSnapshotMapper audienceUserTagSnapshotMapper,
                                  AudienceSegmentMapper audienceSegmentMapper,
                                  AudienceSegmentRuleMapper audienceSegmentRuleMapper) {
        this.audienceTagDefinitionMapper = audienceTagDefinitionMapper;
        this.audienceUserTagSnapshotMapper = audienceUserTagSnapshotMapper;
        this.audienceSegmentMapper = audienceSegmentMapper;
        this.audienceSegmentRuleMapper = audienceSegmentRuleMapper;
    }

    /**
     * 查询标签定义
     */
    @Override
    public List<AudienceTagDefinition> listTagDefinitions() {
        return audienceTagDefinitionMapper.findAll().stream().map(this::toTagDefinition).toList();
    }

    /**
     * 按编码查询标签定义
     */
    @Override
    public Optional<AudienceTagDefinition> findTagDefinitionByCode(String tagCode) {
        return audienceTagDefinitionMapper.findByTagCode(tagCode).map(this::toTagDefinition);
    }

    /**
     * 保存标签定义
     */
    @Override
    public AudienceTagDefinition saveTagDefinition(AudienceTagDefinition tagDefinition) {
        AudienceTagDefinitionDO entity = audienceTagDefinitionMapper.findByTagCode(tagDefinition.tagCode())
                .orElse(new AudienceTagDefinitionDO());
        entity.setTagCode(tagDefinition.tagCode());
        entity.setTagName(tagDefinition.tagName());
        entity.setTagType(tagDefinition.tagType().name());
        entity.setValueScope(tagDefinition.valueScope());
        entity.setDescription(tagDefinition.description());
        entity.setEnabled(tagDefinition.enabled());
        entity.setCreatedAt(tagDefinition.createdAt());
        entity.setUpdatedAt(tagDefinition.updatedAt());
        AudienceTagDefinitionDO saved = audienceTagDefinitionMapper.save(entity);
        return toTagDefinition(saved);
    }

    /**
     * 按用户查询标签快照
     */
    @Override
    public List<AudienceUserTagSnapshot> listUserTags(Long userId) {
        return audienceUserTagSnapshotMapper.findByUserId(userId).stream().map(this::toUserTagSnapshot).toList();
    }

    /**
     * 保存用户标签快照
     */
    @Override
    public AudienceUserTagSnapshot saveUserTag(AudienceUserTagSnapshot userTagSnapshot) {
        AudienceUserTagSnapshotDO entity = audienceUserTagSnapshotMapper.findByUserIdAndTagCode(userTagSnapshot.userId(), userTagSnapshot.tagCode())
                .orElse(new AudienceUserTagSnapshotDO());
        entity.setUserId(userTagSnapshot.userId());
        entity.setTagCode(userTagSnapshot.tagCode());
        entity.setTagValue(userTagSnapshot.tagValue());
        entity.setSource(userTagSnapshot.source());
        entity.setValueUpdatedAt(userTagSnapshot.valueUpdatedAt());
        entity.setCreatedAt(userTagSnapshot.createdAt());
        entity.setUpdatedAt(userTagSnapshot.updatedAt());
        AudienceUserTagSnapshotDO saved = audienceUserTagSnapshotMapper.save(entity);
        return toUserTagSnapshot(saved);
    }

    /**
     * 查询人群定义
     */
    @Override
    public List<AudienceSegment> listSegments() {
        return audienceSegmentMapper.findAll().stream().map(this::toSegment).toList();
    }

    /**
     * 按编码查询人群定义
     */
    @Override
    public Optional<AudienceSegment> findSegmentByCode(String segmentCode) {
        return audienceSegmentMapper.findBySegmentCode(segmentCode).map(this::toSegment);
    }

    /**
     * 查询已发布人群
     */
    @Override
    public List<AudienceSegment> listPublishedSegments() {
        return audienceSegmentMapper.findByStatus(AudienceSegmentStatus.PUBLISHED.name()).stream()
                .map(this::toSegment)
                .toList();
    }

    /**
     * 保存人群定义
     */
    @Override
    public AudienceSegment saveSegment(AudienceSegment segment) {
        AudienceSegmentDO entity = audienceSegmentMapper.findBySegmentCode(segment.segmentCode())
                .orElse(new AudienceSegmentDO());
        entity.setSegmentCode(segment.segmentCode());
        entity.setSegmentName(segment.segmentName());
        entity.setDescription(segment.description());
        entity.setSceneCode(segment.sceneCode());
        entity.setStatus(segment.status().name());
        entity.setCreatedAt(segment.createdAt());
        entity.setUpdatedAt(segment.updatedAt());
        AudienceSegmentDO saved = audienceSegmentMapper.save(entity);
        return toSegment(saved);
    }

    /**
     * 按人群查询规则
     */
    @Override
    public List<AudienceSegmentRule> listSegmentRules(String segmentCode) {
        return audienceSegmentRuleMapper.findBySegmentCode(segmentCode).stream().map(this::toSegmentRule).toList();
    }

    /**
     * 按人群批量查询规则
     */
    @Override
    public List<AudienceSegmentRule> listSegmentRulesBySegmentCodes(Collection<String> segmentCodes) {
        return audienceSegmentRuleMapper.findBySegmentCodes(segmentCodes).stream().map(this::toSegmentRule).toList();
    }

    /**
     * 保存人群规则
     */
    @Override
    public AudienceSegmentRule saveSegmentRule(AudienceSegmentRule segmentRule) {
        AudienceSegmentRuleDO entity = audienceSegmentRuleMapper.findBySegmentCode(segmentRule.segmentCode()).stream()
                .filter(item -> segmentRule.ruleCode().equals(item.getRuleCode()))
                .findFirst()
                .orElse(new AudienceSegmentRuleDO());
        entity.setRuleCode(segmentRule.ruleCode());
        entity.setSegmentCode(segmentRule.segmentCode());
        entity.setTagCode(segmentRule.tagCode());
        entity.setOperator(segmentRule.operator().name());
        entity.setTargetValue(segmentRule.targetValue());
        entity.setRelationType(segmentRule.relation().name());
        entity.setEnabled(segmentRule.enabled());
        entity.setCreatedAt(segmentRule.createdAt());
        entity.setUpdatedAt(segmentRule.updatedAt());
        AudienceSegmentRuleDO saved = audienceSegmentRuleMapper.save(entity);
        return toSegmentRule(saved);
    }

    private AudienceTagDefinition toTagDefinition(AudienceTagDefinitionDO entity) {
        return new AudienceTagDefinition(
                entity.getId(),
                entity.getTagCode(),
                entity.getTagName(),
                enumOrDefault(entity.getTagType(), AudienceTagType.class, AudienceTagType.ENUM),
                entity.getValueScope(),
                entity.getDescription(),
                Boolean.TRUE.equals(entity.getEnabled()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AudienceUserTagSnapshot toUserTagSnapshot(AudienceUserTagSnapshotDO entity) {
        return new AudienceUserTagSnapshot(
                entity.getId(),
                entity.getUserId(),
                entity.getTagCode(),
                entity.getTagValue(),
                entity.getSource(),
                entity.getValueUpdatedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AudienceSegment toSegment(AudienceSegmentDO entity) {
        return new AudienceSegment(
                entity.getId(),
                entity.getSegmentCode(),
                entity.getSegmentName(),
                entity.getDescription(),
                entity.getSceneCode(),
                enumOrDefault(entity.getStatus(), AudienceSegmentStatus.class, AudienceSegmentStatus.DRAFT),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AudienceSegmentRule toSegmentRule(AudienceSegmentRuleDO entity) {
        return new AudienceSegmentRule(
                entity.getId(),
                entity.getRuleCode(),
                entity.getSegmentCode(),
                entity.getTagCode(),
                enumOrDefault(entity.getOperator(), AudienceRuleOperator.class, AudienceRuleOperator.EQ),
                entity.getTargetValue(),
                enumOrDefault(entity.getRelationType(), AudienceRuleRelation.class, AudienceRuleRelation.INCLUDE),
                Boolean.TRUE.equals(entity.getEnabled()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
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
}
