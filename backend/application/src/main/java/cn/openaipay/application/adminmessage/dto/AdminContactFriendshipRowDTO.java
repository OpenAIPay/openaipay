package cn.openaipay.application.adminmessage.dto;

import java.time.LocalDateTime;

/**
 * 好友关系行
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminContactFriendshipRowDTO(
        /** 所属用户ID */
        Long ownerUserId,
        /** 所属展示名称 */
        String ownerDisplayName,
        /** 好友用户ID */
        Long friendUserId,
        /** 好友展示名称 */
        String friendDisplayName,
        /** 备注 */
        String remark,
        /** 来源请求单号 */
        String sourceRequestNo,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
