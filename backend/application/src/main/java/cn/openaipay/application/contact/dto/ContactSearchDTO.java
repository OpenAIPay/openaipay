package cn.openaipay.application.contact.dto;

/**
 * 联系人搜索数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record ContactSearchDTO(
        /** 用户ID */
        Long userId,
        /** 爱支付UID */
        String aipayUid,
        /** 昵称 */
        String nickname,
        /** 头像地址 */
        String avatarUrl,
        /** 手机号 */
        String mobile,
        /** 业务名称 */
        String maskedRealName,
        /** 好友标记 */
        boolean friend,
        /** 拉黑标记 */
        boolean blocked,
        /** 备注 */
        String remark
) {
}
