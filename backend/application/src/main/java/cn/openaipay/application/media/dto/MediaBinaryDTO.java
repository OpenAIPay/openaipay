package cn.openaipay.application.media.dto;

/**
 * 媒体二进制数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record MediaBinaryDTO(
        /** 媒体资源ID */
        String mediaId,
        /** 业务类型 */
        String mimeType,
        /** 原始名称 */
        String originalName,
        /** 二进制内容 */
        byte[] bytes
) {
}
