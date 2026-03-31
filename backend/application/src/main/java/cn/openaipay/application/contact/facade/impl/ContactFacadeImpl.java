package cn.openaipay.application.contact.facade.impl;

import cn.openaipay.application.contact.command.ApplyFriendRequestCommand;
import cn.openaipay.application.contact.command.BlockContactCommand;
import cn.openaipay.application.contact.command.HandleFriendRequestCommand;
import cn.openaipay.application.contact.command.UpdateContactRemarkCommand;
import cn.openaipay.application.contact.dto.ContactFriendDTO;
import cn.openaipay.application.contact.dto.ContactRequestDTO;
import cn.openaipay.application.contact.dto.ContactSearchDTO;
import cn.openaipay.application.contact.facade.ContactFacade;
import cn.openaipay.application.contact.service.ContactService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 联系人门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class ContactFacadeImpl implements ContactFacade {

    /** 联系人信息 */
    private final ContactService contactService;

    public ContactFacadeImpl(ContactService contactService) {
        this.contactService = contactService;
    }

    /**
     * 应用请求。
     */
    @Override
    public ContactRequestDTO applyFriendRequest(ApplyFriendRequestCommand command) {
        return contactService.applyFriendRequest(command);
    }

    /**
     * 处理请求。
     */
    @Override
    public ContactRequestDTO handleFriendRequest(HandleFriendRequestCommand command) {
        return contactService.handleFriendRequest(command);
    }

    /**
     * 查询请求列表。
     */
    @Override
    public List<ContactRequestDTO> listReceivedRequests(Long targetUserId, Integer limit) {
        return contactService.listReceivedRequests(targetUserId, limit);
    }

    /**
     * 查询请求列表。
     */
    @Override
    public List<ContactRequestDTO> listSentRequests(Long requesterUserId, Integer limit) {
        return contactService.listSentRequests(requesterUserId, limit);
    }

    /**
     * 查询业务数据列表。
     */
    @Override
    public List<ContactFriendDTO> listFriends(Long ownerUserId, Integer limit) {
        return contactService.listFriends(ownerUserId, limit);
    }

    /**
     * 处理搜索联系人信息。
     */
    @Override
    public List<ContactSearchDTO> searchContacts(Long ownerUserId, String keyword, Integer limit) {
        return contactService.searchContacts(ownerUserId, keyword, limit);
    }

    /**
     * 更新业务数据。
     */
    @Override
    public void updateRemark(UpdateContactRemarkCommand command) {
        contactService.updateRemark(command);
    }

    /**
     * 删除业务数据。
     */
    @Override
    public void deleteFriend(Long ownerUserId, Long friendUserId) {
        contactService.deleteFriend(ownerUserId, friendUserId);
    }

    /**
     * 处理联系人信息。
     */
    @Override
    public void blockContact(BlockContactCommand command) {
        contactService.blockContact(command);
    }

    /**
     * 处理联系人信息。
     */
    @Override
    public void unblockContact(Long ownerUserId, Long blockedUserId) {
        contactService.unblockContact(ownerUserId, blockedUserId);
    }

    /**
     * 判断是否业务数据。
     */
    @Override
    public boolean isFriend(Long ownerUserId, Long friendUserId) {
        return contactService.isFriend(ownerUserId, friendUserId);
    }
}
