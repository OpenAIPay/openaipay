package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.AdminRequestContext;
import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.admin.web.request.ReverseAccountingVoucherRequest;
import cn.openaipay.adapter.admin.web.request.SaveAccountingSubjectRequest;
import cn.openaipay.adapter.admin.web.request.UpdateAccountingSubjectStatusRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.accounting.command.ReverseAccountingVoucherCommand;
import cn.openaipay.application.accounting.command.SaveAccountingSubjectCommand;
import cn.openaipay.application.accounting.dto.AccountingEntryDTO;
import cn.openaipay.application.accounting.dto.AccountingEventDTO;
import cn.openaipay.application.accounting.dto.AccountingSubjectDTO;
import cn.openaipay.application.accounting.dto.AccountingSubjectSyncResultDTO;
import cn.openaipay.application.accounting.dto.AccountingVoucherDTO;
import cn.openaipay.application.accounting.facade.AccountingFacade;
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
 * 后台会计中心控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@RestController
@RequestMapping("/api/admin/accounting")
public class AdminAccountingController {

    /** 核算门面信息 */
    private final AccountingFacade accountingFacade;
    /** 后台请求信息 */
    private final AdminRequestContext adminRequestContext;

    public AdminAccountingController(AccountingFacade accountingFacade,
                                     AdminRequestContext adminRequestContext) {
        this.accountingFacade = accountingFacade;
        this.adminRequestContext = adminRequestContext;
    }

