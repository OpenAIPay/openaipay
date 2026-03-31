package cn.openaipay.infrastructure.feedback.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.feedback.dataobject.FeedbackTicketDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 反馈单持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
@Mapper
public interface FeedbackTicketMapper extends BaseMapper<FeedbackTicketDO> {

    /**
     * 按反馈单号查找记录。
     */
    default Optional<FeedbackTicketDO> findByFeedbackNo(String feedbackNo) {
        QueryWrapper<FeedbackTicketDO> wrapper = new QueryWrapper<>();
        wrapper.eq("feedback_no", feedbackNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按用户ID查找记录。
     */
    default List<FeedbackTicketDO> findByUserId(Long userId, int limit) {
        QueryWrapper<FeedbackTicketDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("created_at");
        wrapper.last("LIMIT " + Math.max(limit, 1));
        return selectList(wrapper);
    }

    /**
     * 按条件查找记录。
     */
    default List<FeedbackTicketDO> findByFilters(String feedbackNo, String status, String feedbackType, Long userId, int limit) {
        QueryWrapper<FeedbackTicketDO> wrapper = new QueryWrapper<>();
        if (feedbackNo != null && !feedbackNo.isBlank()) {
            wrapper.eq("feedback_no", feedbackNo.trim());
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq("status", status.trim());
        }
        if (feedbackType != null && !feedbackType.isBlank()) {
            wrapper.eq("feedback_type", feedbackType.trim());
        }
        if (userId != null && userId > 0) {
            wrapper.eq("user_id", userId);
        }
        wrapper.orderByDesc("created_at");
        wrapper.last("LIMIT " + Math.max(limit, 1));
        return selectList(wrapper);
    }
}
