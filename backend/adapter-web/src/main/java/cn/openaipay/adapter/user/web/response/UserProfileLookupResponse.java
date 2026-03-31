package cn.openaipay.adapter.user.web.response;

import cn.openaipay.application.user.dto.UserProfileDTO;

/**
 * 按登录号查询用户资料响应。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record UserProfileLookupResponse(
        String userId,
        String aipayUid,
        String loginId,
        String accountStatus,
        String kycLevel,
        String accountSource,
        String nickname,
        String avatarUrl,
        String countryCode,
        String mobile,
        String maskedRealName,
        String idCardNo,
        String gender,
        String region,
        String birthday
) {

    public static UserProfileLookupResponse from(UserProfileDTO profile) {
        return new UserProfileLookupResponse(
                profile.userId() == null ? null : String.valueOf(profile.userId()),
                profile.aipayUid(),
                profile.loginId(),
                profile.accountStatus(),
                profile.kycLevel(),
                profile.accountSource(),
                profile.nickname(),
                profile.avatarUrl(),
                profile.countryCode(),
                profile.mobile(),
                profile.maskedRealName(),
                profile.idCardNo(),
                profile.gender(),
                profile.region(),
                profile.birthday()
        );
    }
}
