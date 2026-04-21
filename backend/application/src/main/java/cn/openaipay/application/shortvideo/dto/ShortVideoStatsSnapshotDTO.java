package cn.openaipay.application.shortvideo.dto;

/**
 * 短视频统计与当前用户互动快照。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record ShortVideoStatsSnapshotDTO(
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
}
