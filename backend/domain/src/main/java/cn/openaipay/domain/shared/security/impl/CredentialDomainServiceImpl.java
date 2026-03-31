package cn.openaipay.domain.shared.security.impl;

import cn.openaipay.domain.shared.security.CredentialDomainService;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 凭证领域服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class CredentialDomainServiceImpl implements CredentialDomainService {

    /** Bearer前缀常量。 */
    private static final String BEARER_PREFIX = "Bearer ";
    /** Hmac算法常量。 */
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    /** 默认签名盐。 */
    private static final String DEFAULT_TOKEN_SIGNING_SECRET =
            "openaipay-local-signing-secret-please-change";
    /** PBKDF2算法常量。 */
    private static final String PASSWORD_ALGORITHM = "PBKDF2WithHmacSHA256";
    /** PBKDF2密码哈希前缀。 */
    private static final String PASSWORD_HASH_PREFIX = "pbkdf2$";
    /** PBKDF2默认迭代次数。 */
    private static final int PASSWORD_ITERATIONS = 120000;
    /** PBKDF2盐长度（字节）。 */
    private static final int PASSWORD_SALT_BYTES = 16;
    /** PBKDF2输出位数。 */
    private static final int PASSWORD_KEY_LENGTH_BITS = 256;
    /** 默认访问令牌过期秒数（7天）。 */
    private static final long DEFAULT_ACCESS_TOKEN_EXPIRES_IN_SECONDS = 7L * 24 * 3600;
    /** 最小访问令牌过期秒数（1分钟）。 */
    private static final long MIN_ACCESS_TOKEN_EXPIRES_IN_SECONDS = 60L;

    /** utf8编码常量。 */
    private final Charset utf8Charset;
    /** token签名盐。 */
    private final byte[] signingSecretBytes;
    /** 随机组件。 */
    private final SecureRandom secureRandom;
    /** 默认访问令牌过期秒数。 */
    private final long defaultAccessTokenExpiresInSeconds;

    public CredentialDomainServiceImpl() {
        this(DEFAULT_TOKEN_SIGNING_SECRET, DEFAULT_ACCESS_TOKEN_EXPIRES_IN_SECONDS);
    }

    public CredentialDomainServiceImpl(String tokenSigningSecret) {
        this(tokenSigningSecret, DEFAULT_ACCESS_TOKEN_EXPIRES_IN_SECONDS);
    }

    public CredentialDomainServiceImpl(String tokenSigningSecret, long defaultAccessTokenExpiresInSeconds) {
        this.utf8Charset = StandardCharsets.UTF_8;
        String normalizedSecret = normalizeOptional(tokenSigningSecret);
        if (normalizedSecret == null) {
            throw new IllegalArgumentException("tokenSigningSecret must not be blank");
        }
        this.signingSecretBytes = normalizedSecret.getBytes(utf8Charset);
        this.secureRandom = new SecureRandom();
        this.defaultAccessTokenExpiresInSeconds = normalizeExpiresInSeconds(defaultAccessTokenExpiresInSeconds);
    }

    /**
     * 规范化业务数据。
     */
    @Override
    public String normalizeOptional(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 处理SHA256信息。
     */
    @Override
    public boolean matchesSha256(String rawPassword, String storedPasswordSha256) {
        String normalizedRawPassword = normalizeOptional(rawPassword);
        String normalizedStoredHash = normalizeOptional(storedPasswordSha256);
        if (normalizedRawPassword == null || normalizedStoredHash == null) {
            return false;
        }
        if (normalizedStoredHash.startsWith(PASSWORD_HASH_PREFIX)) {
            return verifyPbkdf2Password(normalizedRawPassword, normalizedStoredHash);
        }
        return doSha256(normalizedRawPassword).equalsIgnoreCase(normalizedStoredHash);
    }

    /**
     * 生成密码摘要。
     */
    @Override
    public String sha256(String rawPassword) {
        String normalizedRawPassword = normalizeOptional(rawPassword);
        if (normalizedRawPassword == null) {
            throw new IllegalArgumentException("rawPassword must not be blank");
        }
        return buildPbkdf2PasswordHash(normalizedRawPassword);
    }

    /**
     * 构建令牌信息。
     */
    @Override
    public String buildAccessToken(Long subjectId, String deviceId) {
        return buildAccessToken(subjectId, deviceId, defaultAccessTokenExpiresInSeconds);
    }

    /**
     * 构建令牌信息。
     */
    @Override
    public String buildAccessToken(Long subjectId, String deviceId, long expiresInSeconds) {
        if (subjectId == null || subjectId <= 0) {
            throw new IllegalArgumentException("subjectId must be greater than 0");
        }
        long issuedAtEpochSecond = Instant.now().getEpochSecond();
        long normalizedExpiresInSeconds = normalizeExpiresInSeconds(expiresInSeconds);
        long expireAtEpochSecond = issuedAtEpochSecond + normalizedExpiresInSeconds;
        String payload = subjectId + ":" + issuedAtEpochSecond + ":" + expireAtEpochSecond + ":"
                + normalizeDeviceId(deviceId) + ":" + UUID.randomUUID();
        byte[] payloadBytes = payload.getBytes(utf8Charset);
        byte[] signatureBytes = sign(payloadBytes);
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadBytes);
        String encodedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
        return encodedPayload + "." + encodedSignature;
    }

    /**
     * 从访问令牌中解析主体标识。
     */
    @Override
    public Long resolveSubjectIdFromAccessToken(String accessToken) {
        String normalizedToken = normalizeOptional(accessToken);
        if (normalizedToken == null) {
            return null;
        }
        int dotIndex = normalizedToken.indexOf('.');
        if (dotIndex <= 0 || dotIndex >= normalizedToken.length() - 1) {
            return null;
        }
        String payloadPart = normalizedToken.substring(0, dotIndex);
        String signaturePart = normalizedToken.substring(dotIndex + 1);
        byte[] payloadBytes;
        byte[] signatureBytes;
        try {
            payloadBytes = Base64.getUrlDecoder().decode(payloadPart);
            signatureBytes = Base64.getUrlDecoder().decode(signaturePart);
        } catch (IllegalArgumentException decodeError) {
            return null;
        }
        // Reject non-canonical Base64URL forms to prevent equivalent-byte tampering on trailing bits.
        String canonicalPayloadPart = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadBytes);
        String canonicalSignaturePart = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
        if (!MessageDigest.isEqual(canonicalPayloadPart.getBytes(utf8Charset), payloadPart.getBytes(utf8Charset))
                || !MessageDigest.isEqual(canonicalSignaturePart.getBytes(utf8Charset), signaturePart.getBytes(utf8Charset))) {
            return null;
        }
        byte[] expectedSignature = sign(payloadBytes);
        if (!MessageDigest.isEqual(expectedSignature, signatureBytes)) {
            return null;
        }
        String payload = new String(payloadBytes, utf8Charset);
        String[] parts = payload.split(":", 5);
        if (parts.length < 2) {
            return null;
        }
        String subjectPart = parts[0];
        try {
            Long subjectId = Long.parseLong(subjectPart);
            if (subjectId <= 0) {
                return null;
            }
            if (isExpired(parts)) {
                return null;
            }
            return subjectId;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 从Authorization请求头中解析主体标识。
     */
    @Override
    public Long resolveSubjectIdFromAuthorizationHeader(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        if (token == null) {
            return null;
        }
        return resolveSubjectIdFromAccessToken(token);
    }

    private String doSha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(utf8Charset));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }

    private String normalizeDeviceId(String deviceId) {
        String normalized = normalizeOptional(deviceId);
        return normalized == null ? "unknown-device" : normalized;
    }

    private long normalizeExpiresInSeconds(long expiresInSeconds) {
        long value = expiresInSeconds <= 0 ? defaultAccessTokenExpiresInSeconds : expiresInSeconds;
        return Math.max(MIN_ACCESS_TOKEN_EXPIRES_IN_SECONDS, value);
    }

    private boolean isExpired(String[] payloadParts) {
        long nowEpochSecond = Instant.now().atZone(ZoneOffset.UTC).toEpochSecond();
        try {
            if (payloadParts.length >= 3) {
                long expireAtEpochSecond = Long.parseLong(payloadParts[2]);
                return expireAtEpochSecond <= nowEpochSecond;
            }
            long issuedAtEpochSecond = Long.parseLong(payloadParts[1]);
            return issuedAtEpochSecond + defaultAccessTokenExpiresInSeconds <= nowEpochSecond;
        } catch (NumberFormatException ex) {
            return true;
        }
    }

    private byte[] sign(byte[] payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(signingSecretBytes, HMAC_ALGORITHM));
            return mac.doFinal(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("HMAC signature generation failed", ex);
        }
    }

    private String extractBearerToken(String authorizationHeader) {
        String normalized = normalizeOptional(authorizationHeader);
        if (normalized == null) {
            return null;
        }
        if (normalized.length() <= BEARER_PREFIX.length()) {
            return null;
        }
        if (!normalized.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }
        String token = normalized.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private String buildPbkdf2PasswordHash(String normalizedRawPassword) {
        byte[] salt = new byte[PASSWORD_SALT_BYTES];
        secureRandom.nextBytes(salt);
        byte[] derived = pbkdf2(normalizedRawPassword, salt, PASSWORD_ITERATIONS);
        String encodedSalt = Base64.getUrlEncoder().withoutPadding().encodeToString(salt);
        String encodedHash = Base64.getUrlEncoder().withoutPadding().encodeToString(derived);
        return PASSWORD_HASH_PREFIX + PASSWORD_ITERATIONS + "$" + encodedSalt + "$" + encodedHash;
    }

    private boolean verifyPbkdf2Password(String normalizedRawPassword, String storedHash) {
        String[] segments = storedHash.split("\\$");
        if (segments.length != 4) {
            return false;
        }
        int iterations;
        try {
            iterations = Integer.parseInt(segments[1]);
        } catch (NumberFormatException ex) {
            return false;
        }
        if (iterations <= 0) {
            return false;
        }
        byte[] salt;
        byte[] expected;
        try {
            salt = Base64.getUrlDecoder().decode(segments[2]);
            expected = Base64.getUrlDecoder().decode(segments[3]);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        byte[] actual = pbkdf2(normalizedRawPassword, salt, iterations);
        return MessageDigest.isEqual(expected, actual);
    }

    private byte[] pbkdf2(String rawPassword, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, PASSWORD_KEY_LENGTH_BITS);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PASSWORD_ALGORITHM);
            return keyFactory.generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("PBKDF2 hash generation failed", ex);
        }
    }
}
