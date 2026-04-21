package cn.openaipay.adapter.shortvideo.web.request;

import jakarta.validation.constraints.Size;

/**
 * 发布评论请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record CreateShortVideoCommentRequest(
        /** 父评论标识。 */
        @Size(max = 64)
        String parentCommentId,
        /** 评论内容。 */
        @Size(max = 500)
        String content,
        /** 图片媒体标识。 */
        @Size(max = 64)
        String imageMediaId
) {
}
