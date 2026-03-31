package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.adminrisk.dto.AdminRiskBlacklistRowDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskOverviewDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskRuleDTO;
import cn.openaipay.application.adminrisk.dto.AdminRiskUserRowDTO;
import cn.openaipay.application.adminrisk.facade.AdminRiskManageFacade;
import cn.openaipay.domain.riskpolicy.model.RiskDecision;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台风控中心控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@RestController
@RequestMapping("/api/admin/risk")
public class AdminRiskController {

    /** 风控管理门面。 */
    private final AdminRiskManageFacade adminRiskManageFacade;

    public AdminRiskController(AdminRiskManageFacade adminRiskManageFacade) {
        this.adminRiskManageFacade = adminRiskManageFacade;
    }

    /**
     * 处理概览信息。
     */
    @GetMapping("/overview")
    @RequireAdminPermission("risk.center.view")
    public ApiResponse<OverviewResponse> overview() {
        return ApiResponse.success(toOverviewResponse(adminRiskManageFacade.overview()));
    }

    /**
     * 查询用户信息列表。
     */
    @GetMapping("/users")
    @RequireAdminPermission("risk.center.view")
    public ApiResponse<List<RiskUserRow>> listUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "kycLevel", required = false) String kycLevel,
            @RequestParam(value = "riskLevel", required = false) String riskLevel,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(adminRiskManageFacade.listUsers(keyword, kycLevel, riskLevel, pageNo, resolvedPageSize).stream()
                .map(this::toRiskUserRow)
                .toList());
    }

    /**
     * 查询黑名单信息列表。
     */
    @GetMapping("/blacklist")
    @RequireAdminPermission("risk.center.view")
    public ApiResponse<List<RiskBlacklistRow>> listBlacklists(
            @RequestParam(value = "ownerUserId", required = false) Long ownerUserId,
            @RequestParam(value = "blockedUserId", required = false) Long blockedUserId,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(adminRiskManageFacade.listBlacklists(ownerUserId, blockedUserId, pageNo, resolvedPageSize).stream()
                .map(this::toRiskBlacklistRow)
                .toList());
    }

    /**
     * 更新风控资料信息。
     */
    @PutMapping("/users/{userId}/risk-profile")
    @RequireAdminPermission("risk.user.manage")
    public ApiResponse<RiskUserRow> updateRiskProfile(@PathVariable("userId") Long userId,
                                                      @Valid @RequestBody UpdateRiskProfileRequest request) {
        return ApiResponse.success(toRiskUserRow(adminRiskManageFacade.updateRiskProfile(
                userId,
                request.kycLevel(),
                request.riskLevel(),
                request.twoFactorMode(),
                request.deviceLockEnabled(),
                request.privacyModeEnabled()
        )));
    }

    /**
     * 查询规则列表。
     */
    @GetMapping("/rules")
    @RequireAdminPermission("risk.rule.view")
    public ApiResponse<List<RiskRuleRow>> listRules(
            @RequestParam(value = "sceneCode", required = false) String sceneCode,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(adminRiskManageFacade.listRules(sceneCode, status, pageNo, resolvedPageSize).stream()
                .map(this::toRiskRuleRow)
                .toList());
    }

    /**
     * 保存规则。
     */
    @PostMapping("/rules")
    @RequireAdminPermission("risk.rule.manage")
    public ApiResponse<RiskRuleRow> saveRule(@Valid @RequestBody SaveRiskRuleRequest request) {
        return ApiResponse.success(toRiskRuleRow(adminRiskManageFacade.saveRule(new AdminRiskRuleDTO(
                request.ruleCode(),
                request.sceneCode(),
                request.ruleType(),
                request.scopeType(),
                request.scopeValue(),
                request.thresholdAmount(),
                request.currencyCode(),
                request.priority(),
                request.status(),
                request.ruleDesc(),
                request.operator(),
                null,
                null
        ))));
    }

    /**
     * 变更规则状态。
     */
    @PutMapping("/rules/{ruleCode}/status")
    @RequireAdminPermission("risk.rule.manage.put")
    public ApiResponse<RiskRuleRow> changeRuleStatus(@PathVariable("ruleCode") String ruleCode,
                                                     @Valid @RequestBody ChangeRuleStatusRequest request) {
        return ApiResponse.success(toRiskRuleRow(adminRiskManageFacade.changeRuleStatus(
                ruleCode,
                request.status(),
                request.operator()
        )));
    }

    /**
     * 预览风控决策（仅后台调试）。
     */
    @PostMapping("/rules/evaluate")
    @RequireAdminPermission("risk.rule.view")
    public ApiResponse<RiskDecisionResponse> evaluate(@Valid @RequestBody EvaluateRiskRequest request) {
        RiskDecision decision = adminRiskManageFacade.evaluateTradeRisk(
                request.sceneCode(),
                request.userId(),
                request.amount(),
                request.currencyCode()
        );
        return ApiResponse.success(new RiskDecisionResponse(decision.passed(), decision.code(), decision.message()));
    }

    private OverviewResponse toOverviewResponse(AdminRiskOverviewDTO dto) {
        return new OverviewResponse(
                dto.totalUserCount(),
                dto.l0Count(),
                dto.l1Count(),
                dto.l2Count(),
                dto.l3Count(),
                dto.lowRiskCount(),
                dto.mediumRiskCount(),
                dto.highRiskCount(),
                dto.blacklistCount()
        );
    }

    private RiskUserRow toRiskUserRow(AdminRiskUserRowDTO dto) {
        if (dto == null) {
            return null;
        }
        return new RiskUserRow(
                dto.userId(),
                dto.displayName(),
                dto.aipayUid(),
                dto.loginId(),
                dto.mobile(),
                dto.accountStatus(),
                dto.kycLevel(),
                dto.riskLevel(),
                dto.twoFactorMode(),
                dto.deviceLockEnabled(),
                dto.privacyModeEnabled(),
                dto.allowSearchByMobile(),
                dto.allowSearchByAipayUid(),
                dto.hideRealName(),
                dto.personalizedRecommendationEnabled(),
                dto.updatedAt()
        );
    }

    private RiskBlacklistRow toRiskBlacklistRow(AdminRiskBlacklistRowDTO dto) {
        return new RiskBlacklistRow(
                dto.ownerUserId(),
                dto.ownerDisplayName(),
                dto.ownerAipayUid(),
                dto.blockedUserId(),
                dto.blockedDisplayName(),
                dto.blockedAipayUid(),
                dto.reason(),
                dto.createdAt(),
                dto.updatedAt()
        );
    }

    private RiskRuleRow toRiskRuleRow(AdminRiskRuleDTO dto) {
        if (dto == null) {
            return null;
        }
        return new RiskRuleRow(
                dto.ruleCode(),
                dto.sceneCode(),
                dto.ruleType(),
                dto.scopeType(),
                dto.scopeValue(),
                dto.thresholdAmount(),
                dto.currencyCode(),
                dto.priority(),
                dto.status(),
                dto.ruleDesc(),
                dto.updatedBy(),
                dto.createdAt(),
                dto.updatedAt()
        );
    }

    public record OverviewResponse(
            /** 总用户数量 */
            long totalUserCount,
            /** L0数量 */
            long l0Count,
            /** L1数量 */
            long l1Count,
            /** L2数量 */
            long l2Count,
            /** L3数量 */
            long l3Count,
            /** LOW风控数量 */
            long lowRiskCount,
            /** medium风控数量 */
            long mediumRiskCount,
            /** high风控数量 */
            long highRiskCount,
            /** 黑名单数量 */
            long blacklistCount
    ) {
    }

    /** 风控用户行。 */
    public record RiskUserRow(
            /** 用户ID */
            Long userId,
            /** 展示名称 */
            String displayName,
            /** 爱支付UID */
            String aipayUid,
            /** 登录账号ID */
            String loginId,
            /** 手机号 */
            String mobile,
            /** account状态 */
            String accountStatus,
            /** KYClevel信息 */
            String kycLevel,
            /** 风控level信息 */
            String riskLevel,
            /** TWOfactormode信息 */
            String twoFactorMode,
            /** 设备lock启用信息 */
            Boolean deviceLockEnabled,
            /** 隐私mode启用信息 */
            Boolean privacyModeEnabled,
            /** allowsearchBY手机号 */
            Boolean allowSearchByMobile,
            /** allowsearchBY爱支付UID */
            Boolean allowSearchByAipayUid,
            /** hidereal名称 */
            Boolean hideRealName,
            /** personalizedrecommendation启用信息 */
            Boolean personalizedRecommendationEnabled,
            /** 记录更新时间 */
            LocalDateTime updatedAt
    ) {
    }

    /** 黑名单行。 */
    public record RiskBlacklistRow(
            /** 所属用户ID */
            Long ownerUserId,
            /** 所属展示名称 */
            String ownerDisplayName,
            /** 所属爱支付UID */
            String ownerAipayUid,
            /** blocked用户ID */
            Long blockedUserId,
            /** blocked展示名称 */
            String blockedDisplayName,
            /** blocked爱支付UID */
            String blockedAipayUid,
            /** 业务原因 */
            String reason,
            /** 记录创建时间 */
            LocalDateTime createdAt,
            /** 记录更新时间 */
            LocalDateTime updatedAt
    ) {
    }

    public record RiskRuleRow(
            /** 规则编码 */
            String ruleCode,
            /** 场景编码 */
            String sceneCode,
            /** 规则类型 */
            String ruleType,
            /** 作用域类型 */
            String scopeType,
            /** 作用域值 */
            String scopeValue,
            /** 阈值金额 */
            BigDecimal thresholdAmount,
            /** 币种编码 */
            String currencyCode,
            /** 优先级 */
            Integer priority,
            /** 状态 */
            String status,
            /** 描述 */
            String ruleDesc,
            /** 更新人 */
            String updatedBy,
            /** 创建时间 */
            LocalDateTime createdAt,
            /** 更新时间 */
            LocalDateTime updatedAt
    ) {
    }

    public record RiskDecisionResponse(
            /** 是否通过 */
            boolean passed,
            /** 决策编码 */
            String code,
            /** 决策文案 */
            String message
    ) {
    }

    public record UpdateRiskProfileRequest(
            /** KYClevel信息 */
            @Pattern(regexp = "^(L[0-3])?$", message = "kycLevel格式不正确") String kycLevel,
            /** 风控level信息 */
            @Pattern(regexp = "^(LOW|MEDIUM|HIGH)?$", message = "riskLevel格式不正确") String riskLevel,
            /** TWOfactormode信息 */
            @Pattern(regexp = "^(NONE|SMS|APP|BIOMETRIC)?$", message = "twoFactorMode格式不正确") String twoFactorMode,
            /** 设备lock启用信息 */
            Boolean deviceLockEnabled,
            /** 隐私mode启用信息 */
            Boolean privacyModeEnabled
    ) {
    }

    public record SaveRiskRuleRequest(
            /** 规则编码 */
            @NotBlank(message = "ruleCode不能为空") String ruleCode,
            /** 场景编码 */
            @NotBlank(message = "sceneCode不能为空") String sceneCode,
            /** 规则类型 */
            @Pattern(regexp = "^(SINGLE_LIMIT|DAILY_LIMIT|USER_BLOCK)$", message = "ruleType格式不正确") String ruleType,
            /** 作用域类型 */
            @Pattern(regexp = "^(GLOBAL|USER)$", message = "scopeType格式不正确") String scopeType,
            /** 作用域值 */
            String scopeValue,
            /** 阈值金额 */
            BigDecimal thresholdAmount,
            /** 币种编码 */
            String currencyCode,
            /** 优先级 */
            Integer priority,
            /** 状态 */
            @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "status格式不正确") String status,
            /** 描述 */
            String ruleDesc,
            /** 操作人 */
            String operator
    ) {
    }

    public record ChangeRuleStatusRequest(
            /** 状态 */
            @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "status格式不正确") String status,
            /** 操作人 */
            String operator
    ) {
    }

    public record EvaluateRiskRequest(
            /** 场景编码 */
            @NotBlank(message = "sceneCode不能为空") String sceneCode,
            /** 用户ID */
            Long userId,
            /** 金额 */
            BigDecimal amount,
            /** 币种 */
            String currencyCode
    ) {
    }
}
