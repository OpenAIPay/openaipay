package cn.openaipay.application.message.facade.impl;

import cn.openaipay.application.message.command.SendImageMessageCommand;
import cn.openaipay.application.message.command.SendRedPacketMessageCommand;
import cn.openaipay.application.message.command.SendTextMessageCommand;
import cn.openaipay.application.message.command.SendTransferMessageCommand;
import cn.openaipay.application.message.dto.MessageDTO;
import cn.openaipay.application.message.dto.RedPacketDetailDTO;
import cn.openaipay.application.message.dto.RedPacketHistoryDTO;
import cn.openaipay.application.message.facade.MessageFacade;
import cn.openaipay.application.message.service.MessageService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 消息门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class MessageFacadeImpl implements MessageFacade {

    /** 消息信息 */
    private final MessageService messageService;

    public MessageFacadeImpl(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 处理消息信息。
     */
    @Override
    public MessageDTO sendTextMessage(SendTextMessageCommand command) {
        return messageService.sendTextMessage(command);
    }

    /**
     * 处理消息信息。
     */
    @Override
    public MessageDTO sendImageMessage(SendImageMessageCommand command) {
        return messageService.sendImageMessage(command);
    }

    /**
     * 处理转账消息信息。
     */
    @Override
    public MessageDTO sendTransferMessage(SendTransferMessageCommand command) {
        return messageService.sendTransferMessage(command);
    }

    /**
     * 处理RED红包消息信息。
     */
    @Override
    public MessageDTO sendRedPacketMessage(SendRedPacketMessageCommand command) {
        return messageService.sendRedPacketMessage(command);
    }

    /**
     * 获取RED红包明细信息。
     */
    @Override
    public RedPacketDetailDTO getRedPacketDetail(Long userId, String redPacketNo) {
        return messageService.getRedPacketDetail(userId, redPacketNo);
    }

    /**
     * 处理RED红包信息。
     */
    @Override
    public RedPacketDetailDTO claimRedPacket(Long userId, String redPacketNo) {
        return messageService.claimRedPacket(userId, redPacketNo);
    }

    /**
     * 获取RED红包历史信息。
     */
    @Override
    public RedPacketHistoryDTO getRedPacketHistory(Long userId, String direction, Integer year, Integer limit) {
        return messageService.getRedPacketHistory(userId, direction, year, limit);
    }

    /**
     * 查询会话消息信息列表。
     */
    @Override
    public List<MessageDTO> listConversationMessages(Long userId,
                                                     String conversationNo,
                                                     String beforeMessageId,
                                                     Integer limit) {
        return messageService.listConversationMessages(userId, conversationNo, beforeMessageId, limit);
    }
}
