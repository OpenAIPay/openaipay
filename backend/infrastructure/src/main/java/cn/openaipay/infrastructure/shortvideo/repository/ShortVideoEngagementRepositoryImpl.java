package cn.openaipay.infrastructure.shortvideo.repository;

import cn.openaipay.domain.shortvideo.model.UserVideoEngagement;
import cn.openaipay.domain.shortvideo.repository.ShortVideoEngagementRepository;
import cn.openaipay.infrastructure.shortvideo.dataobject.ShortVideoEngagementDO;
import cn.openaipay.infrastructure.shortvideo.mapper.ShortVideoEngagementMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 短视频互动仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Repository
public class ShortVideoEngagementRepositoryImpl implements ShortVideoEngagementRepository {

    /** 持久化接口。 */
    private final ShortVideoEngagementMapper shortVideoEngagementMapper;

    public ShortVideoEngagementRepositoryImpl(ShortVideoEngagementMapper shortVideoEngagementMapper) {
        this.shortVideoEngagementMapper = shortVideoEngagementMapper;
    }

    /**
     * 按用户和视频查询。
     */
    @Override
    public Optional<UserVideoEngagement> findByUserIdAndVideoId(Long userId, String videoId) {
        return shortVideoEngagementMapper.findByUserIdAndVideoId(userId, videoId).map(this::toDomain);
    }

    /**
     * 批量查询用户对多条视频的互动状态。
     */
    @Override
    public Map<String, UserVideoEngagement> findByUserIdAndVideoIds(Long userId, List<String> videoIds) {
        Map<String, UserVideoEngagement> result = new LinkedHashMap<>();
        shortVideoEngagementMapper.findByUserIdAndVideoIds(userId, videoIds)
                .forEach(entity -> result.put(entity.getVideoId(), toDomain(entity)));
        return result;
    }

    /**
     * 保存互动状态。
     */
    @Override
    @Transactional
    public UserVideoEngagement save(UserVideoEngagement engagement) {
        ShortVideoEngagementDO entity = shortVideoEngagementMapper.findByUserIdAndVideoId(
                        engagement.getUserId(),
                        engagement.getVideoId()
                )
                .orElse(new ShortVideoEngagementDO());
        LocalDateTime now = LocalDateTime.now();
        entity.setEngagementId(engagement.getEngagementId());
        entity.setVideoId(engagement.getVideoId());
        entity.setUserId(engagement.getUserId());
        entity.setLiked(engagement.isLiked());
        entity.setFavorited(engagement.isFavorited());
        entity.setLikedAt(engagement.getLikedAt());
        entity.setFavoritedAt(engagement.getFavoritedAt());
        entity.setLastViewedAt(engagement.getLastViewedAt());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(engagement.getCreatedAt() == null ? now : engagement.getCreatedAt());
        }
        entity.setUpdatedAt(engagement.getUpdatedAt() == null ? now : engagement.getUpdatedAt());
        return toDomain(shortVideoEngagementMapper.save(entity));
    }

    /**
     * 删除用户对视频的互动状态。
     */
    @Override
    @Transactional
    public void deleteByUserIdAndVideoId(Long userId, String videoId) {
        if (userId == null || userId <= 0 || videoId == null || videoId.isBlank()) {
            return;
        }
        shortVideoEngagementMapper.deleteByUserIdAndVideoId(userId, videoId.trim());
    }

    private UserVideoEngagement toDomain(ShortVideoEngagementDO entity) {
        return new UserVideoEngagement(
                entity.getId(),
                entity.getEngagementId(),
                entity.getVideoId(),
                entity.getUserId(),
                Boolean.TRUE.equals(entity.getLiked()),
                Boolean.TRUE.equals(entity.getFavorited()),
                entity.getLikedAt(),
                entity.getFavoritedAt(),
                entity.getLastViewedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
