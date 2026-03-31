package cn.openaipay.bootstrap.startup;

import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时校验关键密钥，防止使用弱默认值上线。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Component
public class SecuritySecretGuardRunner implements ApplicationRunner {

    /** 禁止使用的弱口令集合。 */
    private static final Set<String> WEAK_SECRETS = Set.of(
            "openaipay-local-signing-secret-please-change",
            "openaipay-local-dev-signing-secret-please-change",
            "openaipay",
            "123456",
            "password",
            "root"
    );

    /** token签名密钥。 */
    private final String tokenSigningSecret;
    /** 数据库密码。 */
    private final String dbPassword;
    /** 是否允许弱默认值（仅限紧急本地联调）。 */
    private final boolean allowInsecureDefaults;

    public SecuritySecretGuardRunner(
            @Value("${aipay.security.token-signing-secret}") String tokenSigningSecret,
            @Value("${spring.datasource.password}") String dbPassword,
            @Value("${aipay.security.allow-insecure-defaults:false}") boolean allowInsecureDefaults) {
        this.tokenSigningSecret = tokenSigningSecret;
        this.dbPassword = dbPassword;
        this.allowInsecureDefaults = allowInsecureDefaults;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (allowInsecureDefaults) {
            return;
        }
        validateRequired("aipay.security.token-signing-secret", tokenSigningSecret);
        validateRequired("spring.datasource.password", dbPassword);
        validateStrong("aipay.security.token-signing-secret", tokenSigningSecret, 32);
        validateStrong("spring.datasource.password", dbPassword, 12);
    }

    private void validateRequired(String key, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("missing required secure config: " + key);
        }
    }

    private void validateStrong(String key, String value, int minLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() < minLength) {
            throw new IllegalStateException("weak secure config: " + key + " length must be >= " + minLength);
        }
        if (WEAK_SECRETS.contains(normalized.toLowerCase())) {
            throw new IllegalStateException("weak secure config: " + key + " uses forbidden default");
        }
    }
}
