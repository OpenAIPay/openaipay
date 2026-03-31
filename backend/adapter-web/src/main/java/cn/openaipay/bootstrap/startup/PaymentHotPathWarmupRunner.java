package cn.openaipay.bootstrap.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 预热充值链路的关键 JDBC 访问，降低服务启动后的首笔请求抖动。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Component
public class PaymentHotPathWarmupRunner implements ApplicationRunner {
    /** LOG信息 */
    private static final Logger log = LoggerFactory.getLogger(PaymentHotPathWarmupRunner.class);

    /** 模板信息 */
    private final JdbcTemplate jdbcTemplate;
    /** 启用标记 */
    private final boolean enabled;
    /** 用户ID */
    private final Long warmupUserId;

    public PaymentHotPathWarmupRunner(JdbcTemplate jdbcTemplate,
                                      @Value("${aipay.warmup.enabled:true}") boolean enabled,
                                      @Value("${aipay.warmup.user-id:880100068483692100}") Long warmupUserId) {
        this.jdbcTemplate = jdbcTemplate;
        this.enabled = enabled;
        this.warmupUserId = warmupUserId;
    }

    /**
     * 执行业务数据。
     */
    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        long start = System.nanoTime();
        try {
            jdbcTemplate.execute("SELECT 1");
            jdbcTemplate.queryForList(
                    "SELECT rule_code FROM pricing_rule WHERE status = 'ACTIVE' ORDER BY priority DESC, updated_at DESC LIMIT 4"
            );
            jdbcTemplate.queryForList(
                    "SELECT card_no, bank_code FROM bank_card WHERE user_id = ? ORDER BY id ASC LIMIT 2",
                    warmupUserId
            );
            jdbcTemplate.queryForList(
                    "SELECT user_id, available_balance, reserved_balance FROM wallet_account WHERE user_id = ? LIMIT 1",
                    warmupUserId
            );
            warmLatest("trade_order", "trade_order_no");
            warmLatest("pay_order", "pay_order_no");
            warmLatest("pay_participant_branch", "branch_id");
            warmLatest("inbound_order", "inbound_id");
            warmLatest("wallet_tcc_transaction", "branch_id");
            long costMs = (System.nanoTime() - start) / 1_000_000;
            String scene = "启动预热";
            String request = "warmupUserId=" + warmupUserId + ", costMs=" + costMs;
            log.info("[{}]入参：{}", scene, request);
        } catch (RuntimeException ex) {
            long costMs = (System.nanoTime() - start) / 1_000_000;
            log.warn("payment hot path warmup failed costMs={} message={}", costMs, ex.getMessage());
        }
    }

    private void warmLatest(String tableName, String columnName) {
        jdbcTemplate.queryForList(
                "SELECT " + columnName + " FROM " + tableName + " ORDER BY id DESC LIMIT 1"
        );
    }
}
