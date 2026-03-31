package cn.openaipay.application.feedback.facade;

import cn.openaipay.application.feedback.command.HandleFeedbackCommand;
import cn.openaipay.application.feedback.command.SubmitFeedbackCommand;
import cn.openaipay.application.feedback.dto.FeedbackTicketDTO;
import java.util.List;

/**
 * 反馈门面接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
public interface FeedbackFacade {

    /**
     * 提交业务数据。
     */
    FeedbackTicketDTO submit(SubmitFeedbackCommand command);

    /**
     * 获取工单信息。
     */
    FeedbackTicketDTO getTicket(String feedbackNo);

    /**
     * 查询用户工单信息列表。
     */
    List<FeedbackTicketDTO> listUserTickets(Long userId, Integer limit);

    /**
     * 查询工单信息列表。
     */
    List<FeedbackTicketDTO> listTickets(String feedbackNo, String status, String feedbackType, Long userId, Integer limit);

    /**
     * 处理业务数据。
     */
    FeedbackTicketDTO handle(HandleFeedbackCommand command);
}
