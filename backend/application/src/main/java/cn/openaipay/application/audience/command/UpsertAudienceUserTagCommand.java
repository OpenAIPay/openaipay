package cn.openaipay.application.audience.command;

import java.time.LocalDateTime;

/**
 * 保存用户标签命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record UpsertAudienceUserTagCommand(
        /** 用户ID */
        Long userId,
        /** 标签编码 */
        String tagCode,
        /** 标签值 */
        String tagValue,
        /** 数据来源 */
        String source,
        /** 标签值更新时间 */
        LocalDateTime valueUpdatedAt
) {
}
