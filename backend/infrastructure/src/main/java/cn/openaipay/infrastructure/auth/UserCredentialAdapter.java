package cn.openaipay.infrastructure.auth;

import cn.openaipay.application.auth.port.UserCredentialPort;
import cn.openaipay.domain.shared.security.CredentialDomainService;
import cn.openaipay.infrastructure.user.dataobject.UserAccountDO;
import cn.openaipay.infrastructure.user.mapper.UserAccountMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 用户凭证写入适配器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Repository
public class UserCredentialAdapter implements UserCredentialPort {

    /** 用户账户持久化接口。 */
    private final UserAccountMapper userAccountMapper;
    /** 凭证领域服务。 */
    private final CredentialDomainService credentialDomainService;

    public UserCredentialAdapter(UserAccountMapper userAccountMapper,
                                 CredentialDomainService credentialDomainService) {
        this.userAccountMapper = userAccountMapper;
        this.credentialDomainService = credentialDomainService;
    }

    /**
     * 初始化登录密码。
     */
    @Override
    public void initializeLoginPassword(Long userId, String rawPassword) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        String normalizedPassword = credentialDomainService.normalizeOptional(rawPassword);
        if (normalizedPassword == null) {
            throw new IllegalArgumentException("rawPassword must not be blank");
        }
        UserAccountDO account = userAccountMapper.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
        account.setLoginPasswordSet(true);
        account.setLoginPasswordSha256(credentialDomainService.sha256(normalizedPassword));
        account.setUpdatedAt(LocalDateTime.now());
        userAccountMapper.updateById(account);
    }
}
