package cn.openaipay.domain.app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * App 领域模型公共辅助方法。
 *
 * 当前仅服务 iPhone 版本管理模型，不引入 Android / APK 分支。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
final class AppDomainSupport {

    private AppDomainSupport() {
    }

    static String normalizeRequired(String raw, String fieldName) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    static String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    static Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value;
    }

    static Long normalizeNonNegative(Long value, String fieldName) {
        if (value == null) {
            return null;
        }
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }

    static List<String> normalizeTextList(List<String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String raw : rawValues) {
            String text = normalizeOptional(raw);
            if (text != null) {
                normalized.add(text);
            }
        }
        return List.copyOf(new ArrayList<>(normalized));
    }

    static LocalDateTime defaultNow(LocalDateTime value) {
        return value == null ? LocalDateTime.now() : value;
    }

    static String defaultText(String raw, String defaultValue) {
        String normalized = normalizeOptional(raw);
        return normalized == null ? defaultValue : normalized;
    }
}
