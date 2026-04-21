package cn.openaipay.domain.shortvideo.model;

import java.time.LocalDateTime;

/**
 * 短视频评论模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public class VideoComment {

    /** 主键ID。 */
    private final Long id;
    /** 评论业务标识。 */
    private final String commentId;
    /** 视频标识。 */
    private final String videoId;
    /** 父评论标识。 */
    private final String parentCommentId;
    /** 根评论标识。 */
    private final String rootCommentId;
    /** 评论用户ID。 */
    private final Long userId;
    /** 评论内容。 */
    private final String content;
    /** 评论图片媒体标识。 */
    private final String imageMediaId;
    /** 点赞数。 */
    private final long likeCount;
    /** 回复数。 */
    private final long replyCount;
    /** 评论状态。 */
    private final String status;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private final LocalDateTime updatedAt;

    public VideoComment(Long id,
                        String commentId,
                        String videoId,
                        String parentCommentId,
                        String rootCommentId,
                        Long userId,
                        String content,
                        String imageMediaId,
                        long likeCount,
                        long replyCount,
                        String status,
                        LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
        this.id = id;
        this.commentId = normalizeRequired(commentId, "commentId");
        this.videoId = normalizeRequired(videoId, "videoId");
        this.parentCommentId = normalizeOptional(parentCommentId);
        this.rootCommentId = normalizeOptional(rootCommentId);
        this.userId = requirePositive(userId, "userId");
        this.content = normalizeOptional(content);
        this.imageMediaId = normalizeOptional(imageMediaId);
        this.likeCount = Math.max(0L, likeCount);
        this.replyCount = Math.max(0L, replyCount);
        this.status = normalizeRequired(status, "status");
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        if ((this.content == null || this.content.isBlank()) && (this.imageMediaId == null || this.imageMediaId.isBlank())) {
            throw new IllegalArgumentException("content or imageMediaId must not both be blank");
        }
    }

    public VideoComment(Long id,
                        String commentId,
                        String videoId,
                        Long userId,
                        String content,
                        String status,
                        LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
        this(id, commentId, videoId, null, null, userId, content, null, 0L, 0L, status, createdAt, updatedAt);
    }

    public Long getId() {
        return id;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getParentCommentId() {
        return parentCommentId;
    }

    public String getRootCommentId() {
        return rootCommentId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public String getImageMediaId() {
        return imageMediaId;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public long getReplyCount() {
        return replyCount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isTopLevel() {
        return parentCommentId == null;
    }

    public String getRootThreadCommentId() {
        return rootCommentId == null ? commentId : rootCommentId;
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
        String normalized = raw.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}
