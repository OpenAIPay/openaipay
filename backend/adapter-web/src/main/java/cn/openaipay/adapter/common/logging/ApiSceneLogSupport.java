package cn.openaipay.adapter.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 接口日志场景与请求体构建工具。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public final class ApiSceneLogSupport {

    /** 场景缓存属性。 */
    public static final String ATTR_SCENE = "aipay.request.log.scene";
    /** 请求摘要缓存属性。 */
    public static final String ATTR_REQUEST_PAYLOAD = "aipay.request.log.payload";
    /** 业务异常已记录标记。 */
    public static final String ATTR_BIZ_ERROR_LOGGED = "aipay.request.log.bizErrorLogged";

    /** 最大日志长度。 */
    private static final int MAX_LOG_TEXT_LENGTH = 4000;
    /** 敏感字段关键字。 */
    private static final Set<String> SENSITIVE_KEYWORDS = Set.of(
            "password",
            "pwd",
            "token",
            "secret",
            "authorization",
            "cookie",
            "sign",
            "signkey",
            "idcard",
            "id_card",
            "cardno",
            "card_no"
    );
    /** 手机字段关键字。 */
    private static final Set<String> MOBILE_KEYWORDS = Set.of(
            "mobile",
            "phone",
            "loginid",
            "login_id"
    );
    /** 通用键值对脱敏正则。 */
    private static final Pattern INLINE_SENSITIVE_PATTERN = Pattern.compile(
            "(?i)(\\\"?(?:password|pwd|token|secret|authorization|cookie|sign|signKey|id[_-]?card(?:No)?|card[_-]?no|mobile|phone|loginId|login_id)\\\"?\\s*[:=]\\s*)([^,\\s\\]}&]+)"
    );
    /** 请求属性候选字段。 */
    private static final List<String> SCENE_PROPERTY_CANDIDATES = List.of(
            "scene",
            "sceneCode",
            "businessSceneCode",
            "tradeType",
            "action",
            "operationType"
    );

    /** 场景关键字映射。 */
    private static final Map<String, String> SCENE_KEYWORDS = Map.ofEntries(
            Map.entry("deposit", "充值"),
            Map.entry("recharge", "充值"),
            Map.entry("withdraw", "提现"),
            Map.entry("transfer", "转账"),
            Map.entry("refund", "退款"),
            Map.entry("pay", "支付"),
            Map.entry("cashier", "收银台"),
            Map.entry("message", "消息"),
            Map.entry("conversation", "会话"),
            Map.entry("contact", "联系人"),
            Map.entry("feedback", "反馈"),
            Map.entry("bankcard", "银行卡"),
            Map.entry("credit", "爱花"),
            Map.entry("loan", "爱借"),
            Map.entry("fund", "爱存"),
            Map.entry("admin", "管理后台"),
            Map.entry("user", "用户"),
            Map.entry("auth", "鉴权")
    );

    private ApiSceneLogSupport() {
    }

    /**
     * 解析日志场景。
     */
    public static String resolveScene(HttpServletRequest request, Object requestArg, String fallbackScene) {
        String cachedScene = readText(request == null ? null : request.getAttribute(ATTR_SCENE));
        if (cachedScene != null) {
            return cachedScene;
        }

        String sceneFromArg = resolveSceneFromArg(requestArg);
        if (sceneFromArg != null) {
            return sceneFromArg;
        }

        String sceneFromPath = resolveSceneFromPath(request == null ? null : request.getRequestURI());
        if (sceneFromPath != null) {
            return sceneFromPath;
        }
        return normalizeSceneText(fallbackScene, "通用接口");
    }

    /**
     * 选择请求对象。
     */
    public static Object selectRequestArg(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            if (isSkippableArg(arg)) {
                continue;
            }
            return arg;
        }
        return null;
    }

    /**
     * 构建请求摘要。
     */
    public static String buildRequestPayload(HttpServletRequest request, Object requestArg) {
        Object cachedPayload = request == null ? null : request.getAttribute(ATTR_REQUEST_PAYLOAD);
        if (cachedPayload != null) {
            return truncate(sanitizeInlinePairs(cachedPayload.toString()));
        }

        if (requestArg != null && !isSimpleValue(requestArg)) {
            return truncate(sanitizeInlinePairs(requestArg.toString()));
        }

        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        if (request != null) {
            payload.put("method", request.getMethod());
            payload.put("uri", request.getRequestURI());
            String query = request.getQueryString();
            if (query != null && !query.isBlank()) {
                payload.put("query", sanitizeInlinePairs(query));
            }
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (parameterMap != null && !parameterMap.isEmpty()) {
                payload.put("params", simplifyParams(parameterMap));
            }
        }
        if (requestArg != null) {
            payload.put("arg", sanitizeInlinePairs(requestArg.toString()));
        }
        if (payload.isEmpty()) {
            payload.put("request", "N/A");
        }
        return truncate(sanitizeInlinePairs(payload.toString()));
    }

    /**
     * 标记异常已记录。
     */
    public static void markBizErrorLogged(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        request.setAttribute(ATTR_BIZ_ERROR_LOGGED, Boolean.TRUE);
    }

    /**
     * 判断异常是否已记录。
     */
    public static boolean isBizErrorLogged(HttpServletRequest request) {
        return request != null && Boolean.TRUE.equals(request.getAttribute(ATTR_BIZ_ERROR_LOGGED));
    }

    private static Map<String, Object> simplifyParams(Map<String, String[]> rawParams) {
        LinkedHashMap<String, Object> simplified = new LinkedHashMap<>();
        rawParams.forEach((key, values) -> {
            String normalizedKey = normalizeKey(key);
            if (values == null || values.length == 0) {
                simplified.put(key, "");
                return;
            }
            if (values.length == 1) {
                simplified.put(key, sanitizeValueByKey(normalizedKey, values[0]));
                return;
            }
            List<String> valueList = new ArrayList<>(values.length);
            if (isSensitiveOrMobile(normalizedKey)) {
                Arrays.stream(values).map(value -> sanitizeValueByKey(normalizedKey, value)).forEach(valueList::add);
            } else {
                Arrays.stream(values).forEach(valueList::add);
            }
            simplified.put(key, valueList);
        });
        return simplified;
    }

    private static String resolveSceneFromArg(Object requestArg) {
        if (requestArg == null || isSimpleValue(requestArg)) {
            return null;
        }
        try {
            BeanWrapper wrapper = new BeanWrapperImpl(requestArg);
            for (String property : SCENE_PROPERTY_CANDIDATES) {
                if (!wrapper.isReadableProperty(property)) {
                    continue;
                }
                Object value = wrapper.getPropertyValue(property);
                String resolved = normalizeSceneText(readText(value), null);
                if (resolved != null) {
                    return resolved;
                }
            }
        } catch (RuntimeException ignored) {
            return null;
        }
        return null;
    }

    private static String resolveSceneFromPath(String requestUri) {
        if (requestUri == null || requestUri.isBlank()) {
            return null;
        }
        String normalizedUri = requestUri.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> entry : SCENE_KEYWORDS.entrySet()) {
            if (normalizedUri.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        String compactPath = requestUri.replace("/api/", "")
                .replace('/', '.')
                .replaceAll("^\\.+|\\.+$", "");
        return normalizeSceneText(compactPath, null);
    }

    private static String normalizeSceneText(String raw, String fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String normalized = raw.trim();
        if (normalized.length() > 60) {
            normalized = normalized.substring(0, 60);
        }
        String lowered = normalized.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> entry : SCENE_KEYWORDS.entrySet()) {
            if (lowered.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return normalized;
    }

    private static String readText(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private static boolean isSkippableArg(Object arg) {
        return arg instanceof HttpServletRequest
                || arg instanceof HttpServletResponse
                || arg instanceof BindingResult
                || arg instanceof MultipartFile;
    }

    private static boolean isSimpleValue(Object arg) {
        return arg instanceof CharSequence
                || arg instanceof Number
                || arg instanceof Boolean
                || arg instanceof Enum<?>
                || arg instanceof TemporalAccessor
                || arg.getClass().isPrimitive();
    }

    private static String truncate(String raw) {
        if (raw == null) {
            return "null";
        }
        if (raw.length() <= MAX_LOG_TEXT_LENGTH) {
            return raw;
        }
        return raw.substring(0, MAX_LOG_TEXT_LENGTH) + "...";
    }

    private static String sanitizeInlinePairs(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        Matcher matcher = INLINE_SENSITIVE_PATTERN.matcher(raw);
        StringBuffer buffer = new StringBuffer(raw.length());
        while (matcher.find()) {
            String prefix = matcher.group(1);
            String key = normalizeKey(prefix);
            String replacement = prefix + sanitizeValueByKey(key, matcher.group(2));
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String sanitizeValueByKey(String normalizedKey, String rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (isSensitiveKeyword(normalizedKey)) {
            return "******";
        }
        if (isMobileKeyword(normalizedKey)) {
            return maskMobile(rawValue);
        }
        return rawValue;
    }

    private static boolean isSensitiveOrMobile(String normalizedKey) {
        return isSensitiveKeyword(normalizedKey) || isMobileKeyword(normalizedKey);
    }

    private static boolean isSensitiveKeyword(String normalizedKey) {
        if (normalizedKey == null) {
            return false;
        }
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (normalizedKey.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMobileKeyword(String normalizedKey) {
        if (normalizedKey == null) {
            return false;
        }
        for (String keyword : MOBILE_KEYWORDS) {
            if (normalizedKey.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static String maskMobile(String rawMobile) {
        if (rawMobile == null) {
            return null;
        }
        String trimmed = rawMobile.trim();
        if (trimmed.length() < 7) {
            return "***";
        }
        String prefix = trimmed.substring(0, Math.min(3, trimmed.length()));
        String suffix = trimmed.substring(Math.max(trimmed.length() - 2, 0));
        return prefix + "****" + suffix;
    }

    private static String normalizeKey(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("[^A-Za-z0-9_]", "").toLowerCase(Locale.ROOT);
    }
}
