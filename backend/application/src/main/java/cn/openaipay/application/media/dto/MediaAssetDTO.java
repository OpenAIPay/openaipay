package cn.openaipay.application.media.dto;

import java.time.LocalDateTime;

/**
 * 媒体资源数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record MediaAssetDTO(
        /** 媒体资源ID */
        String mediaId,
        /** 所属用户ID */
        Long ownerUserId,
        /** 媒体类型 */
        String mediaType,
        /** 原始名称 */
        String originalName,
        /** 业务类型 */
        String mimeType,
        /** 文件大小字节数 */
        long sizeBytes,
        /** 压缩后大小字节数 */
        Long compressedSizeBytes,
        /** 宽度像素 */
        Integer width,
        /** 高度像素 */
        Integer height,
        /** 业务地址 */
        String contentUrl,
        /** 记录创建时间 */
        LocalDateTime createdAt
) {
}
