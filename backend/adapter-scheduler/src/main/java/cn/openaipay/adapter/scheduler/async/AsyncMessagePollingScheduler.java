package cn.openaipay.adapter.scheduler.async;

import cn.openaipay.adapter.scheduler.lock.SchedulerLockService;
import cn.openaipay.application.outbox.OutboxDispatchService;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 系统内可靠异步消息轮询调度器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Component
public class AsyncMessagePollingScheduler {

    /** LOG信息 */
    private static final Logger log = LoggerFactory.getLogger(AsyncMessagePollingScheduler.class);

    /** 异步消息信息 */
    private final OutboxDispatchService outboxDispatchService;
    /** 分布式锁服务。 */
    private final SchedulerLockService schedulerLockService;
    /** 是否启用调度分布式锁。 */
    private final boolean lockEnabled;
    /** 调度锁租约时长（秒）。 */
    private final long lockLeaseSeconds;

    public AsyncMessagePollingScheduler(OutboxDispatchService outboxDispatchService,
                                        SchedulerLockService schedulerLockService,
                                        @Value("${aipay.scheduler.lock-enabled:true}") boolean lockEnabled,
                                        @Value("${aipay.scheduler.lock-lease-seconds:30}") long lockLeaseSeconds) {
        this.outboxDispatchService = outboxDispatchService;
        this.schedulerLockService = schedulerLockService;
        this.lockEnabled = lockEnabled;
        this.lockLeaseSeconds = Math.max(5L, lockLeaseSeconds);
    }

    /**
     * 轮询处理业务数据。
     */
    @Scheduled(fixedDelayString = "${aipay.async-message.poll-delay-millis:500}")
    public void poll() {
        String lockName = "outbox-polling-dispatch";
        boolean lockAcquired = !lockEnabled || schedulerLockService.tryLock(lockName, Duration.ofSeconds(lockLeaseSeconds));
        if (!lockAcquired) {
            return;
        }
        try {
            int handledCount = outboxDispatchService.dispatchBatch();
            if (handledCount > 0) {
                String scene = "异步消息轮询";
                String request = "handledCount=" + handledCount;
                log.info("[{}]入参：{}", scene, request);
            }
        } finally {
            if (lockEnabled) {
                schedulerLockService.unlock(lockName);
            }
        }
    }
}
