package cn.openaipay.application.auth.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 测试用内存版限流状态存储。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public class InMemoryAttemptGuardStateStore implements AttemptGuardStateStore {

    private final Map<String, AttemptGuardState> map = new HashMap<>();

    @Override
    public Optional<AttemptGuardState> findForUpdate(String scope, String principal) {
        return Optional.ofNullable(map.get(key(scope, principal)));
    }

    @Override
    public void save(AttemptGuardState state) {
        map.put(key(state.scope(), state.principal()), state);
    }

    @Override
    public void delete(String scope, String principal) {
        map.remove(key(scope, principal));
    }

    private String key(String scope, String principal) {
        return scope + "#" + principal;
    }
}
