package cn.openaipay.application.shortvideo.dto;

/**
 * 短视频互动状态 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record ShortVideoEngagementDTO(
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
