package cn.openaipay.infrastructure.shortvideo.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.shortvideo.dataobject.ShortVideoStatsDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短视频统计持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Mapper
public interface ShortVideoStatsMapper extends BaseMapper<ShortVideoStatsDO> {

    /**
     * 按视频标识查询。
     */
    default Optional<ShortVideoStatsDO> findByVideoId(String videoId) {
        QueryWrapper<ShortVideoStatsDO> wrapper = new QueryWrapper<>();
        wrapper.eq("video_id", videoId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 批量按视频标识查询。
     */
    default List<ShortVideoStatsDO> findByVideoIds(Collection<String> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return List.of();
        }
        QueryWrapper<ShortVideoStatsDO> wrapper = new QueryWrapper<>();
        wrapper.in("video_id", videoIds);
        return selectList(wrapper);
    }
}
