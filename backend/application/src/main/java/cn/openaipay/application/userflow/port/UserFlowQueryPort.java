package cn.openaipay.application.userflow.port;

import java.util.Optional;

/**
 * 用户流程查询端口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface UserFlowQueryPort {

    /**
     * 按登录账号查询注册信息。
     */
    Optional<UserRegistrationView> findByLoginId(String loginId);

    /**
     * 判断身份证号是否已被注册账号使用。
     */
    boolean existsRegisterAccountByIdCardNo(String idCardNo);
}
