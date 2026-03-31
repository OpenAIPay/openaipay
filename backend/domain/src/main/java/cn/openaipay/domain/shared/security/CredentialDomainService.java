package cn.openaipay.domain.shared.security;

/**
 * 凭证领域服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface CredentialDomainService {

    /**
     * 规范化业务数据。
     */
    String normalizeOptional(String text);

    /**
     * 处理SHA256信息。
     */
    boolean matchesSha256(String rawPassword, String storedPasswordSha256);

    /**
     * 生成密码摘要。
     */
    String sha256(String rawPassword);

    /**
     * 构建令牌信息。
     */
    String buildAccessToken(Long subjectId, String deviceId);

    /**
     * 构建带显式过期秒数的令牌信息。
     */
    String buildAccessToken(Long subjectId, String deviceId, long expiresInSeconds);

    /**
     * 从访问令牌中解析主体标识。
     */
    Long resolveSubjectIdFromAccessToken(String accessToken);

    /**
     * 从Authorization请求头中解析主体标识。
     */
    Long resolveSubjectIdFromAuthorizationHeader(String authorizationHeader);
}
