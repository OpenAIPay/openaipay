package cn.openaipay.infrastructure.auth;

import cn.openaipay.application.auth.port.UserAuthQueryPort;
import cn.openaipay.application.auth.port.UserAuthView;
import cn.openaipay.infrastructure.user.dataobject.UserAccountDO;
import cn.openaipay.infrastructure.user.dataobject.UserProfileDO;
import cn.openaipay.infrastructure.user.mapper.UserAccountMapper;
import cn.openaipay.infrastructure.user.mapper.UserProfileMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户认证查询适配器模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class UserAuthQueryAdapter implements UserAuthQueryPort {

    /** User账户Persistence组件 */
    private final UserAccountMapper userAccountMapper;
    /** User资料Persistence组件 */
    private final UserProfileMapper userProfileMapper;

    public UserAuthQueryAdapter(
            UserAccountMapper userAccountMapper,
            UserProfileMapper userProfileMapper) {
        this.userAccountMapper = userAccountMapper;
        this.userProfileMapper = userProfileMapper;
    }

    /**
     * 按登录ID查找记录。
     */
    @Override
    public Optional<UserAuthView> findByLoginId(String loginId) {
        return userAccountMapper.findByLoginId(loginId).map(this::toView);
    }

    private UserAuthView toView(UserAccountDO account) {
        UserProfileDO profile = userProfileMapper.findByUserId(account.getUserId()).orElse(null);
        return new UserAuthView(
                account.getUserId(),
                account.getAipayUid(),
                account.getLoginId(),
                account.getAccountStatus(),
                account.getKycLevel(),
                account.getLoginPasswordSha256(),
                firstNonBlank(profile == null ? null : profile.getNickname(), "爱付用户"),
                profile == null ? null : profile.getAvatarUrl(),
                profile == null ? null : profile.getMobile(),
                profile == null ? null : profile.getMaskedRealName(),
                maskIdCardNo(profile == null ? null : profile.getIdCardNo()),
                profile == null ? null : profile.getCountryCode(),
                profile == null ? null : profile.getGender(),
                profile == null ? null : profile.getRegion()
        );
    }

    private String firstNonBlank(String first, String fallback) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        return fallback;
    }

    private String maskIdCardNo(String idCardNo) {
        if (idCardNo == null || idCardNo.isBlank()) {
            return null;
        }
        String normalized = idCardNo.trim();
        if (normalized.length() <= 8) {
            return normalized;
        }
        return normalized.substring(0, 4) + "*".repeat(Math.max(0, normalized.length() - 8)) + normalized.substring(normalized.length() - 4);
    }
}
