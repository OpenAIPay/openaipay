package cn.openaipay.application.shortvideo.service.impl;

import cn.openaipay.application.shortvideo.command.FavoriteShortVideoCommand;
import cn.openaipay.application.shortvideo.command.LikeShortVideoCommand;
import cn.openaipay.application.shortvideo.dto.ShortVideoEngagementDTO;
import cn.openaipay.application.shortvideo.service.ShortVideoEngagementService;
import cn.openaipay.domain.shortvideo.model.ShortVideoPost;
import cn.openaipay.domain.shortvideo.model.UserVideoEngagement;
import cn.openaipay.domain.shortvideo.model.VideoStats;
import cn.openaipay.domain.shortvideo.repository.ShortVideoEngagementRepository;
import cn.openaipay.domain.shortvideo.repository.ShortVideoPostRepository;
import cn.openaipay.domain.shortvideo.repository.ShortVideoStatsRepository;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 短视频互动应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Service
public class ShortVideoEngagementServiceImpl implements ShortVideoEngagementService {

    /** 内容仓储。 */
    private final ShortVideoPostRepository shortVideoPostRepository;
    /** 互动仓储。 */
    private final ShortVideoEngagementRepository shortVideoEngagementRepository;
    /** 统计仓储。 */
    private final ShortVideoStatsRepository shortVideoStatsRepository;

    public ShortVideoEngagementServiceImpl(ShortVideoPostRepository shortVideoPostRepository,
                                           ShortVideoEngagementRepository shortVideoEngagementRepository,
                                           ShortVideoStatsRepository shortVideoStatsRepository) {
        this.shortVideoPostRepository = shortVideoPostRepository;
        this.shortVideoEngagementRepository = shortVideoEngagementRepository;
        this.shortVideoStatsRepository = shortVideoStatsRepository;
    }

    /**
     * 点赞视频。
     */
    @Override
    @Transactional
    public ShortVideoEngagementDTO like(Long userId, LikeShortVideoCommand command) {
        return mutate(userId, command == null ? null : command.videoId(), EngagementKind.LIKE, true);
    }

    /**
     * 取消点赞。
     */
    @Override
    @Transactional
    public ShortVideoEngagementDTO unlike(Long userId, LikeShortVideoCommand command) {
        return mutate(userId, command == null ? null : command.videoId(), EngagementKind.LIKE, false);
    }

    /**
     * 收藏视频。
     */
    @Override
    @Transactional
    public ShortVideoEngagementDTO favorite(Long userId, FavoriteShortVideoCommand command) {
        return mutate(userId, command == null ? null : command.videoId(), EngagementKind.FAVORITE, true);
    }

    /**
     * 取消收藏。
     */
    @Override
    @Transactional
    public ShortVideoEngagementDTO unfavorite(Long userId, FavoriteShortVideoCommand command) {
        return mutate(userId, command == null ? null : command.videoId(), EngagementKind.FAVORITE, false);
    }

    private ShortVideoEngagementDTO mutate(Long userId, String rawVideoId, EngagementKind kind, boolean targetState) {
        Long normalizedUserId = requirePositive(userId, "userId");
        String videoId = normalizeVideoId(rawVideoId);
        ShortVideoPost post = shortVideoPostRepository.findByVideoId(videoId)
                .filter(ShortVideoPost::isPublicPublished)
                .orElseThrow(() -> new NoSuchElementException("video not found: " + videoId));

        Optional<UserVideoEngagement> existingOpt =
                shortVideoEngagementRepository.findByUserIdAndVideoId(normalizedUserId, post.getVideoId());
        UserVideoEngagement current = existingOpt.orElse(UserVideoEngagement.emptyOf(normalizedUserId, post.getVideoId()));

        boolean nextLiked = kind == EngagementKind.LIKE ? targetState : current.isLiked();
        boolean nextFavorited = kind == EngagementKind.FAVORITE ? targetState : current.isFavorited();
        long likeDelta = current.isLiked() == nextLiked ? 0L : (nextLiked ? 1L : -1L);
        long favoriteDelta = current.isFavorited() == nextFavorited ? 0L : (nextFavorited ? 1L : -1L);

        VideoStats stats = (likeDelta == 0L && favoriteDelta == 0L)
                ? shortVideoStatsRepository.findByVideoId(post.getVideoId()).orElse(defaultStats(post.getVideoId()))
                : shortVideoStatsRepository.adjustEngagementCounts(post.getVideoId(), likeDelta, favoriteDelta);

        if (likeDelta == 0L && favoriteDelta == 0L) {
            return toDTO(current, stats);
        }

        if (!nextLiked && !nextFavorited) {
            if (existingOpt.isPresent()) {
                shortVideoEngagementRepository.deleteByUserIdAndVideoId(normalizedUserId, post.getVideoId());
            }
            return toDTO(UserVideoEngagement.emptyOf(normalizedUserId, post.getVideoId()), stats);
        }

        LocalDateTime now = LocalDateTime.now();
        UserVideoEngagement updated = new UserVideoEngagement(
                current.getId(),
                current.getEngagementId(),
                post.getVideoId(),
                normalizedUserId,
                nextLiked,
                nextFavorited,
                nextLiked ? resolveTimestamp(current.isLiked(), current.getLikedAt(), now) : null,
                nextFavorited ? resolveTimestamp(current.isFavorited(), current.getFavoritedAt(), now) : null,
                current.getLastViewedAt(),
                current.getCreatedAt(),
                now
        );
        UserVideoEngagement saved = shortVideoEngagementRepository.save(updated);
        return toDTO(saved, stats);
    }

    private LocalDateTime resolveTimestamp(boolean alreadyEnabled, LocalDateTime existingTimestamp, LocalDateTime now) {
        if (alreadyEnabled && existingTimestamp != null) {
            return existingTimestamp;
        }
        return now;
    }

    private ShortVideoEngagementDTO toDTO(UserVideoEngagement engagement, VideoStats stats) {
        return new ShortVideoEngagementDTO(
                engagement.isLiked(),
                engagement.isFavorited(),
                stats.getLikeCount(),
                stats.getFavoriteCount(),
                stats.getCommentCount()
        );
    }

    private VideoStats defaultStats(String videoId) {
        LocalDateTime now = LocalDateTime.now();
        return new VideoStats(null, videoId, 0, 0, 0, 0, now, now);
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private String normalizeVideoId(String rawVideoId) {
        if (rawVideoId == null || rawVideoId.isBlank()) {
            throw new IllegalArgumentException("videoId must not be blank");
        }
        return rawVideoId.trim();
    }

    /**
     * 互动类型。
     */
    private enum EngagementKind {
        LIKE,
        FAVORITE
    }
}
