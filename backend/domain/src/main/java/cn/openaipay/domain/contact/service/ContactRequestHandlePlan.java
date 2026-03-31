package cn.openaipay.domain.contact.service;

import cn.openaipay.domain.contact.model.ContactFriendship;
import cn.openaipay.domain.contact.model.ContactRequest;
import java.util.List;

/**
 * 好友申请处理计划。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record ContactRequestHandlePlan(
        /** 请求信息 */
        ContactRequest request,
        /** friendshipsTOcreate信息 */
        List<ContactFriendship> friendshipsToCreate
) {
}
