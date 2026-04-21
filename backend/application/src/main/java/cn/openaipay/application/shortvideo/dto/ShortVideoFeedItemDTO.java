package cn.openaipay.application.shortvideo.dto;

/**
 * 短视频信息流条目。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record ShortVideoFeedItemDTO(
        /** 视频标识。 */
        String videoId,
        /** 视频文案。 */
        String caption,
        /** 作者摘要。 */
        ShortVideoAuthorDTO author,
        /** 封面地址。 */
        String coverUrl,
        /** 播放信息。 */
        ShortVideoPlaybackDTO playback,
        /** 当前用户互动快照。 */
        ShortVideoStatsSnapshotDTO engagement
) {
}
