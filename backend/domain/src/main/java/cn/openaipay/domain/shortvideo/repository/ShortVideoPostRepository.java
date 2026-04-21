package cn.openaipay.domain.shortvideo.repository;

import cn.openaipay.domain.shortvideo.model.FeedCursor;
import cn.openaipay.domain.shortvideo.model.ShortVideoPost;
import java.util.List;
import java.util.Optional;

/**
 * 短视频内容仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public interface ShortVideoPostRepository {

    /**
     * 按视频标识查询。
     */
    Optional<ShortVideoPost> findByVideoId(String videoId);

    /**
     * 查询默认公开信息流。
     */
    List<ShortVideoPost> listPublicFeed(FeedCursor cursor, int limit);
}
