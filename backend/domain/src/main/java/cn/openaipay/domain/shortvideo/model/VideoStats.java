package cn.openaipay.domain.shortvideo.model;

import java.time.LocalDateTime;

/**
 * 短视频统计快照。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public class VideoStats {

    /** 主键ID。 */
    private final Long id;
    /** 视频标识。 */
    private final String videoId;
    /** 点赞数。 */
    private final long likeCount;
    /** 收藏数。 */
    private final long favoriteCount;
    /** 评论数。 */
    private final long commentCount;
    /** 播放数。 */
    private final long playCount;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private final LocalDateTime updatedAt;

    public VideoStats(Long id,
                      String videoId,
                      long likeCount,
                      long favoriteCount,
                      long commentCount,
                      long playCount,
                      LocalDateTime createdAt,
                      LocalDateTime updatedAt) {
        this.id = id;
        this.videoId = normalizeRequired(videoId, "videoId");
        this.likeCount = Math.max(0L, likeCount);
        this.favoriteCount = Math.max(0L, favoriteCount);
        this.commentCount = Math.max(0L, commentCount);
        this.playCount = Math.max(0L, playCount);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getVideoId() {
        return videoId;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public long getFavoriteCount() {
        return favoriteCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public long getPlayCount() {
        return playCount;
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
}
