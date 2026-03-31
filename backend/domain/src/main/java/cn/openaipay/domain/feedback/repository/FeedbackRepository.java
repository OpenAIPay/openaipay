package cn.openaipay.domain.feedback.repository;

import cn.openaipay.domain.feedback.model.FeedbackStatus;
import cn.openaipay.domain.feedback.model.FeedbackTicket;
import cn.openaipay.domain.feedback.model.FeedbackType;
import java.util.List;
import java.util.Optional;

/**
 * 反馈单仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
public interface FeedbackRepository {

    /**
     * 保存业务数据。
     */
    FeedbackTicket save(FeedbackTicket ticket);

    /**
     * 按反馈单号查找记录。
     */
    Optional<FeedbackTicket> findByFeedbackNo(String feedbackNo);

    /**
     * 按用户ID查询记录列表。
     */
    List<FeedbackTicket> listByUserId(Long userId, int limit);

    /**
     * 按条件查询记录列表。
     */
    List<FeedbackTicket> listByFilters(String feedbackNo, FeedbackStatus status, FeedbackType feedbackType, Long userId, int limit);
}
