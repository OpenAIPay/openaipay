package cn.openaipay.domain.trade.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 业务交易查询索引模型。
 *
 * 业务场景：后台和账单中心需要按爱花、爱借、爱存等业务单号直接查询交易，避免再从统一交易主单里反推业务信息。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public class TradeBusinessIndex {
    private static final String MOBILE_HALL_TOP_UP_SCENE = "APP_MOBILE_HALL_TOP_UP";
    private static final Pattern MAINLAND_MOBILE_PATTERN = Pattern.compile("(?<!\\d)(1[3-9]\\d{9})(?!\\d)");

    /** 查询索引主键ID。 */
    private final Long id;
    /** 统一交易主单号。 */
    private final String tradeOrderNo;
    /** 业务域编码，例如 AICREDIT、AILOAN、AICASH。 */
    private final TradeBusinessDomainCode businessDomainCode;
    /** 业务交易单号，在业务域内唯一。 */
    private final String bizOrderNo;
    /** 产品类型，例如 AICREDIT、AILOAN、AICASH。 */
    private final String productType;
    /** 业务类型，例如 REPAY、MINIMUM_REPAY、PURCHASE。 */
    private final String businessType;
    /** 业务所属用户ID。 */
    private final Long userId;
    /** 交易对手用户ID。 */
    private final Long counterpartyUserId;
    /** 业务账户号，例如爱花账户号、爱存账户号。 */
    private final String accountNo;
    /** 账单号。 */
    private final String billNo;
    /** 账单月份，格式如 2026-03。 */
    private final String billMonth;
    /** 展示标题。 */
    private final String displayTitle;
    /** 展示副标题。 */
    private final String displaySubtitle;
    /** 展示金额。 */
    private final Money amount;
    /** 统一交易状态。 */
    private final TradeStatus status;
    /** 业务排序时间。 */
    private final LocalDateTime tradeTime;
    /** 记录创建时间。 */
    private final LocalDateTime createdAt;
    /** 记录更新时间。 */
    private final LocalDateTime updatedAt;

    public TradeBusinessIndex(Long id,
                              String tradeOrderNo,
                              TradeBusinessDomainCode businessDomainCode,
                              String bizOrderNo,
                              String productType,
                              String businessType,
                              Long userId,
                              Long counterpartyUserId,
                              String accountNo,
                              String billNo,
                              String billMonth,
                              String displayTitle,
                              String displaySubtitle,
                              Money amount,
                              TradeStatus status,
                              LocalDateTime tradeTime,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
        this.id = id;
        this.tradeOrderNo = normalizeRequired(tradeOrderNo, "tradeOrderNo");
        this.businessDomainCode = businessDomainCode == null ? TradeBusinessDomainCode.TRADE : businessDomainCode;
        this.bizOrderNo = normalizeRequired(bizOrderNo, "bizOrderNo");
        this.productType = normalizeOptional(productType);
        this.businessType = normalizeOptional(businessType);
        this.userId = requirePositive(userId, "userId");
        this.counterpartyUserId = counterpartyUserId == null ? null : requirePositive(counterpartyUserId, "counterpartyUserId");
        this.accountNo = normalizeOptional(accountNo);
        this.billNo = normalizeOptional(billNo);
        this.billMonth = normalizeOptional(billMonth);
        this.displayTitle = normalizeOptional(displayTitle);
        this.displaySubtitle = normalizeOptional(displaySubtitle);
        this.amount = normalizeAmount(amount);
        this.status = status == null ? TradeStatus.CREATED : status;
        this.tradeTime = tradeTime == null ? LocalDateTime.now() : tradeTime;
        this.createdAt = createdAt == null ? this.tradeTime : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 从统一交易主单构造默认业务查询索引。
     *
     * 业务场景：当前主交易写链路先统一落主单，再异步或补充写具体业务扩展单时，需要先有基础查询索引。
     */
    public static TradeBusinessIndex fromTradeOrder(TradeOrder tradeOrder) {
        if (tradeOrder == null) {
            throw new IllegalArgumentException("tradeOrder must not be null");
        }
        Money displayAmount = tradeOrder.getOriginalAmount();
        return new TradeBusinessIndex(
                null,
                tradeOrder.getTradeOrderNo(),
                TradeBusinessDomainCode.from(tradeOrder.getBusinessDomainCode()),
                tradeOrder.getBizOrderNo(),
                null,
                tradeOrder.getTradeType().name(),
                tradeOrder.getPayerUserId(),
                tradeOrder.getPayeeUserId(),
                null,
                null,
                null,
                resolveDefaultDisplayTitle(tradeOrder),
                tradeOrder.getPaymentMethod(),
                displayAmount,
                tradeOrder.getStatus(),
                tradeOrder.getCreatedAt(),
                tradeOrder.getCreatedAt(),
                tradeOrder.getUpdatedAt()
        );
    }

    private static String resolveDefaultDisplayTitle(TradeOrder tradeOrder) {
        String sceneCode = normalizeOptionalStatic(tradeOrder.getBusinessSceneCode());
        if (sceneCode != null && MOBILE_HALL_TOP_UP_SCENE.equalsIgnoreCase(sceneCode)) {
            String mobile = extractMainlandMobileFromMetadata(tradeOrder.getMetadata());
            if (mobile != null) {
                return "为" + mobile + "话费充值";
            }
        }
        return tradeOrder.getBusinessSceneCode();
    }

    private static String extractMainlandMobileFromMetadata(String metadata) {
        String normalized = normalizeOptionalStatic(metadata);
        if (normalized == null) {
            return null;
        }
        Matcher matcher = MAINLAND_MOBILE_PATTERN.matcher(normalized);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    private static String normalizeOptionalStatic(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeRequired(String raw, String field) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long requirePositive(Long value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0");
        }
        return value;
    }

    private Money normalizeAmount(Money value) {
        CurrencyUnit currency = value == null ? CurrencyUnit.of("CNY") : value.getCurrencyUnit();
        if (value == null) {
            return Money.zero(currency).rounded(2, RoundingMode.HALF_UP);
        }
        if (value.isNegative()) {
            throw new IllegalArgumentException("amount must be greater than or equal to 0");
        }
        return value.rounded(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取交易订单NO信息。
     */
    public String getTradeOrderNo() {
        return tradeOrderNo;
    }

    /**
     * 获取领域编码。
     */
    public TradeBusinessDomainCode getBusinessDomainCode() {
        return businessDomainCode;
    }

    /**
     * 获取业务订单NO信息。
     */
    public String getBizOrderNo() {
        return bizOrderNo;
    }

    /**
     * 获取业务数据。
     */
    public String getProductType() {
        return productType;
    }

    /**
     * 获取业务数据。
     */
    public String getBusinessType() {
        return businessType;
    }

    /**
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取用户ID。
     */
    public Long getCounterpartyUserId() {
        return counterpartyUserId;
    }

    /**
     * 获取账户NO信息。
     */
    public String getAccountNo() {
        return accountNo;
    }

    /**
     * 获取账单NO信息。
     */
    public String getBillNo() {
        return billNo;
    }

    /**
     * 获取业务数据。
     */
    public String getBillMonth() {
        return billMonth;
    }

    /**
     * 获取业务数据。
     */
    public String getDisplayTitle() {
        return displayTitle;
    }

    /**
     * 获取业务数据。
     */
    public String getDisplaySubtitle() {
        return displaySubtitle;
    }

    /**
     * 获取金额。
     */
    public Money getAmount() {
        return amount;
    }

    /**
     * 获取状态。
     */
    public TradeStatus getStatus() {
        return status;
    }

    /**
     * 获取交易时间。
     */
    public LocalDateTime getTradeTime() {
        return tradeTime;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
