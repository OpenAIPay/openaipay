package cn.openaipay.domain.contact.model;

/**
 * 联系人搜索结果模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record ContactSearchProfile(
        /** 用户ID */
        Long userId,
        /** 爱支付UID */
        String aipayUid,
        /** 昵称 */
        String nickname,
        /** 头像地址。 */
        String avatarUrl,
        /** 手机号 */
        String mobile,
        /** 脱敏实名。 */
        String maskedRealName,
        /** 是否为好友。 */
        boolean friend,
        /** 是否已拉黑。 */
        boolean blocked,
        /** 备注 */
        String remark
) {
}
