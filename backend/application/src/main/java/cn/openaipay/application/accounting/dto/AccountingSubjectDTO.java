package cn.openaipay.application.accounting.dto;

import java.time.LocalDateTime;

/**
 * 会计科目DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record AccountingSubjectDTO(
        /** 科目编码 */
        String subjectCode,
        /** 科目名称 */
        String subjectName,
        /** 科目类型 */
        String subjectType,
        /** 余额方向 */
        String balanceDirection,
        /** 科目编码 */
        String parentSubjectCode,
        /** 业务单号 */
        Integer levelNo,
        /** 启用标记 */
        boolean enabled,
        /** 备注 */
        String remark,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
