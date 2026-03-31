package cn.openaipay.application.message.facade;

import cn.openaipay.application.message.command.SendImageMessageCommand;
import cn.openaipay.application.message.command.SendRedPacketMessageCommand;
import cn.openaipay.application.message.command.SendTextMessageCommand;
import cn.openaipay.application.message.command.SendTransferMessageCommand;
import cn.openaipay.application.message.dto.MessageDTO;
import cn.openaipay.application.message.dto.RedPacketDetailDTO;
import cn.openaipay.application.message.dto.RedPacketHistoryDTO;
import java.util.List;

/**
 * 消息门面接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface MessageFacade {

    /**
     * 处理消息信息。
     */
    MessageDTO sendTextMessage(SendTextMessageCommand command);

    /**
     * 处理消息信息。
     */
    MessageDTO sendImageMessage(SendImageMessageCommand command);

    /**
     * 处理转账消息信息。
     */
    MessageDTO sendTransferMessage(SendTransferMessageCommand command);

    /**
     * 处理RED红包消息信息。
     */
    MessageDTO sendRedPacketMessage(SendRedPacketMessageCommand command);

    /**
     * 获取RED红包明细信息。
     */
    RedPacketDetailDTO getRedPacketDetail(Long userId, String redPacketNo);

    /**
     * 处理RED红包信息。
     */
    RedPacketDetailDTO claimRedPacket(Long userId, String redPacketNo);

    /**
     * 获取RED红包历史信息。
     */
    RedPacketHistoryDTO getRedPacketHistory(Long userId, String direction, Integer year, Integer limit);

    /** 查询会话消息列表。 */
    List<MessageDTO> listConversationMessages(Long userId,
                                              String conversationNo,
                                              String beforeMessageId,
                                              Integer limit);
}
