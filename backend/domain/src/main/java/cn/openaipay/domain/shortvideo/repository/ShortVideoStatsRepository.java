package cn.openaipay.domain.shortvideo.repository;

import cn.openaipay.domain.shortvideo.model.VideoStats;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 短视频统计仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public interface ShortVideoStatsRepository {

    /**
     * 按视频标识查询。
     */
    Optional<VideoStats> findByVideoId(String videoId);

    /**
     * 批量按视频标识查询。
     */
    Map<String, VideoStats> findByVideoIds(List<String> videoIds);

    /**
     * 保存统计数据。
     */
    VideoStats save(VideoStats videoStats);

    /**
     * 调整点赞/收藏聚合计数。
     */
    VideoStats adjustEngagementCounts(String videoId, long likeDelta, long favoriteDelta);

    /**
     * 调整评论聚合计数。
     */
    VideoStats adjustCommentCount(String videoId, long commentDelta);
}
