package cn.openaipay.application.audience.facade;

import cn.openaipay.application.audience.command.UpsertAudienceSegmentCommand;
import cn.openaipay.application.audience.command.UpsertAudienceSegmentRuleCommand;
import cn.openaipay.application.audience.command.UpsertAudienceTagDefinitionCommand;
import cn.openaipay.application.audience.command.UpsertAudienceUserTagCommand;
import cn.openaipay.application.audience.dto.AudienceSegmentDTO;
import cn.openaipay.application.audience.dto.AudienceSegmentRuleDTO;
import cn.openaipay.application.audience.dto.AudienceTagDefinitionDTO;
import cn.openaipay.application.audience.dto.AudienceUserTagDTO;
import java.util.List;
import java.util.Set;

/**
 * 人群门面
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public interface AudienceFacade {

    /**
     * 查询标签定义
     */
    List<AudienceTagDefinitionDTO> listTagDefinitions();

    /**
     * 保存标签定义
     */
    AudienceTagDefinitionDTO saveTagDefinition(UpsertAudienceTagDefinitionCommand command);

    /**
     * 查询人群定义
     */
    List<AudienceSegmentDTO> listSegments();

    /**
     * 保存人群定义
     */
    AudienceSegmentDTO saveSegment(UpsertAudienceSegmentCommand command);

    /**
     * 查询人群规则
     */
    List<AudienceSegmentRuleDTO> listSegmentRules(String segmentCode);

    /**
     * 保存人群规则
     */
    AudienceSegmentRuleDTO saveSegmentRule(UpsertAudienceSegmentRuleCommand command);

    /**
     * 查询用户标签
     */
    List<AudienceUserTagDTO> listUserTags(Long userId);

    /**
     * 保存用户标签
     */
    AudienceUserTagDTO saveUserTag(UpsertAudienceUserTagCommand command);

    /**
     * 解析用户标签token
     */
    Set<String> resolveUserTagTokens(Long userId);

    /**
     * 计算用户命中人群
     */
    Set<String> matchPublishedSegmentCodes(Long userId);
}
