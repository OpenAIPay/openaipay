package cn.openaipay.infrastructure.shortvideo.repository;

import cn.openaipay.domain.shortvideo.model.VideoStats;
import cn.openaipay.domain.shortvideo.repository.ShortVideoStatsRepository;
import cn.openaipay.infrastructure.shortvideo.dataobject.ShortVideoStatsDO;
import cn.openaipay.infrastructure.shortvideo.mapper.ShortVideoStatsMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 短视频统计仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Repository
public class ShortVideoStatsRepositoryImpl implements ShortVideoStatsRepository {

    /** 持久化接口。 */
    private final ShortVideoStatsMapper shortVideoStatsMapper;

    public ShortVideoStatsRepositoryImpl(ShortVideoStatsMapper shortVideoStatsMapper) {
        this.shortVideoStatsMapper = shortVideoStatsMapper;
    }

    /**
     * 按视频标识查询。
     */
    @Override
    public Optional<VideoStats> findByVideoId(String videoId) {
        return shortVideoStatsMapper.findByVideoId(videoId).map(this::toDomain);
    }

    /**
     * 批量按视频标识查询。
     */
    @Override
    public Map<String, VideoStats> findByVideoIds(List<String> videoIds) {
        Map<String, VideoStats> result = new LinkedHashMap<>();
        shortVideoStatsMapper.findByVideoIds(videoIds).forEach(entity -> result.put(entity.getVideoId(), toDomain(entity)));
        return result;
    }

    /**
     * 保存统计数据。
     */
    @Override
    @Transactional
    public VideoStats save(VideoStats videoStats) {
        ShortVideoStatsDO entity = shortVideoStatsMapper.findByVideoId(videoStats.getVideoId())
                .orElse(new ShortVideoStatsDO());
        LocalDateTime now = LocalDateTime.now();
        entity.setVideoId(videoStats.getVideoId());
        entity.setLikeCount(videoStats.getLikeCount());
        entity.setFavoriteCount(videoStats.getFavoriteCount());
        entity.setCommentCount(videoStats.getCommentCount());
        entity.setPlayCount(videoStats.getPlayCount());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(videoStats.getCreatedAt() == null ? now : videoStats.getCreatedAt());
        }
        entity.setUpdatedAt(videoStats.getUpdatedAt() == null ? now : videoStats.getUpdatedAt());
        return toDomain(shortVideoStatsMapper.save(entity));
    }

    /**
     * 调整互动聚合统计。
     */
    @Override
    @Transactional
    public VideoStats adjustEngagementCounts(String videoId, long likeDelta, long favoriteDelta) {
        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("videoId must not be blank");
        }
        ShortVideoStatsDO entity = shortVideoStatsMapper.findByVideoId(videoId.trim())
                .orElse(new ShortVideoStatsDO());
        LocalDateTime now = LocalDateTime.now();
        entity.setVideoId(videoId.trim());
        entity.setLikeCount(Math.max(0L, (entity.getLikeCount() == null ? 0L : entity.getLikeCount()) + likeDelta));
        entity.setFavoriteCount(Math.max(0L, (entity.getFavoriteCount() == null ? 0L : entity.getFavoriteCount()) + favoriteDelta));
        entity.setCommentCount(entity.getCommentCount() == null ? 0L : entity.getCommentCount());
        entity.setPlayCount(entity.getPlayCount() == null ? 0L : entity.getPlayCount());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        entity.setUpdatedAt(now);
        return toDomain(shortVideoStatsMapper.save(entity));
    }

    /**
     * 调整评论统计。
     */
    @Override
    @Transactional
    public VideoStats adjustCommentCount(String videoId, long commentDelta) {
        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("videoId must not be blank");
        }
        ShortVideoStatsDO entity = shortVideoStatsMapper.findByVideoId(videoId.trim())
                .orElse(new ShortVideoStatsDO());
        LocalDateTime now = LocalDateTime.now();
        entity.setVideoId(videoId.trim());
        entity.setLikeCount(entity.getLikeCount() == null ? 0L : entity.getLikeCount());
        entity.setFavoriteCount(entity.getFavoriteCount() == null ? 0L : entity.getFavoriteCount());
        entity.setCommentCount(Math.max(0L, (entity.getCommentCount() == null ? 0L : entity.getCommentCount()) + commentDelta));
        entity.setPlayCount(entity.getPlayCount() == null ? 0L : entity.getPlayCount());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        entity.setUpdatedAt(now);
        return toDomain(shortVideoStatsMapper.save(entity));
    }

    private VideoStats toDomain(ShortVideoStatsDO entity) {
        return new VideoStats(
                entity.getId(),
                entity.getVideoId(),
                entity.getLikeCount() == null ? 0L : entity.getLikeCount(),
                entity.getFavoriteCount() == null ? 0L : entity.getFavoriteCount(),
                entity.getCommentCount() == null ? 0L : entity.getCommentCount(),
                entity.getPlayCount() == null ? 0L : entity.getPlayCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