    /**
     * 查询事件信息列表。
     */
    @GetMapping("/events")
    @RequireAdminPermission("admin.accounting.event.list")
    public ApiResponse<List<AccountingEventDTO>> listEvents(
            @RequestParam(value = "eventId", required = false) String eventId,
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "sourceBizType", required = false) String sourceBizType,
            @RequestParam(value = "sourceBizNo", required = false) String sourceBizNo,
            @RequestParam(value = "bizOrderNo", required = false) String bizOrderNo,
            @RequestParam(value = "tradeOrderNo", required = false) String tradeOrderNo,
            @RequestParam(value = "payOrderNo", required = false) String payOrderNo,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(accountingFacade.listEvents(
                eventId,
                eventType,
                sourceBizType,
                sourceBizNo,
                bizOrderNo,
                tradeOrderNo,
                payOrderNo,
                status,
                limit
        ));
    }

    /**
     * 获取事件信息。
     */
    @GetMapping("/events/{eventId}")
    @RequireAdminPermission("admin.accounting.event.detail")
    public ApiResponse<AccountingEventDTO> getEvent(@PathVariable("eventId") String eventId) {
        return ApiResponse.success(accountingFacade.getEvent(eventId));
    }

    /**
     * 重试事件信息。
     */
    @PostMapping("/events/{eventId}/retry")
    @RequireAdminPermission("admin.accounting.event.retry")
    public ApiResponse<AccountingVoucherDTO> retryEvent(@PathVariable("eventId") String eventId) {
        return ApiResponse.success(accountingFacade.retryEvent(eventId));
    }

    /**
     * 查询凭证信息列表。
     */
    @GetMapping("/vouchers")
    @RequireAdminPermission("admin.accounting.voucher.list")
    public ApiResponse<List<AccountingVoucherDTO>> listVouchers(
            @RequestParam(value = "voucherNo", required = false) String voucherNo,
            @RequestParam(value = "sourceBizType", required = false) String sourceBizType,
            @RequestParam(value = "sourceBizNo", required = false) String sourceBizNo,
            @RequestParam(value = "bizOrderNo", required = false) String bizOrderNo,
            @RequestParam(value = "tradeOrderNo", required = false) String tradeOrderNo,
            @RequestParam(value = "payOrderNo", required = false) String payOrderNo,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(accountingFacade.listVouchers(
                voucherNo,
                sourceBizType,
                sourceBizNo,
                bizOrderNo,
                tradeOrderNo,
                payOrderNo,
                status,
                limit
        ));
    }

    /**
     * 获取凭证信息。
     */
    @GetMapping("/vouchers/{voucherNo}")
    @RequireAdminPermission("admin.accounting.voucher.detail")
    public ApiResponse<AccountingVoucherDTO> getVoucher(@PathVariable("voucherNo") String voucherNo) {
        return ApiResponse.success(accountingFacade.getVoucher(voucherNo));
    }

    /**
     * 查询凭证信息列表。
     */
    @GetMapping("/vouchers/{voucherNo}/entries")
    @RequireAdminPermission("admin.accounting.entry.list")
    public ApiResponse<List<AccountingEntryDTO>> listVoucherEntries(@PathVariable("voucherNo") String voucherNo) {
        return ApiResponse.success(accountingFacade.listEntries(
                voucherNo,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                500
        ));
    }

    /**
     * 处理凭证信息。
     */
    @PostMapping("/vouchers/{voucherNo}/reverse")
    @RequireAdminPermission("admin.accounting.voucher.reverse")
    public ApiResponse<AccountingVoucherDTO> reverseVoucher(@PathVariable("voucherNo") String voucherNo,
                                                            @Valid @RequestBody ReverseAccountingVoucherRequest request) {
        return ApiResponse.success(accountingFacade.reverseVoucher(new ReverseAccountingVoucherCommand(
                voucherNo,
                request.reverseReason(),
                resolveOperator()
        )));
    }

    /**
     * 查询业务数据列表。
     */
    @GetMapping("/entries")
    @RequireAdminPermission("admin.accounting.entry.list")
    public ApiResponse<List<AccountingEntryDTO>> listEntries(
            @RequestParam(value = "voucherNo", required = false) String voucherNo,
            @RequestParam(value = "subjectCode", required = false) String subjectCode,
            @RequestParam(value = "ownerType", required = false) String ownerType,
            @RequestParam(value = "ownerId", required = false) Long ownerId,
            @RequestParam(value = "bizOrderNo", required = false) String bizOrderNo,
            @RequestParam(value = "tradeOrderNo", required = false) String tradeOrderNo,
            @RequestParam(value = "payOrderNo", required = false) String payOrderNo,
            @RequestParam(value = "sourceBizType", required = false) String sourceBizType,
            @RequestParam(value = "sourceBizNo", required = false) String sourceBizNo,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(accountingFacade.listEntries(
                voucherNo,
                subjectCode,
                ownerType,
                ownerId,
                bizOrderNo,
                tradeOrderNo,
                payOrderNo,
                sourceBizType,
                sourceBizNo,
                limit
        ));
    }

    /**
     * 查询科目信息列表。
     */
    @GetMapping("/subjects")
    @RequireAdminPermission("admin.accounting.subject.list")
    public ApiResponse<List<AccountingSubjectDTO>> listSubjects(
            @RequestParam(value = "enabled", required = false) Boolean enabled,
            @RequestParam(value = "subjectType", required = false) String subjectType,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(accountingFacade.listSubjects(enabled, subjectType, limit));
    }

    /**
     * 保存科目信息。
     */
    @PutMapping("/subjects/{subjectCode}")
    @RequireAdminPermission("admin.accounting.subject.save")
    public ApiResponse<AccountingSubjectDTO> saveSubject(@PathVariable("subjectCode") String subjectCode,
                                                         @Valid @RequestBody SaveAccountingSubjectRequest request) {
        return ApiResponse.success(accountingFacade.saveSubject(new SaveAccountingSubjectCommand(
                subjectCode,
                request.subjectName(),
                request.subjectType(),
                request.balanceDirection(),
                request.parentSubjectCode(),
                request.levelNo(),
                request.enabled(),
                request.remark()
        )));
    }

    /**
     * 更新科目状态。
     */
    @PostMapping("/subjects/{subjectCode}/status")
    @RequireAdminPermission("admin.accounting.subject.save")
    public ApiResponse<AccountingSubjectDTO> updateSubjectStatus(@PathVariable("subjectCode") String subjectCode,
                                                                 @Valid @RequestBody UpdateAccountingSubjectStatusRequest request) {
        return ApiResponse.success(accountingFacade.updateSubjectStatus(subjectCode, request.enabled()));
    }

    /**
     * 初始化科目信息。
     */
    @PostMapping("/subjects/initialize-standard")
    @RequireAdminPermission("admin.accounting.subject.save")
    public ApiResponse<AccountingSubjectSyncResultDTO> initializeStandardSubjects() {
        return ApiResponse.success(accountingFacade.initializeStandardSubjects());
    }

    /**
     * 重置科目信息。
     */
    @PostMapping("/subjects/reset-standard")
    @RequireAdminPermission("admin.accounting.subject.save")
    public ApiResponse<AccountingSubjectSyncResultDTO> resetStandardSubjects() {
        return ApiResponse.success(accountingFacade.resetStandardSubjects());
    }

    private String resolveOperator() {
        String username = adminRequestContext.currentAdminUsername();
        if (username != null && !username.isBlank()) {
            return username.trim();
        }
        return "admin#" + adminRequestContext.requiredAdminId();
    }
}
