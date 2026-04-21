package cn.openaipay.application.shortvideo.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 短视频评论 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record ShortVideoCommentDTO(
        /** 评论标识。 */
        String commentId,
        /** 视频标识。 */
        String videoId,
        /** 父评论标识。 */
        String parentCommentId,
        /** 根评论标识。 */
        String rootCommentId,
        /** 评论用户。 */
        ShortVideoAuthorDTO user,
        /** 评论内容。 */
        String content,
        /** 评论图片地址。 */
        String imageUrl,
        /** 是否已点赞。 */
        boolean liked,
        /** 点赞数。 */
        long likeCount,
        /** 回复数。 */
        long replyCount,
        /** 预览回复列表。 */
        List<ShortVideoCommentDTO> previewReplies,
        /** 创建时间。 */
        LocalDateTime createdAt
) {
}
