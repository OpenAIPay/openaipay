package cn.openaipay.domain.conversation.service;

import cn.openaipay.domain.conversation.model.ConversationMember;
import cn.openaipay.domain.conversation.model.ConversationSession;

/**
 * 私聊会话打开计划。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record ConversationOpenPlan(
        /** 业务键 */
        String bizKey,
        /** 会话信息 */
        ConversationSession session,
        /** 成员信息 */
        ConversationMember initiatorMember,
        /** 成员信息 */
        ConversationMember peerMember
) {
}
