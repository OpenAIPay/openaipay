package cn.openaipay.infrastructure.conversation.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.conversation.dataobject.ConversationMemberDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话成员持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface ConversationMemberMapper extends BaseMapper<ConversationMemberDO> {

    /**
     * 按会话与用户查找记录。
     */
    default Optional<ConversationMemberDO> findByConversationAndUser(String conversationNo, Long userId) {
        QueryWrapper<ConversationMemberDO> wrapper = new QueryWrapper<>();
        wrapper.eq("conversation_no", conversationNo);
        wrapper.eq("user_id", userId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按用户ID查询记录列表。
     */
    default List<ConversationMemberDO> listByUserId(Long userId, int limit) {
        QueryWrapper<ConversationMemberDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("updated_at");
        wrapper.last("LIMIT " + Math.max(1, limit));
        return selectList(wrapper);
    }
}
