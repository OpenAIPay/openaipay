package cn.openaipay.infrastructure.shortvideo.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.shortvideo.dataobject.ShortVideoEngagementDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短视频互动持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Mapper
public interface ShortVideoEngagementMapper extends BaseMapper<ShortVideoEngagementDO> {

    /**
     * 按用户和视频查询。
     */
    default Optional<ShortVideoEngagementDO> findByUserIdAndVideoId(Long userId, String videoId) {
        QueryWrapper<ShortVideoEngagementDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("video_id", videoId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 批量按用户和视频查询。
     */
    default List<ShortVideoEngagementDO> findByUserIdAndVideoIds(Long userId, Collection<String> videoIds) {
        if (userId == null || userId <= 0 || videoIds == null || videoIds.isEmpty()) {
            return List.of();
        }
        QueryWrapper<ShortVideoEngagementDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.in("video_id", videoIds);
        return selectList(wrapper);
    }

    /**
     * 删除用户对视频的互动记录。
     */
    default int deleteByUserIdAndVideoId(Long userId, String videoId) {
        QueryWrapper<ShortVideoEngagementDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("video_id", videoId);
        return delete(wrapper);
    }
}
