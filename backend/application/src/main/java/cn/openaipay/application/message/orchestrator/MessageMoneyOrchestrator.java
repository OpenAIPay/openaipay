package cn.openaipay.application.message.orchestrator;

import cn.openaipay.application.message.command.SendRedPacketMessageCommand;
import cn.openaipay.application.message.command.SendTransferMessageCommand;
import cn.openaipay.domain.message.model.ChatMessage;
import cn.openaipay.domain.message.model.RedPacketOrder;

/**
 * 消息资金类编排器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface MessageMoneyOrchestrator {

    /**
     * 编排转账消息发送。
     */
    ChatMessage sendTransferMessage(SendTransferMessageCommand command);

    /**
     * 编排红包消息发送。
     */
    ChatMessage sendRedPacketMessage(SendRedPacketMessageCommand command);

    /**
     * 编排红包领取。
     */
    RedPacketOrder claimRedPacket(Long userId, String redPacketNo);
}
