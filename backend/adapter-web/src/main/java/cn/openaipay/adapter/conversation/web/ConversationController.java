package cn.openaipay.adapter.conversation.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.conversation.web.request.MarkConversationReadRequest;
import cn.openaipay.adapter.conversation.web.request.OpenPrivateConversationRequest;
import cn.openaipay.application.conversation.command.MarkConversationReadCommand;
import cn.openaipay.application.conversation.command.OpenPrivateConversationCommand;
import cn.openaipay.application.conversation.dto.ConversationDTO;
import cn.openaipay.application.conversation.facade.ConversationFacade;
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
 * 会话控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    /** 会话门面。 */
    private final ConversationFacade conversationFacade;

    /** 创建会话控制器并注入会话门面。 */
    public ConversationController(ConversationFacade conversationFacade) {
        this.conversationFacade = conversationFacade;
    }

    /**
     * 开通会话信息。
     */
    @PostMapping("/private/open")
    public ApiResponse<ConversationDTO> openPrivateConversation(@Valid @RequestBody OpenPrivateConversationRequest request) {
        return ApiResponse.success(conversationFacade.openPrivateConversation(new OpenPrivateConversationCommand(
                request.userId(),
                request.peerUserId()
        )));
    }

    /**
     * 查询用户会话信息列表。
     */
    @GetMapping("/users/{userId}")
    public ApiResponse<List<ConversationDTO>> listUserConversations(@PathVariable("userId") Long userId,
                                                                    @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(conversationFacade.listUserConversations(userId, limit));
    }

    /**
     * 标记业务数据。
     */
    @PostMapping("/read")
    public ApiResponse<Void> markRead(@Valid @RequestBody MarkConversationReadRequest request) {
        conversationFacade.markConversationRead(new MarkConversationReadCommand(
                request.userId(),
                request.conversationNo(),
                request.lastReadMessageId()
        ));
        return ApiResponse.success(null);
    }
}
