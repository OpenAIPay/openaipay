package cn.openaipay.application.feedback.command;

/**
 * 处理反馈单命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
public record HandleFeedbackCommand(
        /** 业务单号 */
        String feedbackNo,
        /** 状态编码 */
        String status,
        /** handledBY信息 */
        String handledBy,
        /** 处理备注 */
        String handleNote
) {
}
