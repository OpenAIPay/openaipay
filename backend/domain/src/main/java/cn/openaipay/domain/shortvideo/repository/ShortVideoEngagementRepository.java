package cn.openaipay.domain.shortvideo.repository;

import cn.openaipay.domain.shortvideo.model.UserVideoEngagement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户短视频互动仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public interface ShortVideoEngagementRepository {

    /**
     * 按用户和视频查询。
     */
    Optional<UserVideoEngagement> findByUserIdAndVideoId(Long userId, String videoId);

    /**
     * 批量查询用户对多条视频的互动状态。
     */
    Map<String, UserVideoEngagement> findByUserIdAndVideoIds(Long userId, List<String> videoIds);

    /**
     * 保存互动状态。
     */
    UserVideoEngagement save(UserVideoEngagement engagement);

    /**
     * 删除用户对某条视频的互动状态。
     */
    void deleteByUserIdAndVideoId(Long userId, String videoId);
}
