package cn.openaipay.application.adminmessage.facade;

import cn.openaipay.application.adminmessage.dto.AdminContactBlacklistRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminContactFriendshipRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminContactRequestRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminConversationDetailDTO;
import cn.openaipay.application.adminmessage.dto.AdminConversationRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminMessageOverviewDTO;
import cn.openaipay.application.adminmessage.dto.AdminMessageRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminRedPacketRowDTO;
import java.util.List;

/**
 * 消息中心门面
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public interface AdminMessageManageFacade {

    /**
     * 查询概览。
     */
    AdminMessageOverviewDTO overview();

    /**
     * 查询会话列表。
     */
    List<AdminConversationRowDTO> listConversations(String keyword, Long userId, Integer pageNo, Integer pageSize);

    /**
     * 查询会话详情。
     */
    AdminConversationDetailDTO getConversationDetail(String conversationNo);

    /**
     * 查询消息列表。
     */
    List<AdminMessageRowDTO> listMessages(String conversationNo,
                                          String messageType,
                                          Long senderUserId,
                                          Long receiverUserId,
                                          Integer pageNo,
                                          Integer pageSize);

    /**
     * 查询红包列表。
     */
    List<AdminRedPacketRowDTO> listRedPackets(String redPacketNo,
                                              Long senderUserId,
                                              Long receiverUserId,
                                              String status,
                                              Integer pageNo,
                                              Integer pageSize);

    /**
     * 查询好友申请列表。
     */
    List<AdminContactRequestRowDTO> listContactRequests(String requestNo,
                                                        Long requesterUserId,
                                                        Long targetUserId,
                                                        String status,
                                                        Integer pageNo,
                                                        Integer pageSize);

    /**
     * 查询好友关系列表。
     */
    List<AdminContactFriendshipRowDTO> listFriendships(Long ownerUserId, Long friendUserId, Integer pageNo, Integer pageSize);

    /**
     * 查询黑名单列表。
     */
    List<AdminContactBlacklistRowDTO> listBlacklists(Long ownerUserId, Long blockedUserId, Integer pageNo, Integer pageSize);
}
