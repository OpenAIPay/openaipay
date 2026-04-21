package cn.openaipay.domain.shortvideo.model;

import java.time.LocalDateTime;

/**
 * 信息流游标。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record FeedCursor(
        /** 对外游标令牌。 */
        String cursorToken,
        /** 上一个排序权重。 */
        Integer lastFeedPriority,
        /** 上一个主键ID。 */
        Long lastSequenceId,
        /** 上一个视频标识。 */
        String lastVideoId,
        /** 游标生成时间。 */
        LocalDateTime generatedAt
) {

    /**
     * 是否包含有效位置。
     */
    public boolean hasPosition() {
        return lastFeedPriority != null && lastSequenceId != null && lastSequenceId > 0;
    }
}
