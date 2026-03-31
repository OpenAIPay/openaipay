package cn.openaipay.application.conversation.facade.impl;

import cn.openaipay.application.conversation.command.MarkConversationReadCommand;
import cn.openaipay.application.conversation.command.OpenPrivateConversationCommand;
import cn.openaipay.application.conversation.dto.ConversationDTO;
import cn.openaipay.application.conversation.facade.ConversationFacade;
import cn.openaipay.application.conversation.service.ConversationService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 会话门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class ConversationFacadeImpl implements ConversationFacade {

    /** 会话信息 */
    private final ConversationService conversationService;

    public ConversationFacadeImpl(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * 开通会话信息。
     */
    @Override
    public ConversationDTO openPrivateConversation(OpenPrivateConversationCommand command) {
        return conversationService.openPrivateConversation(command);
    }

    /**
     * 查询用户会话信息列表。
     */
    @Override
    public List<ConversationDTO> listUserConversations(Long userId, Integer limit) {
        return conversationService.listUserConversations(userId, limit);
    }

    /**
     * 标记会话信息。
     */
    @Override
    public void markConversationRead(MarkConversationReadCommand command) {
        conversationService.markConversationRead(command);
    }

    /**
     * 处理消息信息。
     */
    @Override
    public void appendMessage(String conversationNo,
                              Long senderUserId,
                              Long receiverUserId,
                              String messageId,
                              String preview,
                              LocalDateTime messageAt) {
        conversationService.appendMessage(
                conversationNo,
                senderUserId,
                receiverUserId,
                messageId,
                preview,
                messageAt
        );
    }

    /**
     * 判断是否存在成员信息。
     */
    @Override
    public boolean hasMember(String conversationNo, Long userId) {
        return conversationService.hasMember(conversationNo, userId);
    }
}
