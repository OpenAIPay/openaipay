package cn.openaipay.domain.audience.repository;

import cn.openaipay.domain.audience.model.AudienceSegment;
import cn.openaipay.domain.audience.model.AudienceSegmentRule;
import cn.openaipay.domain.audience.model.AudienceTagDefinition;
import cn.openaipay.domain.audience.model.AudienceUserTagSnapshot;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 人群仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public interface AudienceRepository {

    /**
     * 查询标签定义
     */
    List<AudienceTagDefinition> listTagDefinitions();

    /**
     * 按编码查询标签定义
     */
    Optional<AudienceTagDefinition> findTagDefinitionByCode(String tagCode);

    /**
     * 保存标签定义
     */
    AudienceTagDefinition saveTagDefinition(AudienceTagDefinition tagDefinition);

    /**
     * 按用户查询标签快照
     */
    List<AudienceUserTagSnapshot> listUserTags(Long userId);

    /**
     * 保存用户标签快照
     */
    AudienceUserTagSnapshot saveUserTag(AudienceUserTagSnapshot userTagSnapshot);

    /**
     * 查询人群定义
     */
    List<AudienceSegment> listSegments();

    /**
     * 按编码查询人群定义
     */
    Optional<AudienceSegment> findSegmentByCode(String segmentCode);

    /**
     * 查询已发布人群
     */
    List<AudienceSegment> listPublishedSegments();

    /**
     * 保存人群定义
     */
    AudienceSegment saveSegment(AudienceSegment segment);

    /**
     * 按人群查询规则
     */
    List<AudienceSegmentRule> listSegmentRules(String segmentCode);

    /**
     * 按人群批量查询规则
     */
    List<AudienceSegmentRule> listSegmentRulesBySegmentCodes(Collection<String> segmentCodes);

    /**
     * 保存人群规则
     */
    AudienceSegmentRule saveSegmentRule(AudienceSegmentRule segmentRule);
}
