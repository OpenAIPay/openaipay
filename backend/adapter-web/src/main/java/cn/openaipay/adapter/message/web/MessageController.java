package cn.openaipay.adapter.message.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.message.web.request.ClaimRedPacketRequest;
import cn.openaipay.adapter.message.web.request.SendImageMessageRequest;
import cn.openaipay.adapter.message.web.request.SendRedPacketMessageRequest;
import cn.openaipay.adapter.message.web.request.SendTextMessageRequest;
import cn.openaipay.adapter.message.web.request.SendTransferMessageRequest;
import cn.openaipay.application.message.command.SendImageMessageCommand;
import cn.openaipay.application.message.command.SendRedPacketMessageCommand;
import cn.openaipay.application.message.command.SendTextMessageCommand;
import cn.openaipay.application.message.command.SendTransferMessageCommand;
import cn.openaipay.application.message.dto.MessageDTO;
import cn.openaipay.application.message.dto.RedPacketDetailDTO;
import cn.openaipay.application.message.dto.RedPacketHistoryDTO;
import cn.openaipay.application.message.facade.MessageFacade;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    /** 消息门面。 */
    private final MessageFacade messageFacade;

    /** 创建消息控制器并注入消息门面。 */
    public MessageController(MessageFacade messageFacade) {
        this.messageFacade = messageFacade;
    }

    /**
     * 处理业务数据。
     */
    @PostMapping("/text")
    public ApiResponse<MessageDTO> sendText(@Valid @RequestBody SendTextMessageRequest request) {
        return ApiResponse.success(messageFacade.sendTextMessage(new SendTextMessageCommand(
                request.senderUserId(),
                request.receiverUserId(),
                request.contentText(),
                request.extPayload()
        )));
    }

    /**
     * 处理业务数据。
     */
    @PostMapping("/image")
    public ApiResponse<MessageDTO> sendImage(@Valid @RequestBody SendImageMessageRequest request) {
        return ApiResponse.success(messageFacade.sendImageMessage(new SendImageMessageCommand(
                request.senderUserId(),
                request.receiverUserId(),
                request.mediaId(),
                request.extPayload()
        )));
    }

    /**
     * 处理转账信息。
     */
    @PostMapping("/transfer")
    public ApiResponse<MessageDTO> sendTransfer(@Valid @RequestBody SendTransferMessageRequest request) {
        return ApiResponse.success(messageFacade.sendTransferMessage(new SendTransferMessageCommand(
                request.senderUserId(),
                request.receiverUserId(),
                request.amount(),
                request.paymentMethod(),
                request.paymentToolCode(),
                request.remark(),
                request.extPayload()
        )));
    }

    /**
     * 处理RED红包信息。
     */
    @PostMapping("/red-packet")
    public ApiResponse<MessageDTO> sendRedPacket(@Valid @RequestBody SendRedPacketMessageRequest request) {
        return ApiResponse.success(messageFacade.sendRedPacketMessage(new SendRedPacketMessageCommand(
                request.senderUserId(),
                request.receiverUserId(),
                request.amount(),
                request.paymentMethod(),
                request.extPayload()
        )));
    }

    /**
     * 获取RED红包明细信息。
     */
    @GetMapping("/red-packets/{redPacketNo}")
    public ApiResponse<RedPacketDetailDTO> getRedPacketDetail(
            @PathVariable("redPacketNo") String redPacketNo,
            @RequestParam("userId") Long userId) {
        return ApiResponse.success(messageFacade.getRedPacketDetail(userId, redPacketNo));
    }

    /**
     * 处理RED红包信息。
     */
    @PostMapping("/red-packets/{redPacketNo}/claim")
    public ApiResponse<RedPacketDetailDTO> claimRedPacket(
            @PathVariable("redPacketNo") String redPacketNo,
            @Valid @RequestBody ClaimRedPacketRequest request) {
        return ApiResponse.success(messageFacade.claimRedPacket(request.userId(), redPacketNo));
    }

    /**
     * 获取RED红包历史信息。
     */
    @GetMapping("/red-packets/history")
    public ApiResponse<RedPacketHistoryDTO> getRedPacketHistory(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "direction", required = false) String direction,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(messageFacade.getRedPacketHistory(userId, direction, year, limit));
    }

    /**
     * 查询会话消息信息列表。
     */
    @GetMapping("/conversations/{conversationNo}")
    public ApiResponse<List<MessageDTO>> listConversationMessages(
            @PathVariable("conversationNo") String conversationNo,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "beforeMessageId", required = false) String beforeMessageId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(messageFacade.listConversationMessages(userId, conversationNo, beforeMessageId, limit));
    }
}
