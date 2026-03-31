package cn.openaipay.application.accounting.command;

/**
 * 保存会计科目命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record SaveAccountingSubjectCommand(
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
        Boolean enabled,
        /** 备注 */
        String remark
) {
}
