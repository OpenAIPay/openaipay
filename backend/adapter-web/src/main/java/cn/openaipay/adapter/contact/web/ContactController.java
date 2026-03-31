package cn.openaipay.adapter.contact.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.contact.web.request.ApplyFriendRequest;
import cn.openaipay.adapter.contact.web.request.BlockContactRequest;
import cn.openaipay.adapter.contact.web.request.HandleFriendRequest;
import cn.openaipay.adapter.contact.web.request.UpdateContactRemarkRequest;
import cn.openaipay.application.contact.command.ApplyFriendRequestCommand;
import cn.openaipay.application.contact.command.BlockContactCommand;
import cn.openaipay.application.contact.command.HandleFriendRequestCommand;
import cn.openaipay.application.contact.command.UpdateContactRemarkCommand;
import cn.openaipay.application.contact.dto.ContactFriendDTO;
import cn.openaipay.application.contact.dto.ContactRequestDTO;
import cn.openaipay.application.contact.dto.ContactSearchDTO;
import cn.openaipay.application.contact.facade.ContactFacade;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 联系人控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    /** 联系人门面。 */
    private final ContactFacade contactFacade;

    /** 创建联系人控制器并注入联系人门面。 */
    public ContactController(ContactFacade contactFacade) {
        this.contactFacade = contactFacade;
    }

    /**
     * 应用请求。
     */
    @PostMapping("/requests")
    public ApiResponse<ContactRequestDTO> applyRequest(@Valid @RequestBody ApplyFriendRequest request) {
        return ApiResponse.success(contactFacade.applyFriendRequest(new ApplyFriendRequestCommand(
                request.requesterUserId(),
                request.targetUserId(),
                request.applyMessage()
        )));
    }

    /**
     * 处理请求。
     */
    @PostMapping("/requests/{requestNo}/handle")
    public ApiResponse<ContactRequestDTO> handleRequest(@PathVariable("requestNo") String requestNo,
                                                        @Valid @RequestBody HandleFriendRequest request) {
        return ApiResponse.success(contactFacade.handleFriendRequest(new HandleFriendRequestCommand(
                request.operatorUserId(),
                requestNo,
                request.action()
        )));
    }

    /**
     * 查询请求列表。
     */
    @GetMapping("/requests/received")
    public ApiResponse<List<ContactRequestDTO>> listReceivedRequests(
            @RequestParam("targetUserId") Long targetUserId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(contactFacade.listReceivedRequests(targetUserId, limit));
    }

    /**
     * 查询请求列表。
     */
    @GetMapping("/requests/sent")
    public ApiResponse<List<ContactRequestDTO>> listSentRequests(
            @RequestParam("requesterUserId") Long requesterUserId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(contactFacade.listSentRequests(requesterUserId, limit));
    }

    /**
     * 查询业务数据列表。
     */
    @GetMapping("/friends/{ownerUserId}")
    public ApiResponse<List<ContactFriendDTO>> listFriends(@PathVariable("ownerUserId") Long ownerUserId,
                                                           @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(contactFacade.listFriends(ownerUserId, limit));
    }

    /**
     * 处理搜索联系人信息。
     */
    @GetMapping("/search")
    public ApiResponse<List<ContactSearchDTO>> searchContacts(@RequestParam("ownerUserId") Long ownerUserId,
                                                              @RequestParam("keyword") String keyword,
                                                              @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(contactFacade.searchContacts(ownerUserId, keyword, limit));
    }

    /**
     * 更新业务数据。
     */
    @PutMapping("/remark")
    public ApiResponse<Void> updateRemark(@Valid @RequestBody UpdateContactRemarkRequest request) {
        contactFacade.updateRemark(new UpdateContactRemarkCommand(
                request.ownerUserId(),
                request.friendUserId(),
                request.remark()
        ));
        return ApiResponse.success(null);
    }

    /**
     * 删除业务数据。
     */
    @DeleteMapping("/friends")
    public ApiResponse<Void> deleteFriend(@RequestParam("ownerUserId") Long ownerUserId,
                                          @RequestParam("friendUserId") Long friendUserId) {
        contactFacade.deleteFriend(ownerUserId, friendUserId);
        return ApiResponse.success(null);
    }

    /**
     * 处理业务数据。
     */
    @PostMapping("/block")
    public ApiResponse<Void> block(@Valid @RequestBody BlockContactRequest request) {
        contactFacade.blockContact(new BlockContactCommand(
                request.ownerUserId(),
                request.blockedUserId(),
                request.reason()
        ));
        return ApiResponse.success(null);
    }

    /**
     * 处理业务数据。
     */
    @DeleteMapping("/block")
    public ApiResponse<Void> unblock(@RequestParam("ownerUserId") Long ownerUserId,
                                     @RequestParam("blockedUserId") Long blockedUserId) {
        contactFacade.unblockContact(ownerUserId, blockedUserId);
        return ApiResponse.success(null);
    }
}
