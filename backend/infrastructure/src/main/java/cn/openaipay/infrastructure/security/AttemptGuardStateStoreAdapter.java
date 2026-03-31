package cn.openaipay.infrastructure.security;

import cn.openaipay.application.auth.security.AttemptGuardState;
import cn.openaipay.application.auth.security.AttemptGuardStateStore;
import cn.openaipay.infrastructure.security.dataobject.SecurityAttemptGuardDO;
import cn.openaipay.infrastructure.security.mapper.SecurityAttemptGuardMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基于 MySQL 的限流守卫状态存储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Component
public class AttemptGuardStateStoreAdapter implements AttemptGuardStateStore {

    /** 持久化接口。 */
    private final SecurityAttemptGuardMapper securityAttemptGuardMapper;

    public AttemptGuardStateStoreAdapter(SecurityAttemptGuardMapper securityAttemptGuardMapper) {
        this.securityAttemptGuardMapper = securityAttemptGuardMapper;
    }

    @Override
    @Transactional
    public Optional<AttemptGuardState> findForUpdate(String scope, String principal) {
        if (isBlank(scope) || isBlank(principal)) {
            return Optional.empty();
        }
        return securityAttemptGuardMapper.findForUpdate(scope.trim().toUpperCase(), principal.trim().toLowerCase())
                .map(this::toState);
    }

    @Override
    @Transactional
    public void save(AttemptGuardState state) {
        if (state == null || isBlank(state.scope()) || isBlank(state.principal())) {
            return;
        }
        String scope = state.scope().trim().toUpperCase();
        String principal = state.principal().trim().toLowerCase();
        SecurityAttemptGuardDO entity = securityAttemptGuardMapper.findForUpdate(scope, principal)
                .orElseGet(SecurityAttemptGuardDO::new);
        LocalDateTime now = LocalDateTime.now();
        if (entity.getId() == null) {
            entity.setGuardScope(scope);
            entity.setPrincipalKey(principal);
            entity.setCreatedAt(now);
        }
        entity.setAttemptCount(Math.max(0, state.attemptCount()));
        entity.setLastAttemptAt(toLocalDateTime(state.lastAttemptAtEpochMs()));
        entity.setLockUntil(state.lockUntilEpochMs() <= 0 ? null : toLocalDateTime(state.lockUntilEpochMs()));
        entity.setUpdatedAt(now);
        securityAttemptGuardMapper.save(entity);
    }

    @Override
    @Transactional
    public void delete(String scope, String principal) {
        if (isBlank(scope) || isBlank(principal)) {
            return;
        }
        securityAttemptGuardMapper.delete(new QueryWrapper<SecurityAttemptGuardDO>()
                .eq("guard_scope", scope.trim().toUpperCase())
                .eq("principal_key", principal.trim().toLowerCase()));
    }

    private AttemptGuardState toState(SecurityAttemptGuardDO entity) {
        return new AttemptGuardState(
                entity.getGuardScope(),
                entity.getPrincipalKey(),
                entity.getAttemptCount() == null ? 0 : entity.getAttemptCount(),
                toEpochMilli(entity.getLastAttemptAt()),
                toEpochMilli(entity.getLockUntil())
        );
    }

    private LocalDateTime toLocalDateTime(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }

    private long toEpochMilli(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return 0L;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
