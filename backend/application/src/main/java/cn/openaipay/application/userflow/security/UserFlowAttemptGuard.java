package cn.openaipay.application.userflow.security;

import cn.openaipay.application.auth.exception.TooManyRequestsException;
import cn.openaipay.application.auth.security.AttemptGuardState;
import cn.openaipay.application.auth.security.AttemptGuardStateStore;
import java.time.Duration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户注册流程频率守卫。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Component
public class UserFlowAttemptGuard {

    /** 注册校验最大次数。 */
    private static final int REGISTER_CHECK_MAX_ATTEMPTS = 24;
    /** 注册校验窗口。 */
    private static final Duration REGISTER_CHECK_WINDOW = Duration.ofMinutes(10);
    /** 注册校验锁定时长。 */
    private static final Duration REGISTER_CHECK_LOCK_DURATION = Duration.ofMinutes(10);

    /** 注册提交最大次数。 */
    private static final int REGISTER_SUBMIT_MAX_ATTEMPTS = 8;
    /** 注册提交窗口。 */
    private static final Duration REGISTER_SUBMIT_WINDOW = Duration.ofMinutes(15);
    /** 注册提交锁定时长。 */
    private static final Duration REGISTER_SUBMIT_LOCK_DURATION = Duration.ofMinutes(15);

    /** 注册校验限流作用域。 */
    private static final String REGISTER_CHECK_SCOPE = "REGISTER_CHECK";
    /** 注册提交限流作用域。 */
    private static final String REGISTER_SUBMIT_SCOPE = "REGISTER_SUBMIT";

    /** 限流状态存储。 */
    private final AttemptGuardStateStore attemptGuardStateStore;

    public UserFlowAttemptGuard(AttemptGuardStateStore attemptGuardStateStore) {
        this.attemptGuardStateStore = attemptGuardStateStore;
    }

    /**
     * 注册前检查是否允许继续调用。
     */
    @Transactional
    public void checkRegisterCheckAllowed(String loginId) {
        checkAllowed(REGISTER_CHECK_SCOPE, normalizeLoginId(loginId), REGISTER_CHECK_WINDOW);
    }

    /**
     * 记录一次注册校验请求。
     */
    @Transactional
    public void recordRegisterCheckAttempt(String loginId) {
        recordAttempt(
                REGISTER_CHECK_SCOPE,
                normalizeLoginId(loginId),
                REGISTER_CHECK_WINDOW,
                REGISTER_CHECK_MAX_ATTEMPTS,
                REGISTER_CHECK_LOCK_DURATION
        );
    }

    /**
     * 注册提交前检查是否允许继续调用。
     */
    @Transactional
    public void checkRegisterSubmitAllowed(String loginId) {
        checkAllowed(REGISTER_SUBMIT_SCOPE, normalizeLoginId(loginId), REGISTER_SUBMIT_WINDOW);
    }

    /**
     * 记录一次注册提交请求。
     */
    @Transactional
    public void recordRegisterSubmitAttempt(String loginId) {
        recordAttempt(
                REGISTER_SUBMIT_SCOPE,
                normalizeLoginId(loginId),
                REGISTER_SUBMIT_WINDOW,
                REGISTER_SUBMIT_MAX_ATTEMPTS,
                REGISTER_SUBMIT_LOCK_DURATION
        );
    }

    /**
     * 注册成功后清空提交限流状态。
     */
    @Transactional
    public void clearRegisterSubmitAttempt(String loginId) {
        String normalizedLoginId = normalizeLoginId(loginId);
        if (normalizedLoginId == null) {
            return;
        }
        attemptGuardStateStore.delete(REGISTER_SUBMIT_SCOPE, normalizedLoginId);
    }

    private void checkAllowed(String scope, String principal, Duration window) {
        if (principal == null) {
            return;
        }
        AttemptGuardState state = attemptGuardStateStore.findForUpdate(scope, principal).orElse(null);
        if (state == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (state.lockUntilEpochMs() > now) {
            throw new TooManyRequestsException("请求过于频繁，请稍后再试");
        }
        if (state.lastAttemptAtEpochMs() + window.toMillis() <= now) {
            attemptGuardStateStore.delete(scope, principal);
        }
    }

    private void recordAttempt(String scope,
                               String principal,
                               Duration window,
                               int maxAttempts,
                               Duration lockDuration) {
        if (principal == null) {
            return;
        }
        AttemptGuardState state = attemptGuardStateStore.findForUpdate(scope, principal)
                .orElse(new AttemptGuardState(scope, principal, 0, 0L, 0L));
        long now = System.currentTimeMillis();
        int attemptCount = state.attemptCount();
        if (state.lastAttemptAtEpochMs() + window.toMillis() <= now) {
            attemptCount = 0;
        }
        attemptCount += 1;
        long lockUntil = state.lockUntilEpochMs();
        if (attemptCount >= maxAttempts) {
            lockUntil = now + lockDuration.toMillis();
            attemptCount = 0;
        }
        attemptGuardStateStore.save(new AttemptGuardState(scope, principal, attemptCount, now, lockUntil));
    }

    private String normalizeLoginId(String loginId) {
        if (loginId == null) {
            return null;
        }
        String normalized = loginId.trim();
        return normalized.isEmpty() ? null : normalized.toLowerCase();
    }

}
