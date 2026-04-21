package cn.openaipay.adapter.shortvideo.web.response;

import cn.openaipay.application.shortvideo.dto.ShortVideoAuthorDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoFeedItemDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoFeedPageDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoPlaybackDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoStatsSnapshotDTO;
import java.util.List;

/**
 * 短视频信息流响应。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record ShortVideoFeedResponse(
        /** 条目列表。 */
        List<ShortVideoFeedItemResponse> items,
        /** 下一页游标。 */
        String nextCursor,
        /** 是否还有更多。 */
        boolean hasMore
) {

    /**
     * DTO 转响应。
     */
    public static ShortVideoFeedResponse from(ShortVideoFeedPageDTO dto) {
        if (dto == null) {
            return new ShortVideoFeedResponse(List.of(), null, false);
        }
        return new ShortVideoFeedResponse(
                dto.items() == null ? List.of() : dto.items().stream().map(ShortVideoFeedItemResponse::from).toList(),
                dto.nextCursor(),
                dto.hasMore()
        );
    }

    /**
     * 信息流条目响应。
     */
    public record ShortVideoFeedItemResponse(
            /** 视频标识。 */
            String videoId,
            /** 视频文案。 */
            String caption,
            /** 作者摘要。 */
            ShortVideoAuthorResponse author,
            /** 封面地址。 */
            String coverUrl,
            /** 播放信息。 */
            ShortVideoPlaybackResponse playback,
            /** 当前用户互动快照。 */
            ShortVideoEngagementResponse engagement
    ) {

        private static ShortVideoFeedItemResponse from(ShortVideoFeedItemDTO dto) {
            return new ShortVideoFeedItemResponse(
                    dto.videoId(),
                    dto.caption(),
                    ShortVideoAuthorResponse.from(dto.author()),
                    dto.coverUrl(),
                    ShortVideoPlaybackResponse.from(dto.playback()),
                    ShortVideoEngagementResponse.from(dto.engagement())
            );
        }
    }

    /**
     * 作者摘要响应。
     */
    public record ShortVideoAuthorResponse(
            /** 用户ID。 */
            Long userId,
            /** 昵称。 */
            String nickname,
            /** 头像地址。 */
            String avatarUrl
    ) {

        private static ShortVideoAuthorResponse from(ShortVideoAuthorDTO dto) {
            return new ShortVideoAuthorResponse(
                    dto == null ? null : dto.userId(),
                    dto == null ? null : dto.nickname(),
                    dto == null ? null : dto.avatarUrl()
            );
        }
    }

    /**
     * 播放信息响应。
     */
    public record ShortVideoPlaybackResponse(
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

        private static ShortVideoPlaybackResponse from(ShortVideoPlaybackDTO dto) {
            return new ShortVideoPlaybackResponse(
                    dto == null ? null : dto.playbackUrl(),
                    dto == null ? null : dto.protocol(),
                    dto == null ? null : dto.mimeType(),
                    dto == null ? null : dto.durationMs(),
                    dto == null ? null : dto.width(),
                    dto == null ? null : dto.height()
            );
        }
    }

    /**
     * 互动快照响应。
     */
    public record ShortVideoEngagementResponse(
            /** 是否已点赞。 */
            boolean liked,
            /** 是否已收藏。 */
            boolean favorited,
            /** 点赞数。 */
            long likeCount,
            /** 收藏数。 */
            long favoriteCount,
            /** 评论数。 */
            long commentCount
    ) {

        private static ShortVideoEngagementResponse from(ShortVideoStatsSnapshotDTO dto) {
            if (dto == null) {
                return new ShortVideoEngagementResponse(false, false, 0, 0, 0);
            }
            return new ShortVideoEngagementResponse(
                    dto.liked(),
                    dto.favorited(),
                    dto.likeCount(),
                    dto.favoriteCount(),
                    dto.commentCount()
            );
        }
    }
}
