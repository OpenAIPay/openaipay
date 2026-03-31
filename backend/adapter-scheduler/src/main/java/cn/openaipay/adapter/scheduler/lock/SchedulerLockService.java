package cn.openaipay.adapter.scheduler.lock;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 基于 MySQL 的调度分布式锁服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Component
public class SchedulerLockService {

    /** DB模板。 */
    private final JdbcTemplate jdbcTemplate;
    /** 当前实例标识。 */
    private final String lockedBy;

    public SchedulerLockService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.lockedBy = resolveLockedBy();
    }

    /**
     * 尝试获取锁。
     */
    public boolean tryLock(String lockName, Duration leaseDuration) {
        if (lockName == null || lockName.isBlank()) {
            return false;
        }
        Duration lease = leaseDuration == null || leaseDuration.isNegative() || leaseDuration.isZero()
                ? Duration.ofSeconds(30)
                : leaseDuration;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lockUntil = now.plus(lease);

        int updated = jdbcTemplate.update(
                """
                        UPDATE scheduler_lock
                           SET lock_until = ?, locked_at = ?, locked_by = ?
                         WHERE lock_name = ?
                           AND lock_until <= ?
                        """,
                Timestamp.valueOf(lockUntil),
                Timestamp.valueOf(now),
                lockedBy,
                lockName.trim(),
                Timestamp.valueOf(now)
        );
        if (updated > 0) {
            return true;
        }

        try {
            int inserted = jdbcTemplate.update(
                    """
                            INSERT INTO scheduler_lock(lock_name, lock_until, locked_at, locked_by)
                            VALUES (?, ?, ?, ?)
                            """,
                    lockName.trim(),
                    Timestamp.valueOf(lockUntil),
                    Timestamp.valueOf(now),
                    lockedBy
            );
            return inserted > 0;
        } catch (DuplicateKeyException duplicateKeyException) {
            return false;
        }
    }

    /**
     * 释放锁。
     */
    public void unlock(String lockName) {
        if (lockName == null || lockName.isBlank()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
                """
                        UPDATE scheduler_lock
                           SET lock_until = ?, locked_at = ?, locked_by = ?
                         WHERE lock_name = ?
                           AND locked_by = ?
                        """,
                Timestamp.valueOf(now),
                Timestamp.valueOf(now),
                lockedBy,
                lockName.trim(),
                lockedBy
        );
    }

    private String resolveLockedBy() {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        try {
            String host = InetAddress.getLocalHost().getHostName();
            return host + ":" + pid;
        } catch (UnknownHostException ignored) {
            return "unknown-host:" + pid;
        }
    }
}

