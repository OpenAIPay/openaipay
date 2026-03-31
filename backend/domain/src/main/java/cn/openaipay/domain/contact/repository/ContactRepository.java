package cn.openaipay.domain.contact.repository;

import cn.openaipay.domain.contact.model.ContactFriendship;
import cn.openaipay.domain.contact.model.ContactRequest;
import cn.openaipay.domain.contact.model.ContactSearchProfile;
import java.util.List;
import java.util.Optional;

/**
 * 联系人仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface ContactRepository {

    /**
     * 按请求单号查找请求。
     */
    Optional<ContactRequest> findRequestByRequestNo(String requestNo);

    /**
     * 查找请求。
     */
    Optional<ContactRequest> findPendingRequest(Long requesterUserId, Long targetUserId);

    /**
     * 保存请求。
     */
    ContactRequest saveRequest(ContactRequest request);

    /**
     * 查询请求列表。
     */
    List<ContactRequest> listReceivedRequests(Long targetUserId, int limit);

    /**
     * 查询请求列表。
     */
    List<ContactRequest> listSentRequests(Long requesterUserId, int limit);

    /**
     * 查找业务数据。
     */
    Optional<ContactFriendship> findFriendship(Long ownerUserId, Long friendUserId);

    /**
     * 保存业务数据。
     */
    ContactFriendship saveFriendship(ContactFriendship friendship);

    /**
     * 删除业务数据。
     */
    void deleteFriendship(Long ownerUserId, Long friendUserId);

    /**
     * 查询业务数据列表。
     */
    List<ContactFriendship> listFriendships(Long ownerUserId, int limit);

    /**
     * 判断是否业务数据。
     */
    boolean isBlocked(Long ownerUserId, Long blockedUserId);

    /**
     * 处理业务数据。
     */
    void block(Long ownerUserId, Long blockedUserId, String reason);

    /**
     * 处理业务数据。
     */
    void unblock(Long ownerUserId, Long blockedUserId);

    /**
     * 按用户ID查找资料信息。
     */
    List<ContactSearchProfile> findProfilesByUserIds(Long ownerUserId, List<Long> userIds);

    /**
     * 处理搜索资料信息。
     */
    List<ContactSearchProfile> searchProfiles(Long ownerUserId, String keyword, int limit);
}
