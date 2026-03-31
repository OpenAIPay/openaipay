package cn.openaipay.infrastructure.conversation.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.conversation.dataobject.ConversationSessionDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface ConversationSessionMapper extends BaseMapper<ConversationSessionDO> {

    /**
     * 按会话单号查找记录。
     */
    default Optional<ConversationSessionDO> findByConversationNo(String conversationNo) {
        QueryWrapper<ConversationSessionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("conversation_no", conversationNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按业务KEY查找记录。
     */
    default Optional<ConversationSessionDO> findByBizKey(String bizKey) {
        QueryWrapper<ConversationSessionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("biz_key", bizKey);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按会话单号查找记录。
     */
    default List<ConversationSessionDO> findByConversationNos(Collection<String> conversationNos) {
        if (conversationNos == null || conversationNos.isEmpty()) {
            return List.of();
        }
        QueryWrapper<ConversationSessionDO> wrapper = new QueryWrapper<>();
        wrapper.in("conversation_no", conversationNos);
        return selectList(wrapper);
    }
}
