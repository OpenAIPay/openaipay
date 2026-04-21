package cn.openaipay.application.shortvideo.query;

/**
 * 短视频评论列表查询。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record ListShortVideoCommentsQuery(
        /** 视频标识。 */
        String videoId,
        /** 翻页游标。 */
        String cursor,
        /** 页大小。 */
        Integer limit
) {
}
