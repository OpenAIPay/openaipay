package cn.openaipay.adapter.security;

import cn.openaipay.application.auth.exception.ForbiddenException;
import cn.openaipay.application.auth.exception.UnauthorizedException;
import cn.openaipay.domain.shared.security.CredentialDomainService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * C端用户鉴权拦截器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Component
public class UserSecurityInterceptor implements HandlerInterceptor {

    /** Authorization常量。 */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /** 需要与当前登录用户一致的字段。 */
    private static final Set<String> ASSERTED_USER_ID_KEYS = Set.of(
            "userId",
            "uid",
            "ownerUserId",
            "requesterUserId",
            "operatorUserId",
            "senderUserId",
            "payerUserId"
    );

    /** CredentialDomainService组件。 */
    private final CredentialDomainService credentialDomainService;

    public UserSecurityInterceptor(CredentialDomainService credentialDomainService) {
        this.credentialDomainService = credentialDomainService;
    }

    /**
     * 处理PRE请求。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        Long currentUserId = credentialDomainService
                .resolveSubjectIdFromAuthorizationHeader(request.getHeader(AUTHORIZATION_HEADER));
        if (currentUserId == null || currentUserId <= 0) {
            throw new UnauthorizedException("用户鉴权失败：缺少有效 Bearer Token");
        }
        request.setAttribute(UserRequestContext.ATTR_USER_ID, currentUserId);

        validateUriVariables(request, currentUserId);
        validateQueryParameters(request, currentUserId);
        return true;
    }

    private void validateUriVariables(HttpServletRequest request, Long currentUserId) {
        Object value = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(value instanceof Map<?, ?> uriVariables)) {
            return;
        }
        uriVariables.forEach((rawKey, rawValue) -> {
            if (rawKey == null) {
                return;
            }
            validateAssertedUserId(String.valueOf(rawKey), rawValue, currentUserId);
        });
    }

    private void validateQueryParameters(HttpServletRequest request, Long currentUserId) {
        request.getParameterMap().forEach((key, values) -> {
            if (!ASSERTED_USER_ID_KEYS.contains(key) || values == null) {
                return;
            }
            for (String rawValue : values) {
                validateAssertedUserId(key, rawValue, currentUserId);
            }
        });
    }

    private void validateAssertedUserId(String key, Object rawValue, Long currentUserId) {
        if (!ASSERTED_USER_ID_KEYS.contains(key)) {
            return;
        }
        Long assertedUserId = parseLong(rawValue);
        if (assertedUserId == null || assertedUserId <= 0) {
            throw new UnauthorizedException("用户鉴权失败：用户标识格式错误");
        }
        if (!assertedUserId.equals(currentUserId)) {
            throw new ForbiddenException("用户鉴权失败：请求用户与令牌不匹配");
        }
    }

    private Long parseLong(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof Number number) {
            return number.longValue();
        }
        String text = String.valueOf(rawValue).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
