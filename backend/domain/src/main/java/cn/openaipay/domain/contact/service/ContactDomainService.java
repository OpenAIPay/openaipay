package cn.openaipay.domain.contact.service;

import cn.openaipay.domain.contact.model.ContactRequest;
import java.time.LocalDateTime;

/**
 * 联系人领域服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface ContactDomainService {

    ContactRequest prepareFriendRequest(Long requesterUserId,
                                        Long targetUserId,
                                        boolean blockedByEither,
                                        boolean alreadyFriend,
                                        ContactRequest existingPendingRequest,
                                        String applyMessage,
                                        LocalDateTime now);

    ContactRequestHandlePlan handleRequest(ContactRequest request,
                                           String action,
                                           Long operatorUserId,
                                           LocalDateTime now);

    /**
     * 取消请求用于信息。
     */
    void cancelPendingRequestForBlock(ContactRequest request, Long operatorUserId, LocalDateTime now);
}
