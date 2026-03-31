package cn.openaipay.application.fundaccount.service.impl;

import cn.openaipay.application.fundaccount.command.FundIncomeSettleCommand;
import cn.openaipay.application.fundaccount.service.FundAccountService;
import cn.openaipay.application.fundaccount.service.FundIncomeDistributionService;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.fundaccount.model.FundAccount;
import cn.openaipay.domain.fundaccount.model.FundIncomeCalendar;
import cn.openaipay.domain.fundaccount.model.FundIncomeCalendarStatus;
import cn.openaipay.domain.fundaccount.model.FundProductCodes;
import cn.openaipay.domain.fundaccount.model.FundTransaction;
import cn.openaipay.domain.fundaccount.model.FundTransactionType;
import cn.openaipay.domain.fundaccount.repository.FundAccountRepository;
import cn.openaipay.domain.fundtrade.repository.FundTradeRepository;
import cn.openaipay.domain.shared.number.FundAmount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * 基金收益分发应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Service
public class FundIncomeDistributionServiceImpl implements FundIncomeDistributionService {

    /** 资金编码 */
    private static final String DEFAULT_FUND_CODE = FundProductCodes.DEFAULT_FUND_CODE;
    /** 收益结算业务类型 */
    private static final String DAILY_INCOME_SETTLE_BIZ_TYPE = "92";
    /** shanghaizone信息 */
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    /** 收益结算时间 */
    private static final LocalTime INCOME_SETTLE_START_TIME = LocalTime.of(2, 0);
    /** 业务日期信息 */
    private static final DateTimeFormatter BIZ_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    /** 影响份额持仓的基金交易类型。 */
    private static final List<FundTransactionType> HOLDING_AFFECT_TRANSACTION_TYPES = List.of(
            FundTransactionType.SUBSCRIBE,
            FundTransactionType.REDEEM,
            FundTransactionType.FAST_REDEEM,
            FundTransactionType.PRODUCT_SWITCH
    );

    /** 资金信息 */
    private final FundAccountRepository fundAccountRepository;
    /** 基金交易信息 */
    private final FundTradeRepository fundTradeRepository;
    /** 资金信息 */
    private final FundAccountService fundAccountService;
    /** AI支付ID */
    private final AiPayIdGenerator aiPayIdGenerator;
    /** 业务费率 */
    private final BigDecimal aicashAnnualizedYieldRate;

    public FundIncomeDistributionServiceImpl(FundAccountRepository fundAccountRepository,
                                             FundTradeRepository fundTradeRepository,
                                             FundAccountService fundAccountService,
                                             AiPayIdGenerator aiPayIdGenerator,
                                             @Value("${aipay.fund.aicash.annualized-yield-rate:${aipay.fund.aicash.annualized-yield-rate:0.01045}}") BigDecimal aicashAnnualizedYieldRate) {
        this.fundAccountRepository = fundAccountRepository;
        this.fundTradeRepository = fundTradeRepository;
        this.fundAccountService = fundAccountService;
        this.aiPayIdGenerator = aiPayIdGenerator;
        this.aicashAnnualizedYieldRate = normalizeYieldRate(aicashAnnualizedYieldRate);
    }

    /**
     * 处理结算收益用于基金条件信息。
     */
    @Override
    @Transactional
    public int settleTodayIncomeForFundIfNeeded(String fundCode) {
        String normalizedFundCode = normalizeFundCode(fundCode);
        LocalDateTime now = now();
        if (!shouldSettleToday(now)) {
            return 0;
        }
        FundIncomeCalendar calendar = ensureTodayPublishedCalendar(normalizedFundCode, now, null);
        List<FundAccount> accounts = fundAccountRepository.findAllByFundCode(normalizedFundCode);
        int settledCount = 0;
        for (FundAccount accountSnapshot : accounts) {
            if (accountSnapshot == null || accountSnapshot.getUserId() == null) {
                continue;
            }
            boolean settled = settleTodayIncomeForUserInternal(
                    accountSnapshot.getUserId(),
                    normalizedFundCode,
                    calendar,
                    now
            );
            if (settled) {
                settledCount++;
            }
        }
        return settledCount;
    }

