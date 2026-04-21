package cn.openaipay.adapter.shortvideo.web.response;

import cn.openaipay.application.shortvideo.dto.ShortVideoCommentLikeDTO;

/**
 * 短视频评论点赞响应。
 *
 * @author: tenggk.ai
 * @date: 2026/04/03
 */
public record ShortVideoCommentLikeResponse(
        /** 评论标识。 */
        String commentId,
        /** 是否已点赞。 */
        boolean liked,
        /** 点赞数。 */
        long likeCount
) {

    /**
     * DTO 转响应。
     */
    public static ShortVideoCommentLikeResponse from(ShortVideoCommentLikeDTO dto) {
        if (dto == null) {
            return null;
        }
        return new ShortVideoCommentLikeResponse(dto.commentId(), dto.liked(), dto.likeCount());
    }
}
