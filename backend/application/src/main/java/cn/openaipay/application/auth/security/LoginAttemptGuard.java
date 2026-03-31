package cn.openaipay.application.auth.security;

import cn.openaipay.application.auth.exception.UnauthorizedException;
import java.time.Duration;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 登录失败限流守卫。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Component
public class LoginAttemptGuard {

    /** 最大失败次数。 */
    private static final int MAX_FAILED_ATTEMPTS = 5;
    /** 失败统计窗口。 */
    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(15);
    /** 锁定时长。 */
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    /** 登录尝试状态存储。 */
    private final AttemptGuardStateStore attemptGuardStateStore;

    public LoginAttemptGuard(AttemptGuardStateStore attemptGuardStateStore) {
        this.attemptGuardStateStore = attemptGuardStateStore;
    }

    /**
     * 登录前检查是否已被锁定。
     */
    @Transactional
    public void checkAllowed(String scope, String principal) {
        String key = normalizeKey(scope, principal);
        if (key == null) {
            return;
        }
        Optional<AttemptGuardState> stateOptional = attemptGuardStateStore.findForUpdate(scope, key);
        if (stateOptional.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        AttemptGuardState state = stateOptional.get();
        if (state.lockUntilEpochMs() > now) {
            throw new UnauthorizedException("登录失败次数过多，请稍后再试");
        }
        if (state.lastAttemptAtEpochMs() + FAILURE_WINDOW.toMillis() <= now) {
            attemptGuardStateStore.delete(scope, key);
        }
    }

    /**
     * 记录登录失败。
     */
    @Transactional
    public void recordFailure(String scope, String principal) {
        String key = normalizeKey(scope, principal);
        if (key == null) {
            return;
        }
        AttemptGuardState state = attemptGuardStateStore.findForUpdate(scope, key)
                .orElse(new AttemptGuardState(scope, key, 0, 0L, 0L));
        long now = System.currentTimeMillis();
        int failedAttempts = state.attemptCount();
        if (state.lastAttemptAtEpochMs() + FAILURE_WINDOW.toMillis() <= now) {
            failedAttempts = 0;
        }
        failedAttempts += 1;
        long lockUntil = state.lockUntilEpochMs();
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockUntil = now + LOCK_DURATION.toMillis();
            failedAttempts = 0;
        }
        attemptGuardStateStore.save(new AttemptGuardState(scope, key, failedAttempts, now, lockUntil));
    }

    /**
     * 记录登录成功。
     */
    @Transactional
    public void recordSuccess(String scope, String principal) {
        String key = normalizeKey(scope, principal);
        if (key == null) {
            return;
        }
        attemptGuardStateStore.delete(scope, key);
    }

    private String normalizeKey(String scope, String principal) {
        if (scope == null || scope.isBlank() || principal == null || principal.isBlank()) {
            return null;
        }
        return scope.trim().toUpperCase() + ":" + principal.trim().toLowerCase();
    }
}
