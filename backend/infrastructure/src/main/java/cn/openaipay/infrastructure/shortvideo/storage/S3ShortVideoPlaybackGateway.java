package cn.openaipay.infrastructure.shortvideo.storage;

import cn.openaipay.application.shortvideo.dto.ShortVideoPlaybackDTO;
import cn.openaipay.application.shortvideo.port.ShortVideoPlaybackPort;
import cn.openaipay.domain.media.model.MediaAsset;
import cn.openaipay.domain.shortvideo.model.ShortVideoPost;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 基于对象存储/CDN 的短视频播放地址解析网关。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Component
public class S3ShortVideoPlaybackGateway implements ShortVideoPlaybackPort {

    /** CDN 根地址。 */
    private final String cdnBaseUrl;
    /** 默认播放协议。 */
    private final String defaultPlaybackProtocol;
    /** 默认 MIME 类型。 */
    private final String defaultPlaybackMimeType;

    public S3ShortVideoPlaybackGateway(
            @Value("${aipay.short-video.cdn-base-url:}") String cdnBaseUrl,
            @Value("${aipay.short-video.default-playback-protocol:MP4}") String defaultPlaybackProtocol,
            @Value("${aipay.short-video.default-playback-mime-type:video/mp4}") String defaultPlaybackMimeType) {
        this.cdnBaseUrl = normalizeOptional(cdnBaseUrl);
        this.defaultPlaybackProtocol = normalizeOptional(defaultPlaybackProtocol) == null
                ? "MP4"
                : defaultPlaybackProtocol.trim().toUpperCase(Locale.ROOT);
        this.defaultPlaybackMimeType = normalizeOptional(defaultPlaybackMimeType) == null
                ? "video/mp4"
                : defaultPlaybackMimeType.trim();
    }

    /**
     * 解析播放信息。
     */
    @Override
    public ShortVideoPlaybackDTO resolvePlayback(ShortVideoPost shortVideoPost, MediaAsset playbackMediaAsset) {
        String playbackUrl = resolveResourceUrl(playbackMediaAsset);
        String mimeType = normalizeOptional(playbackMediaAsset.getMimeType()) == null
                ? defaultPlaybackMimeType
                : playbackMediaAsset.getMimeType();
        String protocol = resolveProtocol(playbackUrl, mimeType);
        return new ShortVideoPlaybackDTO(
                playbackUrl,
                protocol,
                mimeType,
                shortVideoPost.getDurationMs(),
                playbackMediaAsset.getWidth(),
                playbackMediaAsset.getHeight()
        );
    }

    /**
     * 解析公开资源地址。
     */
    @Override
    public String resolveResourceUrl(MediaAsset mediaAsset) {
        String storagePath = normalizeOptional(mediaAsset.getStoragePath());
        if (storagePath == null) {
            return "/api/media/" + mediaAsset.getMediaId() + "/content";
        }
        if (isAbsoluteUrl(storagePath)) {
            return storagePath;
        }
        if (cdnBaseUrl != null) {
            return joinUrl(cdnBaseUrl, storagePath);
        }
        return "/api/media/" + mediaAsset.getMediaId() + "/content";
    }

    private String resolveProtocol(String playbackUrl, String mimeType) {
        String lowerMimeType = mimeType == null ? "" : mimeType.toLowerCase(Locale.ROOT);
        String lowerPlaybackUrl = playbackUrl == null ? "" : playbackUrl.toLowerCase(Locale.ROOT);
        if (lowerMimeType.contains("mpegurl") || lowerPlaybackUrl.endsWith(".m3u8")) {
            return "HLS";
        }
        if (lowerMimeType.contains("dash") || lowerPlaybackUrl.endsWith(".mpd")) {
            return "DASH";
        }
        return defaultPlaybackProtocol;
    }

    private boolean isAbsoluteUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private String joinUrl(String baseUrl, String path) {
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        return normalizedBase + "/" + normalizedPath;
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
