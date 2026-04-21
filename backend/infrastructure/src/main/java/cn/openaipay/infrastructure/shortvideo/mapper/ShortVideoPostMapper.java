package cn.openaipay.infrastructure.shortvideo.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.shortvideo.dataobject.ShortVideoPostDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短视频内容持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Mapper
public interface ShortVideoPostMapper extends BaseMapper<ShortVideoPostDO> {

    /**
     * 按视频标识查询。
     */
    default Optional<ShortVideoPostDO> findByVideoId(String videoId) {
        QueryWrapper<ShortVideoPostDO> wrapper = new QueryWrapper<>();
        wrapper.eq("video_id", videoId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 查询默认公开信息流。
     */
    default List<ShortVideoPostDO> listPublicFeed(Integer lastFeedPriority, Long lastId, int limit) {
        QueryWrapper<ShortVideoPostDO> wrapper = new QueryWrapper<>();
        wrapper.eq("publish_status", "PUBLISHED");
        wrapper.eq("visibility_status", "PUBLIC");
        if (lastFeedPriority != null && lastId != null && lastId > 0) {
            wrapper.and(nested -> nested.lt("feed_priority", lastFeedPriority)
                    .or()
                    .eq("feed_priority", lastFeedPriority)
                    .lt("id", lastId));
        }
        wrapper.orderByDesc("feed_priority");
        wrapper.orderByDesc("id");
        wrapper.last("LIMIT " + Math.max(1, limit));
        return selectList(wrapper);
    }
}
