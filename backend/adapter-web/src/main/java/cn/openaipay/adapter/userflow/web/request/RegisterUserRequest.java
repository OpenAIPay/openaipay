package cn.openaipay.adapter.userflow.web.request;

import java.util.List;

/**
 * 注册用户请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record RegisterUserRequest(
        /** 设备标识 */
        String deviceId,
        /** 旧设备标识列表 */
        List<String> legacyDeviceIds,
        /** 登录账号 */
        String loginId,
        /** 用户类型编码 */
        String userTypeCode,
        /** 账号来源 */
        String accountSource,
        /** 昵称 */
        String nickname,
        /** 头像地址 */
        String avatarUrl,
        /** 国家区号 */
        String countryCode,
        /** 手机号 */
        String mobile,
        /** 真实姓名 */
        String realName,
        /** 身份证号 */
        String idCardNo,
        /** 初始登录密码 */
        String loginPassword
) {
}
