package cn.openaipay.application.auth.dto;
/**
 * 登录Result数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record LoginResultDTO(
        /** 访问令牌 */
        String accessToken,
        /** 业务类型 */
        String tokenType,
        /** expiresINseconds信息 */
        long expiresInSeconds,
        /** 用户ID */
        Long userId,
        /** 爱支付UID */
        String aipayUid,
        /** 昵称 */
        String nickname
) {
}
