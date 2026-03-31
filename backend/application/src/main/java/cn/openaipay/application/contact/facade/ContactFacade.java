package cn.openaipay.application.contact.facade;

import cn.openaipay.application.contact.command.ApplyFriendRequestCommand;
import cn.openaipay.application.contact.command.BlockContactCommand;
import cn.openaipay.application.contact.command.HandleFriendRequestCommand;
import cn.openaipay.application.contact.command.UpdateContactRemarkCommand;
import cn.openaipay.application.contact.dto.ContactFriendDTO;
import cn.openaipay.application.contact.dto.ContactRequestDTO;
import cn.openaipay.application.contact.dto.ContactSearchDTO;
import java.util.List;

/**
 * 联系人门面接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface ContactFacade {

    /**
     * 应用请求。
     */
    ContactRequestDTO applyFriendRequest(ApplyFriendRequestCommand command);

    /**
     * 处理请求。
     */
    ContactRequestDTO handleFriendRequest(HandleFriendRequestCommand command);

    /**
     * 查询请求列表。
     */
    List<ContactRequestDTO> listReceivedRequests(Long targetUserId, Integer limit);

    /**
     * 查询请求列表。
     */
    List<ContactRequestDTO> listSentRequests(Long requesterUserId, Integer limit);

    /**
     * 查询业务数据列表。
     */
    List<ContactFriendDTO> listFriends(Long ownerUserId, Integer limit);

    /**
     * 处理搜索联系人信息。
     */
    List<ContactSearchDTO> searchContacts(Long ownerUserId, String keyword, Integer limit);

    /**
     * 更新业务数据。
     */
    void updateRemark(UpdateContactRemarkCommand command);

    /**
     * 删除业务数据。
     */
    void deleteFriend(Long ownerUserId, Long friendUserId);

    /**
     * 处理联系人信息。
     */
    void blockContact(BlockContactCommand command);

    /**
     * 处理联系人信息。
     */
    void unblockContact(Long ownerUserId, Long blockedUserId);

    /**
     * 判断是否业务数据。
     */
    boolean isFriend(Long ownerUserId, Long friendUserId);
}
