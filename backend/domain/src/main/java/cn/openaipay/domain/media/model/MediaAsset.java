package cn.openaipay.domain.media.model;

import java.time.LocalDateTime;

/**
 * 媒体资源模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class MediaAsset {

    /** 主键ID。 */
    private final Long id;
    /** 媒体资源ID。 */
    private final String mediaId;
    /** 所属用户ID。 */
    private final Long ownerUserId;
    /** 媒体类型。 */
    private final String mediaType;
    /** 原始文件名。 */
    private final String originalName;
    /** 文件 MIME 类型。 */
    private final String mimeType;
    /** 文件大小（字节）。 */
    private final long sizeBytes;
    /** 压缩后文件大小（字节）。 */
    private final Long compressedSizeBytes;
    /** 宽度。 */
    private final Integer width;
    /** 高度。 */
    private final Integer height;
    /** 存储路径。 */
    private final String storagePath;
    /** 缩略图路径。 */
    private final String thumbnailPath;
    /** 文件 SHA-256 摘要。 */
    private final String sha256;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public MediaAsset(Long id,
                      String mediaId,
                      Long ownerUserId,
                      String mediaType,
                      String originalName,
                      String mimeType,
                      long sizeBytes,
                      Long compressedSizeBytes,
                      Integer width,
                      Integer height,
                      String storagePath,
                      String thumbnailPath,
                      String sha256,
                      LocalDateTime createdAt,
                      LocalDateTime updatedAt) {
        this.id = id;
        this.mediaId = normalizeRequired(mediaId, "mediaId");
        this.ownerUserId = requirePositive(ownerUserId, "ownerUserId");
        this.mediaType = normalizeRequired(mediaType, "mediaType");
        this.originalName = normalizeRequired(originalName, "originalName");
        this.mimeType = normalizeRequired(mimeType, "mimeType");
        this.sizeBytes = Math.max(0, sizeBytes);
        this.compressedSizeBytes = compressedSizeBytes;
        this.width = width;
        this.height = height;
        this.storagePath = normalizeRequired(storagePath, "storagePath");
        this.thumbnailPath = normalizeOptional(thumbnailPath);
        this.sha256 = normalizeOptional(sha256);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 创建业务数据。
     */
    public static MediaAsset create(String mediaId,
                                    Long ownerUserId,
                                    String mediaType,
                                    String originalName,
                                    String mimeType,
                                    long sizeBytes,
                                    Long compressedSizeBytes,
                                    Integer width,
                                    Integer height,
                                    String storagePath,
                                    String thumbnailPath,
                                    String sha256,
                                    LocalDateTime now) {
        LocalDateTime createdAt = now == null ? LocalDateTime.now() : now;
        return new MediaAsset(
                null,
                mediaId,
                ownerUserId,
                mediaType,
                originalName,
                mimeType,
                sizeBytes,
                compressedSizeBytes,
                width,
                height,
                storagePath,
                thumbnailPath,
                sha256,
                createdAt,
                createdAt
        );
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取媒体ID。
     */
    public String getMediaId() {
        return mediaId;
    }

    /**
     * 获取所属方用户ID。
     */
    public Long getOwnerUserId() {
        return ownerUserId;
    }

    /**
     * 获取媒体类型信息。
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * 获取业务数据。
     */
    public String getOriginalName() {
        return originalName;
    }

    /**
     * 获取业务数据。
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * 获取业务数据。
     */
    public long getSizeBytes() {
        return sizeBytes;
    }

    /**
     * 获取业务数据。
     */
    public Long getCompressedSizeBytes() {
        return compressedSizeBytes;
    }

    /**
     * 获取业务数据。
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * 获取业务数据。
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * 获取业务数据。
     */
    public String getStoragePath() {
        return storagePath;
    }

    /**
     * 获取业务数据。
     */
    public String getThumbnailPath() {
        return thumbnailPath;
    }

    /**
     * 获取SHA256信息。
     */
    public String getSha256() {
        return sha256;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取AT信息。
     */
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
