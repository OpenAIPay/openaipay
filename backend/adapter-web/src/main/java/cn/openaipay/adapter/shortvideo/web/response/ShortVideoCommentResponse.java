package cn.openaipay.adapter.shortvideo.web.response;

import cn.openaipay.application.shortvideo.dto.ShortVideoAuthorDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentPageDTO;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 短视频评论响应。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record ShortVideoCommentResponse(
        /** 评论标识。 */
        String commentId,
        /** 视频标识。 */
        String videoId,
        /** 父评论标识。 */
        String parentCommentId,
        /** 根评论标识。 */
        String rootCommentId,
        /** 评论用户。 */
        CommentUserResponse user,
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
        /** 预览回复。 */
        List<ShortVideoCommentResponse> previewReplies,
        /** 创建时间。 */
        LocalDateTime createdAt
) {

    /**
     * DTO 转响应。
     */
    public static ShortVideoCommentResponse from(ShortVideoCommentDTO dto) {
        if (dto == null) {
            return null;
        }
        return new ShortVideoCommentResponse(
                dto.commentId(),
                dto.videoId(),
                dto.parentCommentId(),
                dto.rootCommentId(),
                CommentUserResponse.from(dto.user()),
                dto.content(),
                dto.imageUrl(),
                dto.liked(),
                dto.likeCount(),
                dto.replyCount(),
                dto.previewReplies() == null
                        ? List.of()
                        : dto.previewReplies().stream().map(ShortVideoCommentResponse::from).toList(),
                dto.createdAt()
        );
    }

    /**
     * 评论分页响应。
     */
    public record CommentPageResponse(
            /** 评论项。 */
            List<ShortVideoCommentResponse> items,
            /** 下一页游标。 */
            String nextCursor,
            /** 是否还有更多。 */
            boolean hasMore
    ) {

        public static CommentPageResponse from(ShortVideoCommentPageDTO dto) {
            if (dto == null) {
                return new CommentPageResponse(List.of(), null, false);
            }
            return new CommentPageResponse(
                    dto.items() == null ? List.of() : dto.items().stream().map(ShortVideoCommentResponse::from).toList(),
                    dto.nextCursor(),
                    dto.hasMore()
            );
        }
    }

    /**
     * 评论用户响应。
     */
    public record CommentUserResponse(
            /** 用户标识。 */
            Long userId,
            /** 用户昵称。 */
            String nickname,
            /** 头像地址。 */
            String avatarUrl
    ) {

        private static CommentUserResponse from(ShortVideoAuthorDTO dto) {
            if (dto == null) {
                return null;
            }
            return new CommentUserResponse(dto.userId(), dto.nickname(), dto.avatarUrl());
        }
    }
}
