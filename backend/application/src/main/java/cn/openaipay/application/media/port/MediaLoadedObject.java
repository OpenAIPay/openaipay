package cn.openaipay.application.media.port;

/**
 * 加载后的媒体对象。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record MediaLoadedObject(
        /** 业务类型 */
        String mimeType,
        /** 二进制内容 */
        byte[] bytes
) {
}
