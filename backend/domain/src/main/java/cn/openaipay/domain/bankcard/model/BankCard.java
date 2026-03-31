package cn.openaipay.domain.bankcard.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 用户银行卡模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class BankCard {

    /** 默认币种。 */
    private static final CurrencyUnit DEFAULT_CURRENCY = CurrencyUnit.of("CNY");
    /** 默认单笔限额。 */
    private static final Money DEFAULT_SINGLE_LIMIT = Money.of(DEFAULT_CURRENCY, new BigDecimal("50000.00"), RoundingMode.HALF_UP);
    /** 默认单日限额。 */
    private static final Money DEFAULT_DAILY_LIMIT = Money.of(DEFAULT_CURRENCY, new BigDecimal("200000.00"), RoundingMode.HALF_UP);

    /** 银行卡主键ID */
    private final Long id;
    /** 银行卡号 */
    private final String cardNo;
    /** 绑卡所属用户ID */
    private final Long userId;
    /** 发卡行编码 */
    private final String bankCode;
    /** 发卡行名称 */
    private final String bankName;
    /** 银行卡类型 */
    private final BankCardType cardType;
    /** 持卡人姓名 */
    private final String cardHolderName;
    /** 银行预留手机号 */
    private final String reservedMobile;
    /** 预留手机号后四位 */
    private final String phoneTailNo;
    /** 银行卡状态 */
    private BankCardStatus cardStatus;
    /** 是否默认支付卡 */
    private boolean defaultCard;
    /** 单笔支付限额 */
    private Money singleLimit;
    /** 单日支付限额 */
    private Money dailyLimit;
    /** 创建时间 */
    private final LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;

    public BankCard(Long id,
                    String cardNo,
                    Long userId,
                    String bankCode,
                    String bankName,
                    BankCardType cardType,
                    String cardHolderName,
                    String reservedMobile,
                    String phoneTailNo,
                    BankCardStatus cardStatus,
                    boolean defaultCard,
                    Money singleLimit,
                    Money dailyLimit,
                    LocalDateTime createdAt,
                    LocalDateTime updatedAt) {
        this.id = id;
        this.cardNo = normalizeCardNo(cardNo);
        this.userId = normalizeUserId(userId);
        this.bankCode = normalizeBankCode(bankCode);
        this.bankName = normalizeRequired(bankName, "bankName", 64);
        this.cardType = requireNonNull(cardType, "cardType");
        this.cardHolderName = normalizeRequired(cardHolderName, "cardHolderName", 64);
        this.reservedMobile = normalizeOptional(reservedMobile, 32);
        this.phoneTailNo = normalizePhoneTailNo(phoneTailNo);
        this.cardStatus = requireNonNull(cardStatus, "cardStatus");
        this.defaultCard = defaultCard;
        this.singleLimit = normalizeLimit(singleLimit, DEFAULT_SINGLE_LIMIT, "singleLimit");
        this.dailyLimit = normalizeLimit(dailyLimit, DEFAULT_DAILY_LIMIT, "dailyLimit");
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 处理业务数据。
     */
    public static BankCard bind(String cardNo,
                                Long userId,
                                String bankCode,
                                String bankName,
                                BankCardType cardType,
                                String cardHolderName,
                                String reservedMobile,
                                String phoneTailNo,
                                boolean defaultCard,
                                Money singleLimit,
                                Money dailyLimit,
                                LocalDateTime now) {
        return new BankCard(
                null,
                cardNo,
                userId,
                bankCode,
                bankName,
                cardType,
                cardHolderName,
                reservedMobile,
                phoneTailNo,
                BankCardStatus.ACTIVE,
                defaultCard,
                singleLimit,
                dailyLimit,
                now,
                now
        );
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取卡NO信息。
     */
    public String getCardNo() {
        return cardNo;
    }

    /**
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取银行编码。
     */
    public String getBankCode() {
        return bankCode;
    }

    /**
     * 获取银行信息。
     */
    public String getBankName() {
        return bankName;
    }

    /**
     * 获取卡类型信息。
     */
    public BankCardType getCardType() {
        return cardType;
    }

    /**
     * 获取业务数据。
     */
    public String getCardHolderName() {
        return cardHolderName;
    }

    /**
     * 获取手机号信息。
     */
    public String getReservedMobile() {
        return reservedMobile;
    }

    /**
     * 获取NO信息。
     */
    public String getPhoneTailNo() {
        return phoneTailNo;
    }

    /**
     * 获取卡状态。
     */
    public BankCardStatus getCardStatus() {
        return cardStatus;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isDefaultCard() {
        return defaultCard;
    }

    /**
     * 获取限额信息。
     */
    public Money getSingleLimit() {
        return singleLimit;
    }

    /**
     * 获取限额信息。
     */
    public Money getDailyLimit() {
        return dailyLimit;
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

    /**
     * 处理业务数据。
     */
    public void activate(LocalDateTime now) {
        this.cardStatus = BankCardStatus.ACTIVE;
        touch(now);
    }

    /**
     * 处理业务数据。
     */
    public void deactivate(LocalDateTime now) {
        this.cardStatus = BankCardStatus.INACTIVE;
        touch(now);
    }

    /**
     * 处理业务数据。
     */
    public void unbind(LocalDateTime now) {
        this.cardStatus = BankCardStatus.UNBOUND;
        this.defaultCard = false;
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markDefault(boolean defaultCard, LocalDateTime now) {
        this.defaultCard = defaultCard;
        touch(now);
    }

    /**
     * 更新限额信息。
     */
    public void updateLimit(Money singleLimit, Money dailyLimit, LocalDateTime now) {
        this.singleLimit = normalizeLimit(singleLimit, this.singleLimit, "singleLimit");
        this.dailyLimit = normalizeLimit(dailyLimit, this.dailyLimit, "dailyLimit");
        touch(now);
    }

    private void touch(LocalDateTime now) {
        this.updatedAt = now;
    }

    private static String normalizeCardNo(String cardNo) {
        String normalized = normalizeRequired(cardNo, "cardNo", 32).replace(" ", "");
        if (normalized.length() < 12) {
            throw new IllegalArgumentException("cardNo length must be >= 12");
        }
        if (!normalized.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("cardNo must contain digits only");
        }
        return normalized;
    }

    private static Long normalizeUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        return userId;
    }

    private static String normalizeBankCode(String bankCode) {
        String normalized = normalizeRequired(bankCode, "bankCode", 16).toUpperCase();
        if (!normalized.chars().allMatch(ch -> Character.isLetterOrDigit(ch) || ch == '_')) {
            throw new IllegalArgumentException("bankCode must contain only letters, digits or underscore");
        }
        return normalized;
    }

    private static String normalizePhoneTailNo(String phoneTailNo) {
        if (phoneTailNo == null || phoneTailNo.isBlank()) {
            return null;
        }
        String digits = phoneTailNo.trim().replaceAll("\\D", "");
        if (digits.length() < 4) {
            throw new IllegalArgumentException("phoneTailNo length must be >= 4");
        }
        return digits.substring(digits.length() - 4);
    }

    private static String normalizeRequired(String raw, String fieldName, int maxLength) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        String normalized = raw.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " length must be <= " + maxLength);
        }
        return normalized;
    }

    private static String normalizeOptional(String raw, int maxLength) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException("field length must be <= " + maxLength);
        }
        return normalized;
    }

    private static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }

    private static Money normalizeLimit(Money source, Money fallback, String fieldName) {
        Money normalized = source == null ? fallback : source.rounded(2, RoundingMode.HALF_UP);
        if (!DEFAULT_CURRENCY.equals(normalized.getCurrencyUnit())) {
            throw new IllegalArgumentException(fieldName + " currency must be CNY");
        }
        if (normalized.isLessThan(Money.zero(DEFAULT_CURRENCY)) || normalized.isZero()) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return normalized;
    }
}
