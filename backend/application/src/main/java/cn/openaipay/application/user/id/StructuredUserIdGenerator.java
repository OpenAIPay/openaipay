package cn.openaipay.application.user.id;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

/**
 * 18位复合数字用户ID生成器：
 * 88(前缀) + 2位类型码 + 2位分片码 + 11位时序段 + 1位Luhn校验
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class StructuredUserIdGenerator {

    /** ID 前缀。 */
    public static final String PREFIX = "88";
    /** 个人类型编码。 */
    public static final String TYPE_PERSONAL = "01";
    /** 商户类型编码。 */
    public static final String TYPE_MERCHANT = "02";
    /** 代理类型编码。 */
    public static final String TYPE_AGENT = "03";
    /** 系统类型编码。 */
    public static final String TYPE_SYSTEM = "09";

    /** 支持的账户类型集合。 */
    private static final Set<String> SUPPORTED_TYPES = Set.of(
            TYPE_PERSONAL,
            TYPE_MERCHANT,
            TYPE_AGENT,
            TYPE_SYSTEM
    );
    /** 自定义纪元秒数。 */
    private static final long CUSTOM_EPOCH_SECONDS = LocalDateTime.of(2024, 1, 1, 0, 0)
            .toEpochSecond(ZoneOffset.ofHours(8));
    /** 时间片最大秒数。 */
    private static final long MAX_TIME_SEGMENT_SECONDS = 999_999_999L;

    /** 序列锁。 */
    private final Object sequenceLock = new Object();
    /** 上次时间片秒值。 */
    private long lastSecond = -1L;
    /** 每秒序列号。 */
    private int perSecondSequence = -1;

    /**
     * 处理业务数据。
     */
    public long generate(String userTypeCode) {
        String normalizedTypeCode = normalizeUserTypeCode(userTypeCode);
        for (int attempt = 0; attempt < 1_000; attempt++) {
            String sequencePart = nextSequencePart();
            int shardCode = resolveShardCode(normalizedTypeCode, sequencePart);
            if (shardCode < 0) {
                continue;
            }
            String body = PREFIX + normalizedTypeCode + to2Digits(shardCode) + sequencePart;
            int checkDigit = luhnCheckDigit(body);
            long userId = Long.parseLong(body + checkDigit);
            if (userId % 100 != shardCode) {
                continue;
            }
            return userId;
        }
        throw new IllegalStateException("failed to generate structured userId");
    }

    /**
     * 规范化用户类型编码。
     */
    public static String normalizeUserTypeCode(String rawTypeCode) {
        if (rawTypeCode == null || rawTypeCode.isBlank()) {
            return TYPE_PERSONAL;
        }
        String normalized = rawTypeCode.trim();
        if (!SUPPORTED_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("unsupported userTypeCode, expected one of 01/02/03/09");
        }
        return normalized;
    }

    /**
     * 判断是否用户ID。
     */
    public static boolean isStructuredUserId(Long userId) {
        return userId != null && isStructuredUserId(userId.longValue());
    }

    /**
     * 判断是否用户ID。
     */
    public static boolean isStructuredUserId(long userId) {
        if (userId <= 0) {
            return false;
        }
        String raw = Long.toString(userId);
        if (raw.length() != 18 || !raw.startsWith(PREFIX)) {
            return false;
        }
        String typeCode = raw.substring(2, 4);
        if (!SUPPORTED_TYPES.contains(typeCode)) {
            return false;
        }
        String body = raw.substring(0, 17);
        int expectedCheckDigit = luhnCheckDigit(body);
        int actualCheckDigit = raw.charAt(17) - '0';
        if (expectedCheckDigit != actualCheckDigit) {
            return false;
        }
        int shardCode = Integer.parseInt(raw.substring(4, 6));
        return userId % 100 == shardCode;
    }

    private String nextSequencePart() {
        synchronized (sequenceLock) {
            long nowSecond = currentTimeSegmentSeconds();
            if (nowSecond == lastSecond) {
                perSecondSequence++;
                if (perSecondSequence >= 100) {
                    nowSecond = waitNextSecond(nowSecond);
                    perSecondSequence = 0;
                }
            } else {
                lastSecond = nowSecond;
                perSecondSequence = 0;
            }
            long sequenceValue = nowSecond * 100 + perSecondSequence;
            return String.format("%011d", sequenceValue);
        }
    }

    private long waitNextSecond(long currentSecond) {
        long now = currentSecond;
        while (now <= lastSecond) {
            LockSupport.parkNanos(1_000_000L);
            now = currentTimeSegmentSeconds();
        }
        lastSecond = now;
        return now;
    }

    private long currentTimeSegmentSeconds() {
        long now = Instant.now().getEpochSecond() - CUSTOM_EPOCH_SECONDS;
        if (now < 0) {
            return 0;
        }
        if (now > MAX_TIME_SEGMENT_SECONDS) {
            throw new IllegalStateException("time segment overflow for 11-digit sequence part");
        }
        return now;
    }

    private int resolveShardCode(String typeCode, String sequencePart) {
        int shardTens = sequencePart.charAt(sequencePart.length() - 1) - '0';
        for (int shardOnes = 0; shardOnes <= 9; shardOnes++) {
            int shardCode = shardTens * 10 + shardOnes;
            String body = PREFIX + typeCode + to2Digits(shardCode) + sequencePart;
            int checkDigit = luhnCheckDigit(body);
            if (checkDigit == shardOnes) {
                return shardCode;
            }
        }
        return -1;
    }

    private String to2Digits(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }

    private static int luhnCheckDigit(String body) {
        if (body == null || body.isBlank() || !body.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("luhn body must be numeric");
        }
        int sum = 0;
        for (int i = 0; i < body.length(); i++) {
            int digit = body.charAt(i) - '0';
            // 18位目标长度下，body(17位)的奇数位需要做Luhn加权
            if ((i & 1) == 0) {
                int doubled = digit * 2;
                sum += doubled > 9 ? doubled - 9 : doubled;
            } else {
                sum += digit;
            }
        }
        return (10 - (sum % 10)) % 10;
    }
}
