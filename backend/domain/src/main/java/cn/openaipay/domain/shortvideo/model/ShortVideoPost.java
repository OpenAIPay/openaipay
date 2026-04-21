package cn.openaipay.domain.shortvideo.model;

import java.time.LocalDateTime;

/**
 * 短视频内容模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public class ShortVideoPost {

    /** 主键ID。 */
    private final Long id;
    /** 视频业务标识。 */
    private final String videoId;
    /** 作者用户ID。 */
    private final Long creatorUserId;
    /** 视频文案。 */
    private final String caption;
    /** 封面媒资ID。 */
    private final String coverMediaId;
    /** 播放媒资ID。 */
    private final String playbackMediaId;
    /** 视频时长毫秒。 */
    private final Long durationMs;
    /** 画面比例。 */
    private final String aspectRatio;
    /** 发布状态。 */
    private final String publishStatus;
    /** 可见性状态。 */
    private final String visibilityStatus;
    /** 信息流权重。 */
    private final Integer feedPriority;
    /** 发布时间。 */
    private final LocalDateTime publishedAt;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private final LocalDateTime updatedAt;

    public ShortVideoPost(Long id,
                          String videoId,
                          Long creatorUserId,
                          String caption,
                          String coverMediaId,
                          String playbackMediaId,
                          Long durationMs,
                          String aspectRatio,
                          String publishStatus,
                          String visibilityStatus,
                          Integer feedPriority,
                          LocalDateTime publishedAt,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.id = id;
        this.videoId = normalizeRequired(videoId, "videoId");
        this.creatorUserId = requirePositive(creatorUserId, "creatorUserId");
        this.caption = normalizeOptional(caption);
        this.coverMediaId = normalizeRequired(coverMediaId, "coverMediaId");
        this.playbackMediaId = normalizeRequired(playbackMediaId, "playbackMediaId");
        this.durationMs = requirePositive(durationMs, "durationMs");
        this.aspectRatio = normalizeRequired(aspectRatio, "aspectRatio");
        this.publishStatus = normalizeRequired(publishStatus, "publishStatus");
        this.visibilityStatus = normalizeRequired(visibilityStatus, "visibilityStatus");
        this.feedPriority = feedPriority == null ? 0 : feedPriority;
        this.publishedAt = publishedAt == null ? LocalDateTime.now() : publishedAt;
        this.createdAt = createdAt == null ? this.publishedAt : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 是否对默认信息流可见。
     */
    public boolean isPublicPublished() {
        return "PUBLISHED".equalsIgnoreCase(publishStatus)
                && "PUBLIC".equalsIgnoreCase(visibilityStatus);
    }

    public Long getId() {
        return id;
    }

    public String getVideoId() {
        return videoId;
    }

    public Long getCreatorUserId() {
        return creatorUserId;
    }

    public String getCaption() {
        return caption;
    }

    public String getCoverMediaId() {
        return coverMediaId;
    }

    public String getPlaybackMediaId() {
        return playbackMediaId;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public String getAspectRatio() {
        return aspectRatio;
    }

    public String getPublishStatus() {
        return publishStatus;
    }

    public String getVisibilityStatus() {
        return visibilityStatus;
    }

    public Integer getFeedPriority() {
        return feedPriority;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private static String normalizeRequired(String raw, String fieldName) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}
