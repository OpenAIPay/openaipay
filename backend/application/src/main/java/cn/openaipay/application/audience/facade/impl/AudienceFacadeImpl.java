package cn.openaipay.application.audience.facade.impl;

import cn.openaipay.application.audience.command.UpsertAudienceSegmentCommand;
import cn.openaipay.application.audience.command.UpsertAudienceSegmentRuleCommand;
import cn.openaipay.application.audience.command.UpsertAudienceTagDefinitionCommand;
import cn.openaipay.application.audience.command.UpsertAudienceUserTagCommand;
import cn.openaipay.application.audience.dto.AudienceSegmentDTO;
import cn.openaipay.application.audience.dto.AudienceSegmentRuleDTO;
import cn.openaipay.application.audience.dto.AudienceTagDefinitionDTO;
import cn.openaipay.application.audience.dto.AudienceUserTagDTO;
import cn.openaipay.application.audience.facade.AudienceFacade;
import cn.openaipay.application.audience.service.AudienceService;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * 人群门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Service
public class AudienceFacadeImpl implements AudienceFacade {

    /** 人群服务 */
    private final AudienceService audienceService;

    public AudienceFacadeImpl(AudienceService audienceService) {
        this.audienceService = audienceService;
    }

    /**
     * 查询标签定义
     */
    @Override
    public List<AudienceTagDefinitionDTO> listTagDefinitions() {
        return audienceService.listTagDefinitions();
    }

    /**
     * 保存标签定义
     */
    @Override
    public AudienceTagDefinitionDTO saveTagDefinition(UpsertAudienceTagDefinitionCommand command) {
        return audienceService.saveTagDefinition(command);
    }

    /**
     * 查询人群定义
     */
    @Override
    public List<AudienceSegmentDTO> listSegments() {
        return audienceService.listSegments();
    }

    /**
     * 保存人群定义
     */
    @Override
    public AudienceSegmentDTO saveSegment(UpsertAudienceSegmentCommand command) {
        return audienceService.saveSegment(command);
    }

    /**
     * 查询人群规则
     */
    @Override
    public List<AudienceSegmentRuleDTO> listSegmentRules(String segmentCode) {
        return audienceService.listSegmentRules(segmentCode);
    }

    /**
     * 保存人群规则
     */
    @Override
    public AudienceSegmentRuleDTO saveSegmentRule(UpsertAudienceSegmentRuleCommand command) {
        return audienceService.saveSegmentRule(command);
    }

    /**
     * 查询用户标签
     */
    @Override
    public List<AudienceUserTagDTO> listUserTags(Long userId) {
        return audienceService.listUserTags(userId);
    }

    /**
     * 保存用户标签
     */
    @Override
    public AudienceUserTagDTO saveUserTag(UpsertAudienceUserTagCommand command) {
        return audienceService.saveUserTag(command);
    }

    /**
     * 解析用户标签token
     */
    @Override
    public Set<String> resolveUserTagTokens(Long userId) {
        return audienceService.resolveUserTagTokens(userId);
    }

    /**
     * 计算用户命中人群
     */
    @Override
    public Set<String> matchPublishedSegmentCodes(Long userId) {
        return audienceService.matchPublishedSegmentCodes(userId);
    }
}
