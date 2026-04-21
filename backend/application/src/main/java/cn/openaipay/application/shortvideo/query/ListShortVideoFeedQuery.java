package cn.openaipay.application.shortvideo.query;

/**
 * 查询短视频信息流请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record ListShortVideoFeedQuery(
        /** 翻页游标。 */
        String cursor,
        /** 请求条数。 */
        Integer limit
) {
}
