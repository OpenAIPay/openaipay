package cn.openaipay.application.auth.port;

import java.util.Optional;

/**
 * 用户认证查询端口接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface UserAuthQueryPort {

    /**
     * 按登录ID查找记录。
     */
    Optional<UserAuthView> findByLoginId(String loginId);
}
