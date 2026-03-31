package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.audience.command.UpsertAudienceSegmentCommand;
import cn.openaipay.application.audience.command.UpsertAudienceSegmentRuleCommand;
import cn.openaipay.application.audience.command.UpsertAudienceTagDefinitionCommand;
import cn.openaipay.application.audience.command.UpsertAudienceUserTagCommand;
import cn.openaipay.application.audience.dto.AudienceSegmentDTO;
import cn.openaipay.application.audience.dto.AudienceSegmentRuleDTO;
import cn.openaipay.application.audience.dto.AudienceTagDefinitionDTO;
import cn.openaipay.application.audience.dto.AudienceUserTagDTO;
import cn.openaipay.application.audience.facade.AudienceFacade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 人群管理控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Validated
@RestController
@RequestMapping("/api/admin/audience")
public class AdminAudienceController {

    /** 人群门面 */
    private final AudienceFacade audienceFacade;

    public AdminAudienceController(AudienceFacade audienceFacade) {
        this.audienceFacade = audienceFacade;
    }

    /**
     * 查询标签定义
     */
    @GetMapping("/tag-definitions")
    @RequireAdminPermission("audience.view")
    public ApiResponse<List<AudienceTagDefinitionDTO>> listTagDefinitions() {
        return ApiResponse.success(audienceFacade.listTagDefinitions());
    }

    /**
     * 保存标签定义
     */
    @PostMapping("/tag-definitions")
    @RequireAdminPermission("audience.manage")
    public ApiResponse<AudienceTagDefinitionDTO> saveTagDefinition(@Valid @RequestBody SaveTagDefinitionRequest request) {
        return ApiResponse.success(audienceFacade.saveTagDefinition(new UpsertAudienceTagDefinitionCommand(
                request.tagCode(),
                request.tagName(),
                request.tagType(),
                request.valueScope(),
                request.description(),
                request.enabled()
        )));
    }

    /**
     * 查询人群定义
     */
    @GetMapping("/segments")
    @RequireAdminPermission("audience.view")
    public ApiResponse<List<AudienceSegmentDTO>> listSegments() {
        return ApiResponse.success(audienceFacade.listSegments());
    }

    /**
     * 保存人群定义
     */
    @PostMapping("/segments")
    @RequireAdminPermission("audience.manage")
    public ApiResponse<AudienceSegmentDTO> saveSegment(@Valid @RequestBody SaveSegmentRequest request) {
        return ApiResponse.success(audienceFacade.saveSegment(new UpsertAudienceSegmentCommand(
                request.segmentCode(),
                request.segmentName(),
                request.description(),
                request.sceneCode(),
                request.status()
        )));
    }

    /**
     * 查询人群规则
     */
    @GetMapping("/segment-rules")
    @RequireAdminPermission("audience.view")
    public ApiResponse<List<AudienceSegmentRuleDTO>> listSegmentRules(
            @RequestParam("segmentCode") @NotBlank String segmentCode) {
        return ApiResponse.success(audienceFacade.listSegmentRules(segmentCode));
    }

    /**
     * 保存人群规则
     */
    @PostMapping("/segment-rules")
    @RequireAdminPermission("audience.manage")
    public ApiResponse<AudienceSegmentRuleDTO> saveSegmentRule(@Valid @RequestBody SaveSegmentRuleRequest request) {
        return ApiResponse.success(audienceFacade.saveSegmentRule(new UpsertAudienceSegmentRuleCommand(
                request.ruleCode(),
                request.segmentCode(),
                request.tagCode(),
                request.operator(),
                request.targetValue(),
                request.relation(),
                request.enabled()
        )));
    }

    /**
     * 查询用户标签
     */
    @GetMapping("/user-tags")
    @RequireAdminPermission("audience.view")
    public ApiResponse<List<AudienceUserTagDTO>> listUserTags(@RequestParam("userId") @NotNull @Positive Long userId) {
        return ApiResponse.success(audienceFacade.listUserTags(userId));
    }

    /**
     * 保存用户标签
     */
    @PostMapping("/user-tags")
    @RequireAdminPermission("audience.manage")
    public ApiResponse<AudienceUserTagDTO> saveUserTag(@Valid @RequestBody SaveUserTagRequest request) {
        return ApiResponse.success(audienceFacade.saveUserTag(new UpsertAudienceUserTagCommand(
                request.userId(),
                request.tagCode(),
                request.tagValue(),
                request.source(),
                request.valueUpdatedAt()
        )));
    }

    /**
     * 查询用户标签token
     */
    @GetMapping("/user-tag-tokens")
    @RequireAdminPermission("audience.view")
    public ApiResponse<Set<String>> resolveUserTagTokens(@RequestParam("userId") @NotNull @Positive Long userId) {
        return ApiResponse.success(audienceFacade.resolveUserTagTokens(userId));
    }

    /**
     * 查询命中人群编码
     */
    @GetMapping("/segment-matches")
    @RequireAdminPermission("audience.view")
    public ApiResponse<Set<String>> matchSegments(@RequestParam("userId") @NotNull @Positive Long userId) {
        return ApiResponse.success(audienceFacade.matchPublishedSegmentCodes(userId));
    }

    public record SaveTagDefinitionRequest(
            /** 标签编码 */
            @NotBlank String tagCode,
            /** 标签名称 */
            @NotBlank String tagName,
            /** 标签类型 */
            String tagType,
            /** 标签值域 */
            String valueScope,
            /** 标签描述 */
            String description,
            /** 是否启用 */
            Boolean enabled
    ) {
    }

    public record SaveSegmentRequest(
            /** 人群编码 */
            @NotBlank String segmentCode,
            /** 人群名称 */
            @NotBlank String segmentName,
            /** 人群描述 */
            String description,
            /** 场景编码 */
            String sceneCode,
            /** 人群状态 */
            String status
    ) {
    }

    public record SaveSegmentRuleRequest(
            /** 规则编码 */
            @NotBlank String ruleCode,
            /** 人群编码 */
            @NotBlank String segmentCode,
            /** 标签编码 */
            @NotBlank String tagCode,
            /** 操作符 */
            String operator,
            /** 目标值 */
            String targetValue,
            /** 规则归属 */
            String relation,
            /** 是否启用 */
            Boolean enabled
    ) {
    }

    public record SaveUserTagRequest(
            /** 用户ID */
            @NotNull @Positive Long userId,
            /** 标签编码 */
            @NotBlank String tagCode,
            /** 标签值 */
            @NotBlank String tagValue,
            /** 数据来源 */
            String source,
            /** 标签值更新时间 */
            LocalDateTime valueUpdatedAt
    ) {
    }
}
