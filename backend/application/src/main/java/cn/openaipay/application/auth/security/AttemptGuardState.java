package cn.openaipay.application.auth.security;

/**
 * 限流守卫状态模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AttemptGuardState(
        /** 作用域。 */
        String scope,
        /** 主体键。 */
        String principal,
        /** 当前窗口计数。 */
        int attemptCount,
        /** 最近尝试时间戳（毫秒）。 */
        long lastAttemptAtEpochMs,
        /** 锁定到期时间戳（毫秒）。 */
        long lockUntilEpochMs
) {
}

