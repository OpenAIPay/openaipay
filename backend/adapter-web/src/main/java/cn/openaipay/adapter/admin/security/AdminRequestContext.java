package cn.openaipay.adapter.admin.security;

import cn.openaipay.application.auth.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 后台管理请求Context模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Component
public class AdminRequestContext {

    /** attr管理标识常量 */
    public static final String ATTR_ADMIN_ID = "admin.current.id";
    /** attr管理用户名常量 */
    public static final String ATTR_ADMIN_USERNAME = "admin.current.username";

    /** 请求 */
    private final HttpServletRequest request;

    public AdminRequestContext(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * 获取当前后台ID。
     */
    public Long currentAdminId() {
        Object value = request.getAttribute(ATTR_ADMIN_ID);
        if (value instanceof Long longValue) {
            return longValue;
        }
        return null;
    }

    /**
     * 获取当前后台用户名信息。
     */
    public String currentAdminUsername() {
        Object value = request.getAttribute(ATTR_ADMIN_USERNAME);
        if (value instanceof String text) {
            return text;
        }
        return null;
    }

    /**
     * 获取并校验后台ID。
     */
    public Long requiredAdminId() {
        Long adminId = currentAdminId();
        if (adminId == null || adminId <= 0) {
            throw new UnauthorizedException("后台鉴权失败：缺少有效管理员标识");
        }
        return adminId;
    }
}
