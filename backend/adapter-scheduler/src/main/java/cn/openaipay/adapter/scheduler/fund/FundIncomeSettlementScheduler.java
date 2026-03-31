package cn.openaipay.adapter.scheduler.fund;

import cn.openaipay.adapter.scheduler.lock.SchedulerLockService;
import cn.openaipay.application.fundaccount.service.FundIncomeDistributionService;
import cn.openaipay.domain.fundaccount.model.FundProductCodes;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 爱存收益定时发放调度器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Component
public class FundIncomeSettlementScheduler {

    /** LOG信息 */
    private static final Logger log = LoggerFactory.getLogger(FundIncomeSettlementScheduler.class);

    /** 收益分发信息 */
    private final FundIncomeDistributionService fundIncomeDistributionService;
    /** 主基金编码 */
    private final String primaryFundCode;
    /** 分布式锁服务。 */
    private final SchedulerLockService schedulerLockService;
    /** 是否启用调度分布式锁。 */
    private final boolean lockEnabled;
    /** 调度锁租约时长（秒）。 */
    private final long lockLeaseSeconds;

    public FundIncomeSettlementScheduler(FundIncomeDistributionService fundIncomeDistributionService,
                                         SchedulerLockService schedulerLockService,
                                         @Value("${aipay.fund.primary-code:AICASH}") String primaryFundCode,
                                         @Value("${aipay.scheduler.lock-enabled:true}") boolean lockEnabled,
                                         @Value("${aipay.scheduler.lock-lease-seconds:30}") long lockLeaseSeconds) {
        this.fundIncomeDistributionService = fundIncomeDistributionService;
        this.schedulerLockService = schedulerLockService;
        this.primaryFundCode = FundProductCodes.normalizeOrDefault(primaryFundCode);
        this.lockEnabled = lockEnabled;
        this.lockLeaseSeconds = Math.max(5L, lockLeaseSeconds);
    }

    /**
     * 处理结算收益信息。
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Shanghai")
    public void settleTodayIncome() {
        String lockName = "fund-income-settlement";
        boolean lockAcquired = !lockEnabled || schedulerLockService.tryLock(lockName, Duration.ofSeconds(lockLeaseSeconds));
        if (!lockAcquired) {
            return;
        }
        try {
            int settledCount = fundIncomeDistributionService.settleTodayIncomeForFundIfNeeded(primaryFundCode);
            if (settledCount > 0) {
                String scene = "收益结算";
                String request = "fundCode=" + primaryFundCode + ", settledCount=" + settledCount;
                log.info("[{}]入参：{}", scene, request);
            }
        } finally {
            if (lockEnabled) {
                schedulerLockService.unlock(lockName);
            }
        }
    }
}
