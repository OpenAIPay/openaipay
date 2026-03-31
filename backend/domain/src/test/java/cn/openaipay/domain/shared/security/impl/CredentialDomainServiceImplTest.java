package cn.openaipay.domain.shared.security.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * CredentialDomainServiceImpl 单元测试。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
class CredentialDomainServiceImplTest {

    @Test
    void sha256ShouldGeneratePbkdf2AndMatchRawPassword() {
        CredentialDomainServiceImpl service = new CredentialDomainServiceImpl("unit-test-secret");

        String hashed = service.sha256("777444");

        Assertions.assertTrue(hashed.startsWith("pbkdf2$"));
        Assertions.assertTrue(service.matchesSha256("777444", hashed));
        Assertions.assertFalse(service.matchesSha256("wrong-password", hashed));
    }

    @Test
    void matchesSha256ShouldSupportHexSha256Hash() throws Exception {
        CredentialDomainServiceImpl service = new CredentialDomainServiceImpl("unit-test-secret");
        String rawPassword = "777444";
        String legacySha256 = legacySha256(rawPassword);

        Assertions.assertTrue(service.matchesSha256(rawPassword, legacySha256));
        Assertions.assertFalse(service.matchesSha256("wrong-password", legacySha256));
    }

    @Test
    void tokenShouldBeRejectedWhenSignatureIsTampered() {
        CredentialDomainServiceImpl service = new CredentialDomainServiceImpl("unit-test-secret");
        String token = service.buildAccessToken(88010001L, "ios-device");
        Assertions.assertEquals(88010001L, service.resolveSubjectIdFromAccessToken(token));

        int lastIndex = token.length() - 1;
        char replacement = token.charAt(lastIndex) == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, lastIndex) + replacement;
        Assertions.assertNull(service.resolveSubjectIdFromAccessToken(tampered));
    }

    private String legacySha256(String rawPassword) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashBytes);
    }
}
