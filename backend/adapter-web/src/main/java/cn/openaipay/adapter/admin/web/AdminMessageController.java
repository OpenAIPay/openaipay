package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.adminmessage.dto.AdminContactBlacklistRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminContactFriendshipRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminContactRequestRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminConversationDetailDTO;
import cn.openaipay.application.adminmessage.dto.AdminConversationRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminMessageOverviewDTO;
import cn.openaipay.application.adminmessage.dto.AdminMessageRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminRedPacketRowDTO;
import cn.openaipay.application.adminmessage.facade.AdminMessageManageFacade;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台消息中心控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@RestController
@RequestMapping("/api/admin/messages")
public class AdminMessageController {

    /** 消息中心门面。 */
    private final AdminMessageManageFacade adminMessageManageFacade;

    public AdminMessageController(AdminMessageManageFacade adminMessageManageFacade) {
        this.adminMessageManageFacade = adminMessageManageFacade;
    }

    /**
     * 处理概览信息。
     */
    @GetMapping("/overview")
    @RequireAdminPermission("message.center.view")
    public ApiResponse<AdminMessageOverviewDTO> overview() {
        return ApiResponse.success(adminMessageManageFacade.overview());
    }

    /**
     * 查询会话信息列表。
     */
    @GetMapping("/conversations")
    @RequireAdminPermission("message.center.view")
    public ApiResponse<List<AdminConversationRowDTO>> listConversations(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(adminMessageManageFacade.listConversations(keyword, userId, pageNo, resolvedPageSize));
    }

    /**
     * 获取会话明细信息。
     */
    @GetMapping("/conversations/{conversationNo}")
    @RequireAdminPermission("message.center.view")
    public ApiResponse<AdminConversationDetailDTO> getConversationDetail(@PathVariable("conversationNo") String conversationNo) {
        return ApiResponse.success(adminMessageManageFacade.getConversationDetail(conversationNo));
    }

    /**
     * 查询消息信息列表。
     */
    @GetMapping("/records")
    @RequireAdminPermission("message.center.view")
    public ApiResponse<List<AdminMessageRowDTO>> listMessages(
            @RequestParam(value = "conversationNo", required = false) String conversationNo,
            @RequestParam(value = "messageType", required = false) String messageType,
            @RequestParam(value = "senderUserId", required = false) Long senderUserId,
            @RequestParam(value = "receiverUserId", required = false) Long receiverUserId,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(adminMessageManageFacade.listMessages(
                conversationNo,
                messageType,
                senderUserId,
                receiverUserId,
                pageNo,
                resolvedPageSize
        ));
    }

    /**
     * 查询红包信息列表。
     */
    @GetMapping("/red-packets")
    @RequireAdminPermission("message.center.view")
    public ApiResponse<List<AdminRedPacketRowDTO>> listRedPackets(
            @RequestParam(value = "redPacketNo", required = false) String redPacketNo,
            @RequestParam(value = "senderUserId", required = false) Long senderUserId,
            @RequestParam(value = "receiverUserId", required = false) Long receiverUserId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(adminMessageManageFacade.listRedPackets(
                redPacketNo,
                senderUserId,
                receiverUserId,
                status,
                pageNo,
                resolvedPageSize
        ));
    }

    /**
     * 查询联系人请求列表。
     */
    @GetMapping("/contact-requests")
    @RequireAdminPermission("message.center.view")
    public ApiResponse<List<AdminContactRequestRowDTO>> listContactRequests(
            @RequestParam(value = "requestNo", required = false) String requestNo,
            @RequestParam(value = "requesterUserId", required = false) Long requesterUserId,
            @RequestParam(value = "targetUserId", required = false) Long targetUserId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(adminMessageManageFacade.listContactRequests(
                requestNo,
                requesterUserId,
                targetUserId,
                status,
                pageNo,
                resolvedPageSize
        ));
    }

    /**
     * 查询好友关系列表。
     */
    @GetMapping("/friends")
    @RequireAdminPermission("message.center.view")
    public ApiResponse<List<AdminContactFriendshipRowDTO>> listFriendships(
            @RequestParam(value = "ownerUserId", required = false) Long ownerUserId,
            @RequestParam(value = "friendUserId", required = false) Long friendUserId,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(adminMessageManageFacade.listFriendships(ownerUserId, friendUserId, pageNo, resolvedPageSize));
    }

    /**
     * 查询黑名单信息列表。
     */
    @GetMapping("/blacklist")
    @RequireAdminPermission("message.center.view")
    public ApiResponse<List<AdminContactBlacklistRowDTO>> listBlacklists(
            @RequestParam(value = "ownerUserId", required = false) Long ownerUserId,
            @RequestParam(value = "blockedUserId", required = false) Long blockedUserId,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(adminMessageManageFacade.listBlacklists(ownerUserId, blockedUserId, pageNo, resolvedPageSize));
    }
}
