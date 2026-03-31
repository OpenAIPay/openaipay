package cn.openaipay.application.adminmessage.dto;

import java.util.List;

/**
 * 会话详情
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminConversationDetailDTO(
        /** 会话信息 */
        AdminConversationRowDTO conversation,
        /** 成员列表 */
        List<AdminConversationMemberRowDTO> members,
        /** 最近消息列表 */
        List<AdminMessageRowDTO> recentMessages
) {
}
