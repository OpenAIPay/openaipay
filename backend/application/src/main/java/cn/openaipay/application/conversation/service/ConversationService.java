package cn.openaipay.application.conversation.service;

import cn.openaipay.application.conversation.command.MarkConversationReadCommand;
import cn.openaipay.application.conversation.command.OpenPrivateConversationCommand;
import cn.openaipay.application.conversation.dto.ConversationDTO;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话应用服务接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface ConversationService {

    /**
     * 开通会话信息。
     */
    ConversationDTO openPrivateConversation(OpenPrivateConversationCommand command);

    /**
     * 查询用户会话信息列表。
     */
    List<ConversationDTO> listUserConversations(Long userId, Integer limit);

    /**
     * 标记会话信息。
     */
    void markConversationRead(MarkConversationReadCommand command);

    /** 向会话追加消息。 */
    void appendMessage(String conversationNo,
                       Long senderUserId,
                       Long receiverUserId,
                       String messageId,
                       String preview,
                       LocalDateTime messageAt);

    /**
     * 判断是否存在成员信息。
     */
    boolean hasMember(String conversationNo, Long userId);
}
