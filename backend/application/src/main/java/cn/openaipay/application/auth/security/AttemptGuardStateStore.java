package cn.openaipay.application.auth.security;

import java.util.Optional;

/**
 * 限流守卫状态存储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public interface AttemptGuardStateStore {

    /**
     * 查询并加锁状态记录。
     */
    Optional<AttemptGuardState> findForUpdate(String scope, String principal);

    /**
     * 保存状态。
     */
    void save(AttemptGuardState state);

    /**
     * 删除状态。
     */
    void delete(String scope, String principal);
}

