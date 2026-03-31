package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.AdminRequestContext;
import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.admin.web.request.ChangePricingRuleStatusRequest;
import cn.openaipay.adapter.admin.web.request.CreatePricingRuleRequest;
import cn.openaipay.adapter.admin.web.request.UpdatePricingRuleRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.pricing.command.ChangePricingRuleStatusCommand;
import cn.openaipay.application.pricing.command.CreatePricingRuleCommand;
import cn.openaipay.application.pricing.command.UpdatePricingRuleCommand;
import cn.openaipay.application.pricing.dto.PricingRuleDTO;
import cn.openaipay.application.pricing.facade.PricingRuleFacade;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台管理Pricing规则控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/admin/pricing-rules")
public class AdminPricingRuleController {

    /** PricingRuleFacade组件 */
    private final PricingRuleFacade pricingRuleFacade;
    /** 管理请求上下文 */
    private final AdminRequestContext adminRequestContext;

    public AdminPricingRuleController(PricingRuleFacade pricingRuleFacade,
                                      AdminRequestContext adminRequestContext) {
        this.pricingRuleFacade = pricingRuleFacade;
        this.adminRequestContext = adminRequestContext;
    }

    /**
     * 创建规则。
     */
    @PostMapping
    @RequireAdminPermission("pricing.rule.create")
    public ApiResponse<PricingRuleDTO> createRule(@Valid @RequestBody CreatePricingRuleRequest request) {
        PricingRuleDTO result = pricingRuleFacade.createRule(new CreatePricingRuleCommand(
                request.ruleCode(),
                request.ruleName(),
                request.businessSceneCode(),
                request.paymentMethod(),
                request.currencyCode(),
                request.feeMode(),
                request.feeRate(),
                request.fixedFee(),
                request.minFee(),
                request.maxFee(),
                request.feeBearer(),
                request.priority(),
                request.validFrom(),
                request.validTo(),
                request.rulePayload(),
                request.initialStatus(),
                resolveOperator(request.operator())
        ));
        return ApiResponse.success(result);
    }

    /**
     * 更新规则。
     */
    @PutMapping("/{ruleId}")
    @RequireAdminPermission("pricing.rule.update")
    public ApiResponse<PricingRuleDTO> updateRule(@PathVariable("ruleId") Long ruleId,
                                                  @Valid @RequestBody UpdatePricingRuleRequest request) {
        PricingRuleDTO result = pricingRuleFacade.updateRule(new UpdatePricingRuleCommand(
                ruleId,
                request.ruleName(),
                request.businessSceneCode(),
                request.paymentMethod(),
                request.currencyCode(),
                request.feeMode(),
                request.feeRate(),
                request.fixedFee(),
                request.minFee(),
                request.maxFee(),
                request.feeBearer(),
                request.priority(),
                request.validFrom(),
                request.validTo(),
                request.rulePayload(),
                resolveOperator(request.operator())
        ));
        return ApiResponse.success(result);
    }

    /**
     * 处理规则状态。
     */
    @PutMapping("/{ruleId}/status")
    @RequireAdminPermission("pricing.rule.status")
    public ApiResponse<PricingRuleDTO> changeRuleStatus(@PathVariable("ruleId") Long ruleId,
                                                        @Valid @RequestBody ChangePricingRuleStatusRequest request) {
        PricingRuleDTO result = pricingRuleFacade.changeRuleStatus(new ChangePricingRuleStatusCommand(
                ruleId,
                request.status(),
                resolveOperator(request.operator())
        ));
        return ApiResponse.success(result);
    }

    /**
     * 获取规则。
     */
    @GetMapping("/{ruleId}")
    @RequireAdminPermission("pricing.rule.view")
    public ApiResponse<PricingRuleDTO> getRule(@PathVariable("ruleId") Long ruleId) {
        return ApiResponse.success(pricingRuleFacade.getRule(ruleId));
    }

    /**
     * 查询规则列表。
     */
    @GetMapping
    @RequireAdminPermission("pricing.rule.list")
    public ApiResponse<List<PricingRuleDTO>> listRules(
            @RequestParam(value = "businessSceneCode", required = false) String businessSceneCode,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            @RequestParam(value = "status", required = false) String status) {
        return ApiResponse.success(pricingRuleFacade.listRules(businessSceneCode, paymentMethod, status));
    }

    private String resolveOperator(String requestOperator) {
        if (requestOperator != null && !requestOperator.isBlank()) {
            return requestOperator.trim();
        }
        String currentUsername = adminRequestContext.currentAdminUsername();
        if (currentUsername != null && !currentUsername.isBlank()) {
            return currentUsername;
        }
        return "system";
    }
}
