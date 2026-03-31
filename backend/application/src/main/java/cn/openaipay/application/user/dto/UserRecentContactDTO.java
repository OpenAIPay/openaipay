package cn.openaipay.application.user.dto;

/**
 * 用户最近联系人数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record UserRecentContactDTO(
        /** 所属用户ID */
        Long ownerUserId,
        /** 联系人用户ID */
        Long contactUserId,
        /** 联系人平台用户号 */
        String contactAipayUid,
        /** 联系人昵称 */
        String contactNickname,
        /** 联系人展示名称 */
        String contactDisplayName,
        /** 联系人脱敏真实姓名 */
        String contactMaskedRealName,
        /** 联系人头像地址 */
        String contactAvatarUrl,
        /** 联系人展示手机号（脱敏） */
        String contactMobileMasked,
        /** 最近互动场景编码 */
        String interactionSceneCode,
        /** 最近互动备注 */
        String interactionRemark,
        /** 历史互动次数 */
        long interactionCount,
        /** 最近互动时间 */
        String lastInteractionAt
) {
}
