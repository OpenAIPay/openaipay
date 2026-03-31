package cn.openaipay.application.auth.port;

/**
 * 用户凭证写入端口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface UserCredentialPort {

    /**
     * 初始化登录密码。
     */
    void initializeLoginPassword(Long userId, String rawPassword);
}
