package cn.openaipay.adapter.user.web.request;
/**
 * 更新用户资料请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record UpdateUserProfileRequest(
        /** 昵称 */
        String nickname,
        /** 头像地址 */
        String avatarUrl,
        /** 手机号 */
        String mobile,
        /** 性别 */
        String gender,
        /** 地区 */
        String region,
        /** 生日 */
        String birthday
) {
}
