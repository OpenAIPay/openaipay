package cn.openaipay.application.app.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * App 行为埋点报表 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AppBehaviorEventReportDTO(
        /** 应用编码。 */
        String appCode,
        /** 查询起始时间。 */
        LocalDateTime startAt,
        /** 查询结束时间。 */
        LocalDateTime endAt,
        /** 生成时间。 */
        LocalDateTime generatedAt,
        /** 报表行列表。 */
        List<AppBehaviorEventReportRowDTO> rows
) {
}
