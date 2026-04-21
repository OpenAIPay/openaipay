package cn.openaipay.application.shortvideo.dto;

import java.util.List;

/**
 * 短视频评论分页 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record ShortVideoCommentPageDTO(
        /** 评论条目。 */
        List<ShortVideoCommentDTO> items,
        /** 下一页游标。 */
        String nextCursor,
        /** 是否还有更多。 */
        boolean hasMore
) {
}
