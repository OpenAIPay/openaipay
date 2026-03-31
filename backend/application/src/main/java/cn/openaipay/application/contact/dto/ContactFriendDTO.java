package cn.openaipay.application.contact.dto;

import java.time.LocalDateTime;

/**
 * 好友数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record ContactFriendDTO(
        /** 用户ID */
        Long friendUserId,
        /** 爱支付UID */
        String aipayUid,
        /** 昵称 */
        String nickname,
        /** 业务名称 */
        String maskedRealName,
        /** 手机号信息 */
        String mobileMasked,
        /** 头像地址 */
        String avatarUrl,
        /** 备注 */
        String remark,
        /** 记录创建时间 */
        LocalDateTime createdAt
) {
}
