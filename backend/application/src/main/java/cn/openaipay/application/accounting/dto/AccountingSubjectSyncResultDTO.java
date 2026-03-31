package cn.openaipay.application.accounting.dto;

/**
 * 标准会计科目初始化/重置结果。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record AccountingSubjectSyncResultDTO(
        /** 操作类型 */
        String operation,
        /** 科目次数 */
        int standardSubjectCount,
        /** 业务次数 */
        int addedCount,
        /** 更新次数 */
        int updatedCount,
        /** 业务次数 */
        int disabledCount,
        /** 业务次数 */
        int unchangedCount,
        /** 消息内容 */
        String message
) {
}
