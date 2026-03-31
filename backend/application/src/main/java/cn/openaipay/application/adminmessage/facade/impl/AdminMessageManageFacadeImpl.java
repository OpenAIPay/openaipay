package cn.openaipay.application.adminmessage.facade.impl;

import cn.openaipay.application.adminmessage.dto.AdminContactBlacklistRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminContactFriendshipRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminContactRequestRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminConversationDetailDTO;
import cn.openaipay.application.adminmessage.dto.AdminConversationRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminMessageOverviewDTO;
import cn.openaipay.application.adminmessage.dto.AdminMessageRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminRedPacketRowDTO;
import cn.openaipay.application.adminmessage.facade.AdminMessageManageFacade;
import cn.openaipay.application.adminmessage.service.AdminMessageManageService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 消息中心门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Service
public class AdminMessageManageFacadeImpl implements AdminMessageManageFacade {

    private final AdminMessageManageService adminMessageManageService;

    public AdminMessageManageFacadeImpl(AdminMessageManageService adminMessageManageService) {
        this.adminMessageManageService = adminMessageManageService;
    }

    @Override
    public AdminMessageOverviewDTO overview() {
        return adminMessageManageService.overview();
    }

    @Override
    public List<AdminConversationRowDTO> listConversations(String keyword, Long userId, Integer pageNo, Integer pageSize) {
        return adminMessageManageService.listConversations(keyword, userId, pageNo, pageSize);
    }

    @Override
    public AdminConversationDetailDTO getConversationDetail(String conversationNo) {
        return adminMessageManageService.getConversationDetail(conversationNo);
    }

    @Override
    public List<AdminMessageRowDTO> listMessages(String conversationNo,
                                                 String messageType,
                                                 Long senderUserId,
                                                 Long receiverUserId,
                                                 Integer pageNo,
                                                 Integer pageSize) {
        return adminMessageManageService.listMessages(conversationNo, messageType, senderUserId, receiverUserId, pageNo, pageSize);
    }

    @Override
    public List<AdminRedPacketRowDTO> listRedPackets(String redPacketNo,
                                                     Long senderUserId,
                                                     Long receiverUserId,
                                                     String status,
                                                     Integer pageNo,
                                                     Integer pageSize) {
        return adminMessageManageService.listRedPackets(redPacketNo, senderUserId, receiverUserId, status, pageNo, pageSize);
    }

    @Override
    public List<AdminContactRequestRowDTO> listContactRequests(String requestNo,
                                                               Long requesterUserId,
                                                               Long targetUserId,
                                                               String status,
                                                               Integer pageNo,
                                                               Integer pageSize) {
        return adminMessageManageService.listContactRequests(requestNo, requesterUserId, targetUserId, status, pageNo, pageSize);
    }

    @Override
    public List<AdminContactFriendshipRowDTO> listFriendships(Long ownerUserId, Long friendUserId, Integer pageNo, Integer pageSize) {
        return adminMessageManageService.listFriendships(ownerUserId, friendUserId, pageNo, pageSize);
    }

    @Override
    public List<AdminContactBlacklistRowDTO> listBlacklists(Long ownerUserId, Long blockedUserId, Integer pageNo, Integer pageSize) {
        return adminMessageManageService.listBlacklists(ownerUserId, blockedUserId, pageNo, pageSize);
    }
}
