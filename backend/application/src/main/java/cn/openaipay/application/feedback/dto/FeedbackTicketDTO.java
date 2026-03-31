package cn.openaipay.application.feedback.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 反馈单数据传输对象。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
public record FeedbackTicketDTO(
        /** 业务单号 */
        String feedbackNo,
        /** 用户ID */
        String userId,
        /** 昵称 */
        String nickname,
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
        List<String> attachmentUrls,
        /** 状态编码 */
        String status,
        /** handledBY信息 */
        String handledBy,
        /** 处理备注 */
        String handleNote,
        /** 业务时间 */
        LocalDateTime handledAt,
        /** 业务时间 */
        LocalDateTime closedAt,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
