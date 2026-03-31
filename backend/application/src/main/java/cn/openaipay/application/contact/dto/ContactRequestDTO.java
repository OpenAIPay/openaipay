package cn.openaipay.application.contact.dto;

import java.time.LocalDateTime;

/**
 * 好友申请数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record ContactRequestDTO(
        /** 请求幂等号 */
        String requestNo,
        /** 申请方用户ID */
        Long requesterUserId,
        /** 目标用户ID */
        Long targetUserId,
        /** 申请方昵称 */
        String requesterNickname,
        /** 申请方脱敏实名 */
        String requesterMaskedRealName,
        /** 申请方脱敏手机号 */
        String requesterMobileMasked,
        /** 申请方头像地址 */
        String requesterAvatarUrl,
        /** 业务说明 */
        String applyMessage,
        /** 状态编码 */
        String status,
        /** BY用户ID */
        Long handledByUserId,
        /** 业务时间 */
        LocalDateTime handledAt,
        /** 记录创建时间 */
        LocalDateTime createdAt
) {
}
