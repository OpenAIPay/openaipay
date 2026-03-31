package cn.openaipay.application.shared.id;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;
import java.util.regex.Pattern;

/**
 * AiPay分布式ID生成器。
 *
 * 32位数字结构：
 * 1-2:  系统域
 * 3-4:  业务类型
 * 5-18: 时间戳 yyyyMMddHHmmss
 * 19-20: 机器ID
 * 21-24: 用户基因位（userId末4位，左补0）
 * 25-32: 序列位（3位毫秒 + 5位自增）
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Component
public class AiPayIdGenerator {

    /** 交易域编码。 */
    public static final String DOMAIN_TRADE = "10";
    /** 支付域编码。 */
    public static final String DOMAIN_PAY = "20";
    /** 钱包账户域编码。 */
    public static final String DOMAIN_WALLET_ACCOUNT = "21";
    /** 信用账户域编码。 */
    public static final String DOMAIN_CREDIT_ACCOUNT = "22";
    /** 基金账户域编码。 */
    public static final String DOMAIN_FUND_ACCOUNT = "30";
    /** 优惠券域编码。 */
    public static final String DOMAIN_COUPON = "40";
    /** 入金域编码。 */
    public static final String DOMAIN_INBOUND = "60";
    /** 出金域编码。 */
    public static final String DOMAIN_OUTBOUND = "61";
    /** 网关交换域编码，沿用历史 61 号段。 */
    public static final String DOMAIN_GATEWAY = DOMAIN_OUTBOUND;

    /** 单毫秒最大序列数。 */
    private static final int MAX_PER_MILLISECOND = 99_999;
    /** 时间戳格式化器。 */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("uuuuMMddHHmmss", Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);
    /** 完整 ID 格式校验正则。 */
    private static final Pattern FULL_ID_PATTERN = Pattern.compile("^(10|20|21|22|30|40|60|61)\\d{30}$");
    /** 两位数字格式校验正则。 */
    private static final Pattern TWO_DIGITS_PATTERN = Pattern.compile("^\\d{2}$");
    /** 纯数字格式校验正则。 */
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");

    /** 已知业务域集合。 */
    private static final Set<String> KNOWN_DOMAINS = Set.of(
            DOMAIN_TRADE,
            DOMAIN_PAY,
            DOMAIN_WALLET_ACCOUNT,
            DOMAIN_CREDIT_ACCOUNT,
            DOMAIN_FUND_ACCOUNT,
            DOMAIN_COUPON,
            DOMAIN_INBOUND,
            DOMAIN_OUTBOUND
    );

    /** 业务域别名映射。 */
    private static final Map<String, String> DOMAIN_ALIASES = Map.ofEntries(
            Map.entry("trade", DOMAIN_TRADE),
            Map.entry("pay", DOMAIN_PAY),
            Map.entry("wallet", DOMAIN_WALLET_ACCOUNT),
            Map.entry("walletaccount", DOMAIN_WALLET_ACCOUNT),
            Map.entry("wallet_account", DOMAIN_WALLET_ACCOUNT),
            Map.entry("credit", DOMAIN_CREDIT_ACCOUNT),
            Map.entry("creditaccount", DOMAIN_CREDIT_ACCOUNT),
            Map.entry("credit_account", DOMAIN_CREDIT_ACCOUNT),
            Map.entry("loan", DOMAIN_CREDIT_ACCOUNT),
            Map.entry("loanaccount", DOMAIN_CREDIT_ACCOUNT),
            Map.entry("loan_account", DOMAIN_CREDIT_ACCOUNT),
            Map.entry("ailoan", DOMAIN_CREDIT_ACCOUNT),
            Map.entry("fund", DOMAIN_FUND_ACCOUNT),
            Map.entry("fundaccount", DOMAIN_FUND_ACCOUNT),
            Map.entry("fund_account", DOMAIN_FUND_ACCOUNT),
            Map.entry("coupon", DOMAIN_COUPON),
            Map.entry("inbound", DOMAIN_INBOUND),
            Map.entry("outbound", DOMAIN_OUTBOUND),
            Map.entry("gateway", DOMAIN_GATEWAY)
    );

    /** 工作节点ID。 */
    private final String workerId;
    /** 上次毫秒值。 */
    private long lastMillis = -1L;
    /** 序号。 */
    private int sequence = 0;

    public AiPayIdGenerator(@Value("${aipay.id.worker-id:0}") int workerId) {
        if (workerId < 0 || workerId > 99) {
            throw new IllegalArgumentException("workerId must be between 0 and 99");
        }
        this.workerId = String.format(Locale.ROOT, "%02d", workerId);
    }

    /**
     * 生成32位数字ID。
     */
    public synchronized String generate(String domain, String bizType, String userId) {
        String domainCode = normalizeDomain(domain);
        String bizTypeCode = normalizeBizType(bizType);
        String userGene = normalizeUserGene(userId);

        long nowMillis = System.currentTimeMillis();
        if (nowMillis < lastMillis) {
            nowMillis = lastMillis;
        }

        if (nowMillis == lastMillis) {
            sequence++;
            if (sequence > MAX_PER_MILLISECOND) {
                nowMillis = waitNextMillis(lastMillis);
                sequence = 0;
            }
        } else {
            sequence = 0;
        }
        lastMillis = nowMillis;

        String timestamp = formatSecondTimestamp(nowMillis);
        String sequencePart = buildSequencePart(nowMillis, sequence);
        return domainCode + bizTypeCode + timestamp + workerId + userGene + sequencePart;
    }

    /**
     * 校验业务数据。
     */
    public boolean validate(String id) {
        if (id == null || !FULL_ID_PATTERN.matcher(id).matches()) {
            return false;
        }
        String timestamp = id.substring(4, 18);
        try {
            LocalDateTime.parse(timestamp, TIMESTAMP_FORMATTER);
        } catch (DateTimeParseException ex) {
            return false;
        }
        return true;
    }

    private String normalizeDomain(String rawDomain) {
        if (rawDomain == null || rawDomain.isBlank()) {
            throw new IllegalArgumentException("domain must not be blank");
        }
        String trimmed = rawDomain.trim();
        if (KNOWN_DOMAINS.contains(trimmed)) {
            return trimmed;
        }
        String alias = DOMAIN_ALIASES.get(trimmed.toLowerCase(Locale.ROOT));
        if (alias != null) {
            return alias;
        }
        throw new IllegalArgumentException("unsupported domain, expected one of 10/20/21/22/30/40/60/61");
    }

    private String normalizeBizType(String rawBizType) {
        if (rawBizType == null || rawBizType.isBlank()) {
            throw new IllegalArgumentException("bizType must not be blank");
        }
        String bizType = rawBizType.trim();
        if (!TWO_DIGITS_PATTERN.matcher(bizType).matches()) {
            throw new IllegalArgumentException("bizType must be 2 digits");
        }
        return bizType;
    }

    private String normalizeUserGene(String rawUserId) {
        if (rawUserId == null || rawUserId.isBlank()) {
            return "0000";
        }
        String numeric = rawUserId.trim();
        if (!NUMERIC_PATTERN.matcher(numeric).matches()) {
            throw new IllegalArgumentException("userId must be numeric");
        }
        if (numeric.length() >= 4) {
            return numeric.substring(numeric.length() - 4);
        }
        return String.format(Locale.ROOT, "%04d", Integer.parseInt(numeric));
    }

    private String formatSecondTimestamp(long millis) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .format(TIMESTAMP_FORMATTER);
    }

    private String buildSequencePart(long millis, int currentSequence) {
        int milliPart = (int) (millis % 1000);
        return String.format(Locale.ROOT, "%03d%05d", milliPart, currentSequence);
    }

    private long waitNextMillis(long targetMillis) {
        long nowMillis = System.currentTimeMillis();
        while (nowMillis <= targetMillis) {
            LockSupport.parkNanos(100_000L);
            nowMillis = System.currentTimeMillis();
        }
        return nowMillis;
    }
}
