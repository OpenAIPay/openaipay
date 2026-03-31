package cn.openaipay.application.adminmessage.service.impl;

import cn.openaipay.application.adminmessage.dto.AdminContactBlacklistRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminContactFriendshipRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminContactRequestRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminConversationDetailDTO;
import cn.openaipay.application.adminmessage.dto.AdminConversationRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminMessageOverviewDTO;
import cn.openaipay.application.adminmessage.dto.AdminMessageRowDTO;
import cn.openaipay.application.adminmessage.dto.AdminRedPacketRowDTO;
import cn.openaipay.application.adminmessage.port.AdminMessageManagePort;
import cn.openaipay.application.adminmessage.service.AdminMessageManageService;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消息中心服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Service
public class AdminMessageManageServiceImpl implements AdminMessageManageService {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 200;

    private final AdminMessageManagePort adminMessageManagePort;

    public AdminMessageManageServiceImpl(AdminMessageManagePort adminMessageManagePort) {
        this.adminMessageManagePort = adminMessageManagePort;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminMessageOverviewDTO overview() {
        return adminMessageManagePort.overview();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminConversationRowDTO> listConversations(String keyword, Long userId, Integer pageNo, Integer pageSize) {
        return adminMessageManagePort.listConversations(
                normalizeKeyword(keyword),
                normalizeOptionalPositive(userId, "userId"),
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminConversationDetailDTO getConversationDetail(String conversationNo) {
        return adminMessageManagePort.getConversationDetail(requireText(conversationNo, "conversationNo"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminMessageRowDTO> listMessages(String conversationNo,
                                                 String messageType,
                                                 Long senderUserId,
                                                 Long receiverUserId,
                                                 Integer pageNo,
                                                 Integer pageSize) {
        return adminMessageManagePort.listMessages(
                normalizeKeyword(conversationNo),
                normalizeCode(messageType),
                normalizeOptionalPositive(senderUserId, "senderUserId"),
                normalizeOptionalPositive(receiverUserId, "receiverUserId"),
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRedPacketRowDTO> listRedPackets(String redPacketNo,
                                                     Long senderUserId,
                                                     Long receiverUserId,
                                                     String status,
                                                     Integer pageNo,
                                                     Integer pageSize) {
        return adminMessageManagePort.listRedPackets(
                normalizeKeyword(redPacketNo),
                normalizeOptionalPositive(senderUserId, "senderUserId"),
                normalizeOptionalPositive(receiverUserId, "receiverUserId"),
                normalizeCode(status),
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminContactRequestRowDTO> listContactRequests(String requestNo,
                                                               Long requesterUserId,
                                                               Long targetUserId,
                                                               String status,
                                                               Integer pageNo,
                                                               Integer pageSize) {
        return adminMessageManagePort.listContactRequests(
                normalizeKeyword(requestNo),
                normalizeOptionalPositive(requesterUserId, "requesterUserId"),
                normalizeOptionalPositive(targetUserId, "targetUserId"),
                normalizeCode(status),
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminContactFriendshipRowDTO> listFriendships(Long ownerUserId, Long friendUserId, Integer pageNo, Integer pageSize) {
        return adminMessageManagePort.listFriendships(
                normalizeOptionalPositive(ownerUserId, "ownerUserId"),
                normalizeOptionalPositive(friendUserId, "friendUserId"),
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminContactBlacklistRowDTO> listBlacklists(Long ownerUserId, Long blockedUserId, Integer pageNo, Integer pageSize) {
        return adminMessageManagePort.listBlacklists(
                normalizeOptionalPositive(ownerUserId, "ownerUserId"),
                normalizeOptionalPositive(blockedUserId, "blockedUserId"),
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    private Long normalizeOptionalPositive(Long value, String label) {
        if (value == null) {
            return null;
        }
        if (value <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0");
        }
        return value;
    }

    private String requireText(String value, String label) {
        String normalized = normalizeKeyword(value);
        if (normalized == null) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return normalized;
    }

    private String normalizeCode(String raw) {
        String normalized = normalizeKeyword(raw);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeKeyword(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim();
    }

    private int normalizePageNo(Integer pageNo) {
        if (pageNo == null || pageNo <= 0) {
            return DEFAULT_PAGE_NO;
        }
        return pageNo;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
