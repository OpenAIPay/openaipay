package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.AdminRequestContext;
import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.admin.web.request.RequeueOutboxDeadLetterRequest;
import cn.openaipay.adapter.admin.web.request.RequeueOutboxDeadLettersRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.outbox.dto.OutboxMessageDTO;
import cn.openaipay.application.outbox.dto.OutboxOverviewDTO;
import cn.openaipay.application.outbox.dto.OutboxTopicDistributionDTO;
import cn.openaipay.application.outbox.facade.OutboxMonitorFacade;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台消息投递中心控制器（Outbox）。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@RestController
@RequestMapping("/api/admin/outbox")
public class AdminOutboxController {

    /** 默认limit信息 */
    private static final int DEFAULT_LIMIT = 20;
    /** 最大limit信息 */
    private static final int MAX_LIMIT = 200;

    /** 消息投递中心门面（Outbox） */
    private final OutboxMonitorFacade outboxMonitorFacade;
    /** 后台请求上下文。 */
    private final AdminRequestContext adminRequestContext;

    public AdminOutboxController(OutboxMonitorFacade outboxMonitorFacade,
                                 AdminRequestContext adminRequestContext) {
        this.outboxMonitorFacade = outboxMonitorFacade;
        this.adminRequestContext = adminRequestContext;
    }

    /**
     * 处理概览信息。
     */
    @GetMapping("/overview")
    @RequireAdminPermission("outbox.monitor.view")
    public ApiResponse<OutboxOverviewDTO> overview() {
        return ApiResponse.success(outboxMonitorFacade.getOverview());
    }

    /**
     * 处理主题信息。
     */
    @GetMapping("/topics")
    @RequireAdminPermission("outbox.monitor.view")
    public ApiResponse<List<OutboxTopicDistributionDTO>> topicDistribution(
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(outboxMonitorFacade.listTopicDistribution(normalizeLimit(limit)));
    }

    /**
     * 查询消息信息列表。
     */
    @GetMapping("/messages")
    @RequireAdminPermission("outbox.monitor.view")
    public ApiResponse<List<OutboxMessageDTO>> listMessages(
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "onlyRetried", required = false) Boolean onlyRetried,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "includePayload", required = false) Boolean includePayload) {
        return ApiResponse.success(outboxMonitorFacade.listMessages(
                topic,
                status,
                keyword,
                onlyRetried,
                normalizeLimit(limit),
                includePayload
        ));
    }

    /**
     * 查询死信列表。
     */
    @GetMapping("/dead-letters")
    @RequireAdminPermission("outbox.monitor.view")
    public ApiResponse<List<OutboxMessageDTO>> deadLetters(
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "includePayload", required = false) Boolean includePayload) {
        return ApiResponse.success(outboxMonitorFacade.listDeadLetters(
                topic,
                keyword,
                normalizeLimit(limit),
                includePayload
        ));
    }

    /**
     * 获取消息信息。
     */
    @GetMapping("/messages/{id}")
    @RequireAdminPermission("outbox.monitor.view")
    public ApiResponse<OutboxMessageDTO> getMessage(@PathVariable("id") Long id) {
        return ApiResponse.success(outboxMonitorFacade.getMessage(id));
    }

    /**
     * 重放单条死信。
     */
    @PostMapping("/dead-letters/{id}/requeue")
    @RequireAdminPermission("outbox.monitor.requeue")
    public ApiResponse<OutboxRequeueResultResponse> requeueDeadLetter(@PathVariable("id") Long id,
                                                                       @RequestBody(required = false) RequeueOutboxDeadLetterRequest request) {
        LocalDateTime nextRetryAt = request == null ? null : request.nextRetryAt();
        boolean requeued = outboxMonitorFacade.requeueDeadLetter(id, nextRetryAt, resolveOperator());
        return ApiResponse.success(new OutboxRequeueResultResponse(
                requeued ? 1 : 0,
                requeued ? 1 : 0,
                null,
                nextRetryAt
        ));
    }

    /**
     * 批量重放死信。
     */
    @PostMapping("/dead-letters/requeue")
    @RequireAdminPermission("outbox.monitor.requeue")
    public ApiResponse<OutboxRequeueResultResponse> requeueDeadLetters(
            @Valid @RequestBody(required = false) RequeueOutboxDeadLettersRequest request) {
        String topic = request == null ? null : normalizeTopic(request.topic());
        Integer limit = request == null ? null : request.limit();
        LocalDateTime nextRetryAt = request == null ? null : request.nextRetryAt();
        int normalizedLimit = limit == null || limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        int requeuedCount = outboxMonitorFacade.requeueDeadLetters(topic, normalizedLimit, nextRetryAt, resolveOperator());
        return ApiResponse.success(new OutboxRequeueResultResponse(
                normalizedLimit,
                requeuedCount,
                topic,
                nextRetryAt
        ));
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String normalizeTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            return null;
        }
        return topic.trim();
    }

    private String resolveOperator() {
        String username = adminRequestContext.currentAdminUsername();
        if (username != null && !username.isBlank()) {
            return username.trim();
        }
        Long adminId = adminRequestContext.currentAdminId();
        if (adminId != null && adminId > 0) {
            return "admin#" + adminId;
        }
        return "system";
    }

    /**
     * 死信重放结果响应。
     */
    public record OutboxRequeueResultResponse(
            /** 请求重放上限。 */
            int requestedLimit,
            /** 实际重放数量。 */
            int requeuedCount,
            /** 主题。 */
            String topic,
            /** 下次重试时间。 */
            LocalDateTime nextRetryAt
    ) {
    }
}
