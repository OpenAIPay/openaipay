package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.AdminRequestContext;
import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.admin.web.request.HandleFeedbackTicketRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.feedback.command.HandleFeedbackCommand;
import cn.openaipay.application.feedback.dto.FeedbackTicketDTO;
import cn.openaipay.application.feedback.facade.FeedbackFacade;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台反馈单控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
@RestController
@RequestMapping("/api/admin/feedback")
public class AdminFeedbackController {

    /** 默认分页页码。 */
    private static final int DEFAULT_PAGE_NO = 1;
    /** 默认分页大小。 */
    private static final int DEFAULT_PAGE_SIZE = 20;
    /** 最大分页大小。 */
    private static final int MAX_PAGE_SIZE = 200;

    /** 反馈门面。 */
    private final FeedbackFacade feedbackFacade;
    /** 管理后台请求上下文。 */
    private final AdminRequestContext adminRequestContext;

    public AdminFeedbackController(FeedbackFacade feedbackFacade,
                                   AdminRequestContext adminRequestContext) {
        this.feedbackFacade = feedbackFacade;
        this.adminRequestContext = adminRequestContext;
    }

    /**
     * 查询工单信息列表。
     */
    @GetMapping("/tickets")
    @RequireAdminPermission("feedback.ticket.list")
    public ApiResponse<List<FeedbackTicketDTO>> listTickets(
            @RequestParam(value = "feedbackNo", required = false) String feedbackNo,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "feedbackType", required = false) String feedbackType,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        int normalizedPageNo = normalizePageNo(pageNo);
        int normalizedPageSize = normalizePageSize(pageSize == null ? limit : pageSize);
        List<FeedbackTicketDTO> rows = feedbackFacade.listTickets(
                feedbackNo,
                status,
                feedbackType,
                userId,
                resolveFetchLimit(normalizedPageNo, normalizedPageSize)
        );
        return ApiResponse.success(pageSlice(rows, normalizedPageNo, normalizedPageSize));
    }

    /**
     * 获取工单信息。
     */
    @GetMapping("/tickets/{feedbackNo}")
    @RequireAdminPermission("feedback.ticket.view")
    public ApiResponse<FeedbackTicketDTO> getTicket(@PathVariable("feedbackNo") String feedbackNo) {
        return ApiResponse.success(feedbackFacade.getTicket(feedbackNo));
    }

    /**
     * 处理工单信息。
     */
    @PutMapping("/tickets/{feedbackNo}/status")
    @RequireAdminPermission("feedback.ticket.handle")
    public ApiResponse<FeedbackTicketDTO> handleTicket(@PathVariable("feedbackNo") String feedbackNo,
                                                        @Valid @RequestBody HandleFeedbackTicketRequest request) {
        return ApiResponse.success(feedbackFacade.handle(new HandleFeedbackCommand(
                feedbackNo,
                request.status(),
                resolveOperator(),
                request.handleNote()
        )));
    }

    private String resolveOperator() {
        String username = adminRequestContext.currentAdminUsername();
        if (username != null && !username.isBlank()) {
            return username.trim();
        }
        return "admin#" + adminRequestContext.requiredAdminId();
    }

    private int normalizePageNo(Integer pageNo) {
        if (pageNo == null || pageNo <= 0) {
            return DEFAULT_PAGE_NO;
        }
        return pageNo;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private int resolveFetchLimit(int pageNo, int pageSize) {
        long fetchLimit = (long) pageNo * pageSize;
        return fetchLimit > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) fetchLimit;
    }

    private <T> List<T> pageSlice(List<T> rows, int pageNo, int pageSize) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        int offset = (pageNo - 1) * pageSize;
        if (offset >= rows.size()) {
            return List.of();
        }
        int endIndex = Math.min(rows.size(), offset + pageSize);
        return rows.subList(offset, endIndex);
    }
}