    /**
     * 处理结算收益用于用户条件信息。
     */
    @Override
    @Transactional
    public boolean settleTodayIncomeForUserIfNeeded(Long userId, String fundCode) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        String normalizedFundCode = normalizeFundCode(fundCode);
        LocalDateTime now = now();
        if (!shouldSettleToday(now)) {
            return false;
        }
        FundAccount lockedAccount = fundAccountRepository.findByUserIdAndFundCodeForUpdate(userId, normalizedFundCode).orElse(null);
        if (lockedAccount == null) {
            return false;
        }
        FundIncomeCalendar calendar = ensureTodayPublishedCalendar(normalizedFundCode, now, lockedAccount.getLatestNav());
        return settleTodayIncomeForLockedAccount(lockedAccount, calendar, now);
    }

    private boolean settleTodayIncomeForUserInternal(Long userId,
                                                     String fundCode,
                                                     FundIncomeCalendar calendar,
                                                     LocalDateTime now) {
        FundAccount lockedAccount = fundAccountRepository.findByUserIdAndFundCodeForUpdate(userId, fundCode).orElse(null);
        if (lockedAccount == null) {
            return false;
        }
        return settleTodayIncomeForLockedAccount(lockedAccount, calendar, now);
    }

    private boolean settleTodayIncomeForLockedAccount(FundAccount account,
                                                      FundIncomeCalendar calendar,
                                                      LocalDateTime now) {
        // 收益按 T-1 已确认份额计算：从当前持仓回推出“今日确认变更前”的份额。
        FundAmount eligibleShare = resolveIncomeEligibleShare(account, now);
        if (eligibleShare.compareTo(FundAmount.ZERO) <= 0) {
            return false;
        }
        String businessNo = buildIncomeSettleBusinessNo(account.getUserId(), account.getFundCode(), calendar.getBizDate());
        if (fundTradeRepository.findTransactionByBusinessNoAndType(
                account.getUserId(),
                account.getFundCode(),
                FundTransactionType.INCOME_SETTLE,
                businessNo
        ).isPresent()) {
            return false;
        }
        FundAmount incomeAmount = calculateIncomeAmount(eligibleShare, calendar.getIncomePer10k());
        if (incomeAmount.compareTo(FundAmount.ZERO) <= 0) {
            return false;
        }
        String orderNo = aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_FUND_ACCOUNT,
                DAILY_INCOME_SETTLE_BIZ_TYPE,
                String.valueOf(account.getUserId())
        );
        fundAccountService.settleIncome(new FundIncomeSettleCommand(
                orderNo,
                account.getUserId(),
                account.getFundCode(),
                incomeAmount,
                calendar.getNav(),
                businessNo
        ));
        return true;
    }

    private FundAmount resolveIncomeEligibleShare(FundAccount account, LocalDateTime now) {
        FundAmount candidateShare = account.getHoldingShare();
        LocalDateTime dayStart = now.toLocalDate().atStartOfDay();
        List<FundTransaction> todayConfirmedTransactions =
                fundTradeRepository.findConfirmedTransactionsUpdatedInRange(
                        account.getUserId(),
                        account.getFundCode(),
                        dayStart,
                        now,
                        HOLDING_AFFECT_TRANSACTION_TYPES
                );
        for (FundTransaction transaction : todayConfirmedTransactions) {
            if (transaction == null || transaction.getTransactionType() == null) {
                continue;
            }
            switch (transaction.getTransactionType()) {
                case SUBSCRIBE -> candidateShare = candidateShare.subtract(normalizeShareDelta(transaction.getConfirmedShare()));
                case REDEEM, FAST_REDEEM, PRODUCT_SWITCH ->
                        candidateShare = candidateShare.add(normalizeShareDelta(transaction.getRequestShare()));
                default -> {
                    // ignore
                }
            }
        }
        if (candidateShare.compareTo(FundAmount.ZERO) < 0) {
            return FundAmount.ZERO;
        }
        return candidateShare.setScale(FundAmount.SCALE, RoundingMode.HALF_UP);
    }

    private FundAmount normalizeShareDelta(FundAmount value) {
        if (value == null || value.compareTo(FundAmount.ZERO) <= 0) {
            return FundAmount.ZERO;
        }
        return value.setScale(FundAmount.SCALE, RoundingMode.HALF_UP);
    }

    private FundIncomeCalendar ensureTodayPublishedCalendar(String fundCode,
                                                            LocalDateTime now,
                                                            FundAmount fallbackNav) {
        LocalDate bizDate = now.toLocalDate();
        FundIncomeCalendar calendar = fundAccountRepository.findIncomeCalendarForUpdate(fundCode, bizDate)
                .orElse(FundIncomeCalendar.planned(fundCode, bizDate, now));
        if (calendar.getCalendarStatus() == FundIncomeCalendarStatus.PLANNED) {
            FundAmount nav = resolveNav(calendar.getNav(), fallbackNav);
            FundAmount incomePer10k = resolveIncomePer10k(fundCode);
            calendar.publish(nav, incomePer10k, now);
            return fundAccountRepository.saveIncomeCalendar(calendar);
        }
        return calendar;
    }

    private boolean shouldSettleToday(LocalDateTime now) {
        if (now.toLocalTime().isBefore(INCOME_SETTLE_START_TIME)) {
            return false;
        }
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    private String buildIncomeSettleBusinessNo(Long userId, String fundCode, LocalDate bizDate) {
        String compressedFundCode = compressFundCode(fundCode);
        String datePart = bizDate.format(BIZ_DATE_FORMATTER);
        return "FIS:" + compressedFundCode + ":" + datePart + ":" + userId;
    }

    private String compressFundCode(String fundCode) {
        if (fundCode == null || fundCode.isBlank()) {
            return DEFAULT_FUND_CODE;
        }
        String normalized = fundCode.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() <= 12) {
            return normalized;
        }
        return normalized.substring(0, 12);
    }

    private FundAmount calculateIncomeAmount(FundAmount holdingShare, FundAmount incomePer10k) {
        FundAmount income = holdingShare
                .multiply(incomePer10k)
                .divide(FundAmount.of(new BigDecimal("10000")), FundAmount.SCALE, RoundingMode.HALF_UP);
        if (income.compareTo(FundAmount.ZERO) < 0) {
            return FundAmount.ZERO;
        }
        return income.setScale(FundAmount.SCALE, RoundingMode.HALF_UP);
    }

    private FundAmount resolveNav(FundAmount calendarNav, FundAmount fallbackNav) {
        if (calendarNav != null && calendarNav.compareTo(FundAmount.ZERO) > 0) {
            return calendarNav.setScale(FundAmount.SCALE, RoundingMode.HALF_UP);
        }
        if (fallbackNav != null && fallbackNav.compareTo(FundAmount.ZERO) > 0) {
            return fallbackNav.setScale(FundAmount.SCALE, RoundingMode.HALF_UP);
        }
        return FundAmount.ONE.setScale(FundAmount.SCALE, RoundingMode.HALF_UP);
    }

    private FundAmount resolveIncomePer10k(String fundCode) {
        BigDecimal annualizedYieldRate = resolveAnnualizedYieldRate(fundCode);
        if (annualizedYieldRate.signum() <= 0) {
            return FundAmount.ZERO;
        }
        BigDecimal incomePer10k = annualizedYieldRate
                .multiply(BigDecimal.valueOf(10_000))
                .divide(BigDecimal.valueOf(365), FundAmount.SCALE, RoundingMode.HALF_UP);
        return FundAmount.of(incomePer10k);
    }

    private BigDecimal resolveAnnualizedYieldRate(String fundCode) {
        if (DEFAULT_FUND_CODE.equalsIgnoreCase(normalizeFundCode(fundCode))) {
            return aicashAnnualizedYieldRate;
        }
        return BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
    }

    private String normalizeFundCode(String fundCode) {
        if (fundCode == null || fundCode.isBlank()) {
            return DEFAULT_FUND_CODE;
        }
        String normalized = FundProductCodes.normalizeOrDefault(fundCode);
        if (normalized.length() > 32) {
            throw new IllegalArgumentException("fundCode length must be <= 32");
        }
        return normalized;
    }

    private BigDecimal normalizeYieldRate(BigDecimal source) {
        if (source == null || source.signum() < 0) {
            return BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
        }
        return source.setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 处理NOW信息。
     */
    protected LocalDateTime now() {
        return LocalDateTime.now(SHANGHAI_ZONE);
    }
}
