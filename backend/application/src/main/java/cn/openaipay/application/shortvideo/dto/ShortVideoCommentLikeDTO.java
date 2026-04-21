package cn.openaipay.application.shortvideo.dto;

/**
 * 短视频评论点赞 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/04/03
 */
public record ShortVideoCommentLikeDTO(
        /** 评论标识。 */
        String commentId,
        /** 是否已点赞。 */
        boolean liked,
        /** 点赞数。 */
        long likeCount
) {
}
