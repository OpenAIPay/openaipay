package cn.openaipay.application.feedback.command;

import java.util.List;

/**
 * 提交反馈单命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
public record SubmitFeedbackCommand(
        /** 用户ID */
        Long userId,
        /** 反馈类型 */
        String feedbackType,
        /** 来源渠道 */
        String sourceChannel,
        /** 来源页面编码 */
        String sourcePageCode,
        /** 标题 */
        String title,
        /** 内容 */
        String content,
        /** 联系人手机号 */
        String contactMobile,
        /** 附件地址列表 */
        List<String> attachmentUrls
) {
}
