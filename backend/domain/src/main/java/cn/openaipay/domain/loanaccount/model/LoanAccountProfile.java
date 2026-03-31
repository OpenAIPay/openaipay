package cn.openaipay.domain.loanaccount.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 爱借账户档案聚合。
 *
 * 业务场景：
 * - 持久化保存爱借账户核心参数（年化利率、原始年化利率、总期数、放款日期），
 *   避免关键计算长期依赖 trade metadata 文本解析。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public class LoanAccountProfile {

    /** 主键ID。 */
    private final Long id;
    /** 账户号。 */
    private final String accountNo;
    /** 用户ID。 */
    private final Long userId;
    /** 年化利率（百分数）。 */
    private final BigDecimal annualRatePercent;
    /** 原始年化利率（百分数）。 */
    private final BigDecimal originalAnnualRatePercent;
    /** 总期数（月）。 */
    private final Integer totalTermMonths;
    /** 放款日。 */
    private final LocalDate drawDate;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private final LocalDateTime updatedAt;

    public LoanAccountProfile(Long id,
                              String accountNo,
                              Long userId,
                              BigDecimal annualRatePercent,
                              BigDecimal originalAnnualRatePercent,
                              Integer totalTermMonths,
                              LocalDate drawDate,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
        this.id = id;
        this.accountNo = normalizeRequired(accountNo, "accountNo");
        this.userId = requirePositive(userId, "userId");
        this.annualRatePercent = normalizeRate(annualRatePercent, "annualRatePercent");
        this.originalAnnualRatePercent = normalizeRate(originalAnnualRatePercent, "originalAnnualRatePercent");
        this.totalTermMonths = normalizeTermMonths(totalTermMonths);
        this.drawDate = drawDate == null ? LocalDate.now() : drawDate;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 创建默认档案。
     */
    public static LoanAccountProfile createDefault(String accountNo,
                                                   Long userId,
                                                   BigDecimal annualRatePercent,
                                                   BigDecimal originalAnnualRatePercent,
                                                   Integer totalTermMonths,
                                                   LocalDate drawDate,
                                                   LocalDateTime now) {
        LocalDateTime occurredAt = now == null ? LocalDateTime.now() : now;
        return new LoanAccountProfile(
                null,
                accountNo,
                userId,
                annualRatePercent,
                originalAnnualRatePercent,
                totalTermMonths,
                drawDate,
                occurredAt,
                occurredAt
        );
    }

    /**
     * 更新档案核心参数。
     */
    public LoanAccountProfile refresh(BigDecimal annualRatePercent,
                                      BigDecimal originalAnnualRatePercent,
                                      Integer totalTermMonths,
                                      LocalDate drawDate,
                                      LocalDateTime now) {
        return new LoanAccountProfile(
                id,
                accountNo,
                userId,
                annualRatePercent,
                originalAnnualRatePercent,
                totalTermMonths,
                drawDate,
                createdAt,
                now == null ? LocalDateTime.now() : now
        );
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取账户号。
     */
    public String getAccountNo() {
        return accountNo;
    }

    /**
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取年化利率。
     */
    public BigDecimal getAnnualRatePercent() {
        return annualRatePercent;
    }

    /**
     * 获取原始年化利率。
     */
    public BigDecimal getOriginalAnnualRatePercent() {
        return originalAnnualRatePercent;
    }

    /**
     * 获取总期数。
     */
    public Integer getTotalTermMonths() {
        return totalTermMonths;
    }

    /**
     * 获取放款日。
     */
    public LocalDate getDrawDate() {
        return drawDate;
    }

    /**
     * 获取创建时间。
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取更新时间。
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private String normalizeRequired(String raw, String fieldName) {
        if (raw == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private BigDecimal normalizeRate(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }

    private Integer normalizeTermMonths(Integer value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("totalTermMonths must be greater than 0");
        }
        return value;
    }
}
