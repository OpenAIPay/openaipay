package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.admin.web.request.ChangeCouponTemplateStatusRequest;
import cn.openaipay.adapter.admin.web.request.CreateCouponTemplateRequest;
import cn.openaipay.adapter.admin.web.request.IssueCouponRequest;
import cn.openaipay.adapter.admin.web.request.RedeemCouponRequest;
import cn.openaipay.adapter.admin.web.request.UpdateCouponTemplateRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.coupon.command.ChangeCouponTemplateStatusCommand;
import cn.openaipay.application.coupon.command.CreateCouponTemplateCommand;
import cn.openaipay.application.coupon.command.IssueCouponCommand;
import cn.openaipay.application.coupon.command.RedeemCouponCommand;
import cn.openaipay.application.coupon.command.UpdateCouponTemplateCommand;
import cn.openaipay.application.coupon.dto.CouponIssueDTO;
import cn.openaipay.application.coupon.dto.CouponOpsSummaryDTO;
import cn.openaipay.application.coupon.dto.CouponTemplateDTO;
import cn.openaipay.application.coupon.facade.CouponFacade;
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
 * 后台管理优惠券控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {

    /** CouponFacade组件 */
    private final CouponFacade couponFacade;

    public AdminCouponController(CouponFacade couponFacade) {
        this.couponFacade = couponFacade;
    }

    /**
     * 创建模板信息。
     */
    @PostMapping("/templates")
    @RequireAdminPermission("coupon.template.create")
    public ApiResponse<CouponTemplateDTO> createTemplate(@Valid @RequestBody CreateCouponTemplateRequest request) {
        CouponTemplateDTO result = couponFacade.createTemplate(new CreateCouponTemplateCommand(
                request.templateCode(),
                request.templateName(),
                request.sceneType(),
                request.valueType(),
                request.amount(),
                request.minAmount(),
                request.maxAmount(),
                request.thresholdAmount(),
                request.totalBudget(),
                request.totalStock(),
                request.perUserLimit(),
                request.claimStartTime(),
                request.claimEndTime(),
                request.useStartTime(),
                request.useEndTime(),
                request.fundingSource(),
                request.rulePayload(),
                request.initialStatus(),
                resolveOperator(request.operator())
        ));
        return ApiResponse.success(result);
    }

    /**
     * 更新模板信息。
     */
    @PutMapping("/templates/{templateId}")
    @RequireAdminPermission("coupon.template.update")
    public ApiResponse<CouponTemplateDTO> updateTemplate(@PathVariable("templateId") Long templateId,
                                                         @Valid @RequestBody UpdateCouponTemplateRequest request) {
        CouponTemplateDTO result = couponFacade.updateTemplate(new UpdateCouponTemplateCommand(
                templateId,
                request.templateName(),
                request.sceneType(),
                request.valueType(),
                request.amount(),
                request.minAmount(),
                request.maxAmount(),
                request.thresholdAmount(),
                request.totalBudget(),
                request.totalStock(),
                request.perUserLimit(),
                request.claimStartTime(),
                request.claimEndTime(),
                request.useStartTime(),
                request.useEndTime(),
                request.fundingSource(),
                request.rulePayload(),
                resolveOperator(request.operator())
        ));
        return ApiResponse.success(result);
    }

    /**
     * 处理模板状态。
     */
    @PutMapping("/templates/{templateId}/status")
    @RequireAdminPermission("coupon.template.status")
    public ApiResponse<CouponTemplateDTO> changeTemplateStatus(@PathVariable("templateId") Long templateId,
                                                               @Valid @RequestBody ChangeCouponTemplateStatusRequest request) {
        CouponTemplateDTO result = couponFacade.changeTemplateStatus(new ChangeCouponTemplateStatusCommand(
                templateId,
                request.status(),
                resolveOperator(request.operator())
        ));
        return ApiResponse.success(result);
    }

    /**
     * 获取模板信息。
     */
    @GetMapping("/templates/{templateId}")
    @RequireAdminPermission("coupon.template.view")
    public ApiResponse<CouponTemplateDTO> getTemplate(@PathVariable("templateId") Long templateId) {
        return ApiResponse.success(couponFacade.getTemplate(templateId));
    }

    /**
     * 查询模板信息列表。
     */
    @GetMapping("/templates")
    @RequireAdminPermission("coupon.template.list")
    public ApiResponse<List<CouponTemplateDTO>> listTemplates(
            @RequestParam(value = "sceneType", required = false) String sceneType,
            @RequestParam(value = "status", required = false) String status) {
        return ApiResponse.success(couponFacade.listTemplates(sceneType, status));
    }

    /**
     * 处理汇总信息。
     */
    @GetMapping("/summary")
    @RequireAdminPermission("coupon.template.list")
    public ApiResponse<CouponOpsSummaryDTO> summary() {
        return ApiResponse.success(couponFacade.queryOpsSummary());
    }

    /**
     * 处理优惠券信息。
     */
    @PostMapping("/issue")
    @RequireAdminPermission("coupon.issue.create")
    public ApiResponse<CouponIssueDTO> issueCoupon(@Valid @RequestBody IssueCouponRequest request) {
        CouponIssueDTO result = couponFacade.issueCoupon(new IssueCouponCommand(
                request.templateId(),
                request.userId(),
                request.claimChannel(),
                request.businessNo(),
                resolveOperator(request.operator())
        ));
        return ApiResponse.success(result);
    }

    /**
     * 处理优惠券信息。
     */
    @PostMapping("/redeem")
    @RequireAdminPermission("coupon.issue.redeem")
    public ApiResponse<CouponIssueDTO> redeemCoupon(@Valid @RequestBody RedeemCouponRequest request) {
        CouponIssueDTO result = couponFacade.redeemCoupon(new RedeemCouponCommand(
                request.couponNo(),
                request.orderNo()
        ));
        return ApiResponse.success(result);
    }

    /**
     * 查询用户优惠券信息列表。
     */
    @GetMapping("/users/{userId}")
    @RequireAdminPermission("coupon.issue.list_user")
    public ApiResponse<List<CouponIssueDTO>> listUserCoupons(@PathVariable("userId") Long userId) {
        return ApiResponse.success(couponFacade.listUserCoupons(userId));
    }

    /**
     * 获取业务数据。
     */
    @GetMapping("/issues/{couponNo}")
    @RequireAdminPermission("coupon.issue.list_user")
    public ApiResponse<CouponIssueDTO> getIssue(@PathVariable("couponNo") String couponNo) {
        return ApiResponse.success(couponFacade.getIssue(couponNo));
    }

    /**
     * 查询业务数据列表。
     */
    @GetMapping("/issues")
    @RequireAdminPermission("coupon.issue.list_user")
    public ApiResponse<List<CouponIssueDTO>> listIssues(
            @RequestParam(value = "templateId", required = false) Long templateId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(couponFacade.listIssues(templateId, userId, status, limit));
    }

    private String resolveOperator(String operator) {
        if (operator == null || operator.isBlank()) {
            return "admin";
        }
        return operator.trim();
    }
}
