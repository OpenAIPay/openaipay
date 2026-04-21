package cn.openaipay.adapter.shortvideo.web.response;

import cn.openaipay.application.shortvideo.dto.ShortVideoEngagementDTO;

/**
 * 短视频互动状态响应。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
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

    /**
     * DTO 转响应。
     */
    public static ShortVideoEngagementResponse from(ShortVideoEngagementDTO dto) {
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
