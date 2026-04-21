package cn.openaipay.application.shortvideo.dto;

import java.util.List;

/**
 * 短视频信息流分页结果。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record ShortVideoFeedPageDTO(
        /** 当前页条目。 */
        List<ShortVideoFeedItemDTO> items,
        /** 下一页游标。 */
        String nextCursor,
        /** 是否还有更多。 */
        boolean hasMore
) {
}
