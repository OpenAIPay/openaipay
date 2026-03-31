package cn.openaipay.domain.accounting.model;

/**
 * 会计科目查询条件。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record AccountingSubjectQuery(
        /** 启用标记 */
        Boolean enabled,
        /** 科目类型 */
        String subjectType,
        /** 查询条数上限 */
        Integer limit
) {
}
