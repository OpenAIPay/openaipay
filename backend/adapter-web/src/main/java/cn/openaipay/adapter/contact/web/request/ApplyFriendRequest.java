package cn.openaipay.adapter.contact.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 发送好友申请请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record ApplyFriendRequest(
        /** 申请方用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long requesterUserId,
        /** 目标用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long targetUserId,
        /** 业务说明 */
        String applyMessage
) {
}
