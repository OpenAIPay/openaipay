package cn.openaipay.infrastructure.asyncmessage.mapper;

import cn.openaipay.infrastructure.asyncmessage.dataobject.AsyncMessageDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 系统内可靠异步消息持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Mapper
public interface AsyncMessageMapper extends BaseMapper<AsyncMessageDO> {

    /**
     * 按主题与消息KEY查找记录。
     */
    default Optional<AsyncMessageDO> findByTopicAndMessageKey(String topic, String messageKey) {
        QueryWrapper<AsyncMessageDO> wrapper = new QueryWrapper<>();
        wrapper.eq("topic", topic)
                .eq("message_key", messageKey)
                .last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
