package cn.openaipay.adapter.scheduler.pay;

import cn.openaipay.adapter.scheduler.lock.SchedulerLockService;
import cn.openaipay.application.pay.service.PayReconSweepService;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 支付待对账主单兜底续跑调度器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Component
public class PayReconSweepScheduler {

    /** LOG信息 */
    private static final Logger log = LoggerFactory.getLogger(PayReconSweepScheduler.class);

    /** 支付信息 */
    private final PayReconSweepService payReconSweepService;
    /** 启用标记 */
    private final boolean enabled;
    /** batchsize信息 */
    private final int batchSize;
    /** 分布式锁服务。 */
    private final SchedulerLockService schedulerLockService;
    /** 是否启用调度分布式锁。 */
    private final boolean lockEnabled;
    /** 调度锁租约时长（秒）。 */
    private final long lockLeaseSeconds;

    public PayReconSweepScheduler(PayReconSweepService payReconSweepService,
                                  SchedulerLockService schedulerLockService,
                                  @Value("${aipay.pay.recon-sweep-enabled:true}") boolean enabled,
                                  @Value("${aipay.pay.recon-sweep-batch-size:16}") int batchSize,
                                  @Value("${aipay.scheduler.lock-enabled:true}") boolean lockEnabled,
                                  @Value("${aipay.scheduler.lock-lease-seconds:30}") long lockLeaseSeconds) {
        this.payReconSweepService = payReconSweepService;
        this.schedulerLockService = schedulerLockService;
        this.enabled = enabled;
        this.batchSize = Math.max(1, Math.min(batchSize, 200));
        this.lockEnabled = lockEnabled;
        this.lockLeaseSeconds = Math.max(5L, lockLeaseSeconds);
    }

    /**
     * 扫描处理业务数据。
     */
    @Scheduled(
            fixedDelayString = "${aipay.pay.recon-sweep-delay-millis:45000}",
            initialDelayString = "${aipay.pay.recon-sweep-initial-delay-millis:5000}"
    )
    public void sweep() {
        if (!enabled) {
            return;
        }
        String lockName = "pay-recon-sweep";
        boolean lockAcquired = !lockEnabled || schedulerLockService.tryLock(lockName, Duration.ofSeconds(lockLeaseSeconds));
        if (!lockAcquired) {
            return;
        }
        try {
            int handledCount = payReconSweepService.sweepReconPendingPayments(batchSize);
            if (handledCount > 0) {
                String scene = "支付对账兜底";
                String request = "batchSize=" + batchSize + ", handledCount=" + handledCount;
                log.info("[{}]入参：{}", scene, request);
            }
        } finally {
            if (lockEnabled) {
                schedulerLockService.unlock(lockName);
            }
        }
    }
}
