package cn.openaipay.domain.fundaccount.model;

import cn.openaipay.domain.shared.number.FundAmount;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 基金收益日历模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class FundIncomeCalendar {

    /** 基金产品编码 */
    private final String fundCode;
    /** 业务日期 */
    private final LocalDate bizDate;
    /** 净值 */
    private FundAmount nav;
    /** 收益万份 */
    private FundAmount incomePer10k;
    /** 日历状态 */
    private FundIncomeCalendarStatus calendarStatus;
    /** 发布时间 */
    private LocalDateTime publishedAt;
    /** 结算时间 */
    private LocalDateTime settledAt;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public FundIncomeCalendar(String fundCode,
                              LocalDate bizDate,
                              FundAmount nav,
                              FundAmount incomePer10k,
                              FundIncomeCalendarStatus calendarStatus,
                              LocalDateTime publishedAt,
                              LocalDateTime settledAt,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
        this.fundCode = fundCode;
        this.bizDate = bizDate;
        this.nav = normalize(nav);
        this.incomePer10k = normalize(incomePer10k);
        this.calendarStatus = calendarStatus;
        this.publishedAt = publishedAt;
        this.settledAt = settledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 处理业务数据。
     */
    public static FundIncomeCalendar planned(String fundCode, LocalDate bizDate, LocalDateTime now) {
        return new FundIncomeCalendar(
                fundCode,
                bizDate,
                FundAmount.ZERO,
                FundAmount.ZERO,
                FundIncomeCalendarStatus.PLANNED,
                null,
                null,
                now,
                now
        );
    }

    /**
     * 获取基金编码。
     */
    public String getFundCode() {
        return fundCode;
    }

    /**
     * 获取业务日期信息。
     */
    public LocalDate getBizDate() {
        return bizDate;
    }

    /**
     * 获取NAV信息。
     */
    public FundAmount getNav() {
        return nav;
    }

    /**
     * 获取收益PER10K信息。
     */
    public FundAmount getIncomePer10k() {
        return incomePer10k;
    }

    /**
     * 获取日历状态。
     */
    public FundIncomeCalendarStatus getCalendarStatus() {
        return calendarStatus;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getSettledAt() {
        return settledAt;
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
     * 发布业务数据。
     */
    public void publish(FundAmount nav, FundAmount incomePer10k, LocalDateTime now) {
        FundAmount navValue = normalize(nav);
        FundAmount incomeValue = normalize(incomePer10k);
        if (navValue.compareTo(FundAmount.ZERO) <= 0) {
            throw new IllegalArgumentException("nav must be greater than 0");
        }
        if (incomeValue.compareTo(FundAmount.ZERO) < 0) {
            throw new IllegalArgumentException("incomePer10k must be greater than or equal to 0");
        }
        this.nav = navValue;
        this.incomePer10k = incomeValue;
        this.calendarStatus = FundIncomeCalendarStatus.PUBLISHED;
        this.publishedAt = now;
        this.updatedAt = now;
    }

    /**
     * 标记业务数据。
     */
    public void markSettled(LocalDateTime now) {
        if (calendarStatus != FundIncomeCalendarStatus.PUBLISHED
                && calendarStatus != FundIncomeCalendarStatus.SETTLED) {
            throw new IllegalStateException("income calendar must be published before settled");
        }
        this.calendarStatus = FundIncomeCalendarStatus.SETTLED;
        this.settledAt = now;
        this.updatedAt = now;
    }

    private FundAmount normalize(FundAmount source) {
        if (source == null) {
            return FundAmount.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return source.setScale(4, RoundingMode.HALF_UP);
    }
}
