package cn.openaipay.domain.shortvideo.model;

import java.time.LocalDateTime;

/**
 * 用户短视频互动状态。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public class UserVideoEngagement {

    /** 主键ID。 */
    private final Long id;
    /** 互动业务标识。 */
    private final String engagementId;
    /** 视频标识。 */
    private final String videoId;
    /** 用户标识。 */
    private final Long userId;
    /** 点赞状态。 */
    private final boolean liked;
    /** 收藏状态。 */
    private final boolean favorited;
    /** 点赞时间。 */
    private final LocalDateTime likedAt;
    /** 收藏时间。 */
    private final LocalDateTime favoritedAt;
    /** 最近浏览时间。 */
    private final LocalDateTime lastViewedAt;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private final LocalDateTime updatedAt;

    public UserVideoEngagement(Long id,
                               String engagementId,
                               String videoId,
                               Long userId,
                               boolean liked,
                               boolean favorited,
                               LocalDateTime likedAt,
                               LocalDateTime favoritedAt,
                               LocalDateTime lastViewedAt,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt) {
        this.id = id;
        this.engagementId = normalizeRequired(engagementId, "engagementId");
        this.videoId = normalizeRequired(videoId, "videoId");
        this.userId = requirePositive(userId, "userId");
        this.liked = liked;
        this.favorited = favorited;
        this.likedAt = likedAt;
        this.favoritedAt = favoritedAt;
        this.lastViewedAt = lastViewedAt;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    public static UserVideoEngagement emptyOf(Long userId, String videoId) {
        LocalDateTime now = LocalDateTime.now();
        return new UserVideoEngagement(
                null,
                "ENG-" + userId + "-" + videoId,
                videoId,
                userId,
                false,
                false,
                null,
                null,
                null,
                now,
                now
        );
    }

    public Long getId() {
        return id;
    }

    public String getEngagementId() {
        return engagementId;
    }

    public String getVideoId() {
        return videoId;
    }

    public Long getUserId() {
        return userId;
    }

    public boolean isLiked() {
        return liked;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public LocalDateTime getLikedAt() {
        return likedAt;
    }

    public LocalDateTime getFavoritedAt() {
        return favoritedAt;
    }

    public LocalDateTime getLastViewedAt() {
        return lastViewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private static String normalizeRequired(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return raw.trim();
    }

    private static Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}
