package cn.openaipay.application.feedback.facade.impl;

import cn.openaipay.application.feedback.command.HandleFeedbackCommand;
import cn.openaipay.application.feedback.command.SubmitFeedbackCommand;
import cn.openaipay.application.feedback.dto.FeedbackTicketDTO;
import cn.openaipay.application.feedback.facade.FeedbackFacade;
import cn.openaipay.application.feedback.service.FeedbackService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 反馈门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Service
public class FeedbackFacadeImpl implements FeedbackFacade {

    /** 反馈信息 */
    private final FeedbackService feedbackService;

    public FeedbackFacadeImpl(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /**
     * 提交业务数据。
     */
    @Override
    public FeedbackTicketDTO submit(SubmitFeedbackCommand command) {
        return feedbackService.submit(command);
    }

    /**
     * 获取工单信息。
     */
    @Override
    public FeedbackTicketDTO getTicket(String feedbackNo) {
        return feedbackService.getTicket(feedbackNo);
    }

    /**
     * 查询用户工单信息列表。
     */
    @Override
    public List<FeedbackTicketDTO> listUserTickets(Long userId, Integer limit) {
        return feedbackService.listUserTickets(userId, limit);
    }

    /**
     * 查询工单信息列表。
     */
    @Override
    public List<FeedbackTicketDTO> listTickets(String feedbackNo, String status, String feedbackType, Long userId, Integer limit) {
        return feedbackService.listTickets(feedbackNo, status, feedbackType, userId, limit);
    }

    /**
     * 处理业务数据。
     */
    @Override
    public FeedbackTicketDTO handle(HandleFeedbackCommand command) {
        return feedbackService.handle(command);
    }
}
