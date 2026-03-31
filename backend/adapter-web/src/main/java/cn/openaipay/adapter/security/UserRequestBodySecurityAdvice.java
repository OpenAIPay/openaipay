package cn.openaipay.adapter.security;

import cn.openaipay.application.auth.exception.ForbiddenException;
import cn.openaipay.application.auth.exception.UnauthorizedException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/**
 * C端请求体用户身份校验Advice。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@RestControllerAdvice
public class UserRequestBodySecurityAdvice extends RequestBodyAdviceAdapter {

    /** 需要与当前登录用户一致的字段。 */
    private static final Set<String> ASSERTED_USER_ID_KEYS = Set.of(
            "userId",
            "ownerUserId",
            "requesterUserId",
            "operatorUserId",
            "senderUserId",
            "payerUserId"
    );

    /** UserRequestContext组件。 */
    private final UserRequestContext userRequestContext;

    public UserRequestBodySecurityAdvice(UserRequestContext userRequestContext) {
        this.userRequestContext = userRequestContext;
    }

    /**
     * 全量启用。
     */
    @Override
    public boolean supports(MethodParameter methodParameter,
                            java.lang.reflect.Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * 请求体解析后执行用户身份校验。
     */
    @Override
    public Object afterBodyRead(Object body,
                                HttpInputMessage inputMessage,
                                MethodParameter parameter,
                                java.lang.reflect.Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        Long currentUserId = userRequestContext.currentUserId();
        if (currentUserId == null || currentUserId <= 0 || body == null) {
            return body;
        }
        validateBodyAssertedUserId(body, currentUserId);
        return body;
    }

    @SuppressWarnings("unchecked")
    private void validateBodyAssertedUserId(Object body, Long currentUserId) {
        if (body instanceof Map<?, ?> valueMap) {
            ((Map<String, Object>) valueMap).forEach((key, value) ->
                    validateAssertedUserId(key, value, currentUserId));
            return;
        }
        Class<?> bodyClass = body.getClass();
        for (String key : ASSERTED_USER_ID_KEYS) {
            try {
                Method method = bodyClass.getMethod(key);
                Object fieldValue = method.invoke(body);
                validateAssertedUserId(key, fieldValue, currentUserId);
            } catch (NoSuchMethodException ignored) {
                // 忽略不存在字段，避免影响无用户主体字段的请求体。
            } catch (ForbiddenException | UnauthorizedException securityException) {
                throw securityException;
            } catch (Exception reflectionException) {
                throw new IllegalStateException("用户鉴权失败：请求体校验异常", reflectionException);
            }
        }
    }

    private void validateAssertedUserId(String key, Object rawValue, Long currentUserId) {
        if (!ASSERTED_USER_ID_KEYS.contains(key) || rawValue == null) {
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

