package cn.openaipay.application.shortvideo.port;

import cn.openaipay.application.shortvideo.dto.ShortVideoPlaybackDTO;
import cn.openaipay.domain.media.model.MediaAsset;
import cn.openaipay.domain.shortvideo.model.ShortVideoPost;

/**
 * 短视频播放资源解析端口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public interface ShortVideoPlaybackPort {

    /**
     * 解析播放信息。
     */
    ShortVideoPlaybackDTO resolvePlayback(ShortVideoPost shortVideoPost, MediaAsset playbackMediaAsset);

    /**
     * 解析公开资源地址。
     */
    String resolveResourceUrl(MediaAsset mediaAsset);
}
