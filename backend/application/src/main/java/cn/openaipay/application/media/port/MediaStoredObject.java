package cn.openaipay.application.media.port;

/**
 * 存储后的媒体对象。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record MediaStoredObject(
        /** 存储路径 */
        String storagePath,
        /** 缩略图路径 */
        String thumbnailPath,
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
        /** SHA256信息 */
        String sha256
) {
}
