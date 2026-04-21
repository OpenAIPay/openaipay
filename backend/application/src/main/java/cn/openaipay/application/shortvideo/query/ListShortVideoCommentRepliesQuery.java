package cn.openaipay.application.shortvideo.query;

/**
 * 查询短视频评论回复列表。
 *
 * @author: tenggk.ai
 * @date: 2026/04/03
 */
public record ListShortVideoCommentRepliesQuery(
        /** 评论标识。 */
        String commentId,
        /** 分页游标。 */
        String cursor,
        /** 单页数量。 */
        Integer limit
) {
}
