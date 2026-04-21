package cn.openaipay.application.shortvideo.dto;

/**
 * 短视频播放信息。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record ShortVideoPlaybackDTO(
        /** 播放地址。 */
        String playbackUrl,
        /** 协议。 */
        String protocol,
        /** MIME 类型。 */
        String mimeType,
        /** 时长毫秒。 */
        Long durationMs,
        /** 视频宽度。 */
        Integer width,
        /** 视频高度。 */
        Integer height
) {
}
