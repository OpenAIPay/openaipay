package cn.openaipay.application.accounting.command;

/**
 * 冲正会计凭证命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record ReverseAccountingVoucherCommand(
        /** 业务单号 */
        String voucherNo,
        /** 业务原因 */
        String reverseReason,
        /** 操作人 */
        String operator
) {
}
