package cn.openaipay.adapter.feedback.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.feedback.web.request.SubmitFeedbackRequest;
import cn.openaipay.application.feedback.command.SubmitFeedbackCommand;
import cn.openaipay.application.feedback.dto.FeedbackTicketDTO;
import cn.openaipay.application.feedback.facade.FeedbackFacade;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 反馈单控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    /** 反馈应用服务。 */
    private final FeedbackFacade feedbackFacade;

    /** 创建反馈控制器并注入反馈应用服务。 */
    public FeedbackController(FeedbackFacade feedbackFacade) {
        this.feedbackFacade = feedbackFacade;
    }

    /**
     * 提交业务数据。
     */
    @PostMapping("/tickets")
    public ApiResponse<FeedbackTicketDTO> submit(@Valid @RequestBody SubmitFeedbackRequest request) {
        return ApiResponse.success(feedbackFacade.submit(new SubmitFeedbackCommand(
                request.userId(),
                request.feedbackType(),
                request.sourceChannel(),
                request.sourcePageCode(),
                request.title(),
                request.content(),
                request.contactMobile(),
                request.attachmentUrls()
        )));
    }

    /**
     * 获取工单信息。
     */
    @GetMapping("/tickets/{feedbackNo}")
    public ApiResponse<FeedbackTicketDTO> getTicket(@PathVariable("feedbackNo") String feedbackNo) {
        return ApiResponse.success(feedbackFacade.getTicket(feedbackNo));
    }

    /**
     * 查询用户工单信息列表。
     */
    @GetMapping("/users/{userId}/tickets")
    public ApiResponse<List<FeedbackTicketDTO>> listUserTickets(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(feedbackFacade.listUserTickets(userId, limit));
    }
}
