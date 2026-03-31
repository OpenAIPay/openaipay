package cn.openaipay.adapter.security;

import cn.openaipay.application.auth.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * C端用户请求Context模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Component
public class UserRequestContext {

    /** attr用户标识常量。 */
    public static final String ATTR_USER_ID = "user.current.id";

    /** 请求对象。 */
    private final HttpServletRequest request;

    public UserRequestContext(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * 获取当前用户ID。
     */
    public Long currentUserId() {
        Object value = request.getAttribute(ATTR_USER_ID);
        if (value instanceof Long longValue) {
            return longValue;
        }
        return null;
    }

    /**
     * 获取并校验当前用户ID。
     */
    public Long requiredCurrentUserId() {
        Long userId = currentUserId();
        if (userId == null || userId <= 0) {
            throw new UnauthorizedException("用户鉴权失败：缺少有效用户身份");
        }
        return userId;
    }
}

