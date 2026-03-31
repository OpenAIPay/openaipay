package cn.openaipay.infrastructure.userflow;

import cn.openaipay.application.userflow.port.UserFlowQueryPort;
import cn.openaipay.application.userflow.port.UserRegistrationView;
import cn.openaipay.infrastructure.user.dataobject.UserAccountDO;
import cn.openaipay.infrastructure.user.dataobject.UserProfileDO;
import cn.openaipay.infrastructure.user.mapper.UserAccountMapper;
import cn.openaipay.infrastructure.user.mapper.UserProfileMapper;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 用户流程查询适配器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Repository
public class UserFlowQueryAdapter implements UserFlowQueryPort {

    /** 用户账户持久化接口。 */
    private final UserAccountMapper userAccountMapper;
    /** 用户资料持久化接口。 */
    private final UserProfileMapper userProfileMapper;

    public UserFlowQueryAdapter(UserAccountMapper userAccountMapper, UserProfileMapper userProfileMapper) {
        this.userAccountMapper = userAccountMapper;
        this.userProfileMapper = userProfileMapper;
    }

    /**
     * 按登录账号查询注册信息。
     */
    @Override
    public Optional<UserRegistrationView> findByLoginId(String loginId) {
        return userAccountMapper.findByLoginId(loginId).map(this::toView);
    }

    /**
     * 判断身份证号是否已被注册账号使用。
     */
    @Override
    public boolean existsRegisterAccountByIdCardNo(String idCardNo) {
        List<UserProfileDO> profiles = userProfileMapper.findByIdCardNo(idCardNo);
        if (profiles.isEmpty()) {
            return false;
        }
        Set<Long> userIds = new LinkedHashSet<>();
        profiles.forEach(profile -> {
            if (profile != null && profile.getUserId() != null) {
                userIds.add(profile.getUserId());
            }
        });
        if (userIds.isEmpty()) {
            return false;
        }
        return userAccountMapper.findByUserIds(userIds).stream()
                .anyMatch(account -> "REGISTER".equalsIgnoreCase(account.getAccountSource()));
    }

    private UserRegistrationView toView(UserAccountDO account) {
        return new UserRegistrationView(
                account.getUserId(),
                account.getLoginId(),
                account.getKycLevel()
        );
    }
}
