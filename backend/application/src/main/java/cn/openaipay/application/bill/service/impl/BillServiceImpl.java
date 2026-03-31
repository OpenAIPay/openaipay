package cn.openaipay.application.bill.service.impl;

import cn.openaipay.application.bill.dto.BillEntryDTO;
import cn.openaipay.application.bill.dto.BillEntryPageDTO;
import cn.openaipay.application.bill.service.BillService;
import cn.openaipay.domain.fundaccount.model.FundProductCodes;
import cn.openaipay.domain.message.repository.RedPacketOrderRepository;
import cn.openaipay.domain.pay.model.PayOrder;
import cn.openaipay.domain.pay.repository.PayOrderRepository;
import cn.openaipay.domain.trade.model.TradeBusinessDomainCode;
import cn.openaipay.domain.trade.model.TradeBusinessIndex;
import cn.openaipay.domain.trade.model.TradeOrder;
import cn.openaipay.domain.trade.model.TradeStatus;
import cn.openaipay.domain.trade.repository.TradeRepository;
import cn.openaipay.domain.user.model.UserAggregate;
import cn.openaipay.domain.user.model.UserProfile;
import cn.openaipay.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 账单应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Service
public class BillServiceImpl implements BillService {

    /** 默认币种编码。 */
    private static final String DEFAULT_CURRENCY = "CNY";
    /** 爱存收益发放账单标题。 */
    private static final String AICASH_INCOME_SETTLE_TITLE = "爱存收益发放";
    /** 爱存收益发放账单副标题。 */
    private static final String AICASH_INCOME_SETTLE_SUBTITLE = "投资理财";
    /** 爱存转出到余额账单标题。 */
    private static final String AICASH_REDEEM_TO_BALANCE_TITLE = "爱存转出至余额";
    /** 红包账单副标题。 */
    private static final String RED_PACKET_BILL_SUBTITLE = "红包";
    /** 默认账单读取条数。 */
    private static final int DEFAULT_BILL_ENTRY_LIMIT = 20;
    /** 单次账单读取最大条数。 */
    private static final int MAX_BILL_ENTRY_LIMIT = 100;
    /** 默认账单页码。 */
    private static final int DEFAULT_BILL_ENTRY_PAGE_NO = 1;
    /** 账单分页最大页码。 */
    private static final int MAX_BILL_ENTRY_PAGE_NO = 100_000;
    /** 账单分页查询最大偏移量。 */
    private static final int MAX_BILL_ENTRY_PAGE_OFFSET = 1_000_000;
    /** 账单兜底查询扫描倍率。 */
    private static final int BILL_ENTRY_FALLBACK_SCAN_MULTIPLIER = 4;
    /** 账单兜底查询扫描上限。 */
    private static final int MAX_BILL_ENTRY_FALLBACK_SCAN_LIMIT = 100;
    /** 账单月份格式校验（yyyy-MM）。 */
    private static final Pattern BILL_MONTH_PATTERN = Pattern.compile("^\\d{4}-\\d{2}$");
    /** 银行名称提取正则。 */
    private static final Pattern BANK_NAME_PATTERN = Pattern.compile("([\\p{IsHan}A-Za-z0-9]{2,20}银行)");
    /** 账单游标时间格式（yyyy-MM-dd HH:mm:ss）。 */
    private static final DateTimeFormatter BILL_CURSOR_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 交易仓储。 */
    private final TradeRepository tradeRepository;
    /** 支付仓储。 */
    private final PayOrderRepository payOrderRepository;
    /** 用户仓储。 */
    private final UserRepository userRepository;
    /** 红包订单仓储。 */
    private final RedPacketOrderRepository redPacketOrderRepository;

    public BillServiceImpl(TradeRepository tradeRepository,
                           PayOrderRepository payOrderRepository,
                           UserRepository userRepository,
                           RedPacketOrderRepository redPacketOrderRepository) {
        this.tradeRepository = tradeRepository;
        this.payOrderRepository = payOrderRepository;
        this.userRepository = userRepository;
        this.redPacketOrderRepository = redPacketOrderRepository;
    }

    /**
     * 查询统一账单读模型条目。
     */
    @Override
    @Transactional(readOnly = true)
    public List<BillEntryDTO> queryUserBillEntries(Long userId,
                                                   String billMonth,
                                                   String businessDomainCode,
                                                   Integer limit) {
        Long normalizedUserId = requirePositive(userId, "userId");
        int normalizedLimit = normalizeBillEntryLimit(limit);
        String normalizedBillMonth = normalizeBillMonth(billMonth);
        TradeBusinessDomainCode normalizedBusinessDomainCode = normalizeBusinessDomainCode(businessDomainCode);
        List<TradeBusinessIndex> indexes = tradeRepository.findRecentTradeBusinessIndexesByUserId(
                normalizedUserId,
                normalizedBillMonth,
                normalizedBusinessDomainCode,
                normalizedLimit
        );
        Map<Long, String> nicknameCache = new HashMap<>();
        List<BillEntryDTO> indexedEntries = indexes.stream()
                .filter(this::isBillVisibleIndex)
                .map(index -> toBillEntryDTO(index, nicknameCache))
                .toList();

        List<TradeOrder> fallbackTrades = tradeRepository.findRecentSucceededTradesByUserId(
                normalizedUserId,
                normalizeBillEntryFallbackScanLimit(normalizedLimit)
        );
        List<BillEntryDTO> fallbackEntries = fallbackTrades.stream()
                .filter(tradeOrder -> matchesBillMonth(tradeOrder, normalizedBillMonth))
                .filter(tradeOrder -> matchesBusinessDomainCode(tradeOrder, normalizedBusinessDomainCode))
                .map(this::toFallbackBillEntryDTO)
                .toList();

        List<BillEntryDTO> mergedEntries = mergeBillEntries(indexedEntries, fallbackEntries, normalizedLimit);
        return enrichBillEntriesWithDiscount(mergedEntries);
    }

    /**
     * 分页查询统一账单读模型条目。
     */
    @Override
    @Transactional(readOnly = true)
    public BillEntryPageDTO queryUserBillEntriesPage(Long userId,
                                                     String billMonth,
                                                     String businessDomainCode,
                                                     Integer pageNo,
                                                     Integer pageSize,
                                                     String cursorTradeTime,
                                                     Long cursorId) {
        Long normalizedUserId = requirePositive(userId, "userId");
        int normalizedPageNo = normalizeBillEntryPageNo(pageNo);
        int normalizedPageSize = normalizeBillEntryLimit(pageSize);
        int fetchLimit = normalizedPageSize + 1;
        String normalizedBillMonth = normalizeBillMonth(billMonth);
        TradeBusinessDomainCode normalizedBusinessDomainCode = normalizeBusinessDomainCode(businessDomainCode);
        BillCursor cursor = normalizeBillCursor(cursorTradeTime, cursorId);
        List<TradeBusinessIndex> indexes;
        if (cursor == null) {
            int normalizedOffset = normalizeBillEntryPageOffset(normalizedPageNo, normalizedPageSize);
            indexes = tradeRepository.findTradeBusinessIndexesByUserId(
                    normalizedUserId,
                    normalizedBillMonth,
                    normalizedBusinessDomainCode,
                    normalizedOffset,
                    fetchLimit
            );
        } else {
            indexes = tradeRepository.findTradeBusinessIndexesByUserIdAfterCursor(
                    normalizedUserId,
                    normalizedBillMonth,
                    normalizedBusinessDomainCode,
                    cursor.tradeTime(),
                    cursor.cursorId(),
                    fetchLimit
            );
        }
        List<TradeBusinessIndex> succeededIndexes = indexes.stream()
                .filter(this::isBillVisibleIndex)
                .toList();
        boolean hasMore = succeededIndexes.size() > normalizedPageSize;
        List<TradeBusinessIndex> pageIndexes = succeededIndexes.stream()
                .limit(normalizedPageSize)
                .toList();
        Map<Long, String> nicknameCache = new HashMap<>();
        List<BillEntryDTO> pageEntries = pageIndexes.stream()
                .map(index -> toBillEntryDTO(index, nicknameCache))
                .toList();
        List<BillEntryDTO> entries = enrichBillEntriesWithDiscount(pageEntries);
        Integer nextPageNo = hasMore && cursor == null && normalizedPageNo < Integer.MAX_VALUE
                ? normalizedPageNo + 1
                : null;
        String nextCursorTradeTime = null;
        Long nextCursorId = null;
        if (hasMore && !pageIndexes.isEmpty()) {
            TradeBusinessIndex anchor = pageIndexes.get(pageIndexes.size() - 1);
            nextCursorTradeTime = formatBillCursorTradeTime(anchor.getTradeTime());
            nextCursorId = anchor.getId();
        }
        return new BillEntryPageDTO(
                entries,
                normalizedPageNo,
                normalizedPageSize,
                hasMore,
                nextPageNo,
                nextCursorTradeTime,
                nextCursorId
        );
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private int normalizeBillEntryLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_BILL_ENTRY_LIMIT;
        }
        return Math.max(1, Math.min(limit, MAX_BILL_ENTRY_LIMIT));
    }

    private int normalizeBillEntryPageNo(Integer pageNo) {
        if (pageNo == null) {
            return DEFAULT_BILL_ENTRY_PAGE_NO;
        }
        return Math.max(DEFAULT_BILL_ENTRY_PAGE_NO, Math.min(pageNo, MAX_BILL_ENTRY_PAGE_NO));
    }

    private int normalizeBillEntryPageOffset(int pageNo, int pageSize) {
        long offset = (long) (pageNo - 1) * pageSize;
        if (offset <= 0) {
            return 0;
        }
        return (int) Math.min(offset, MAX_BILL_ENTRY_PAGE_OFFSET);
    }

    private BillCursor normalizeBillCursor(String cursorTradeTime, Long cursorId) {
        String normalizedCursorTradeTime = normalizeOptional(cursorTradeTime);
        if (normalizedCursorTradeTime == null && cursorId == null) {
            return null;
        }
        if (normalizedCursorTradeTime == null || cursorId == null || cursorId <= 0) {
            throw new IllegalArgumentException("cursorTradeTime and cursorId must be provided together");
        }
        return new BillCursor(parseBillCursorTradeTime(normalizedCursorTradeTime), cursorId);
    }

    private LocalDateTime parseBillCursorTradeTime(String rawCursorTradeTime) {
        String normalized = normalizeOptional(rawCursorTradeTime);
        if (normalized == null) {
            throw new IllegalArgumentException("cursorTradeTime must not be blank");
        }
        try {
            return LocalDateTime.parse(normalized, BILL_CURSOR_TIME_FORMATTER);
        } catch (RuntimeException ignored) {
            // ignore
        }
        try {
            return LocalDateTime.parse(normalized);
        } catch (RuntimeException ignored) {
            // ignore
        }
        String normalizedForPattern = normalized.replace("T", " ").replace("Z", "");
        int dotIndex = normalizedForPattern.indexOf('.');
        if (dotIndex > 0) {
            normalizedForPattern = normalizedForPattern.substring(0, dotIndex);
        }
        if (normalizedForPattern.length() >= 19) {
            normalizedForPattern = normalizedForPattern.substring(0, 19);
        }
        try {
            return LocalDateTime.parse(normalizedForPattern, BILL_CURSOR_TIME_FORMATTER);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("cursorTradeTime format must be yyyy-MM-dd HH:mm:ss or ISO-8601");
        }
    }

    private String formatBillCursorTradeTime(LocalDateTime tradeTime) {
        if (tradeTime == null) {
            return null;
        }
        return tradeTime.withNano(0).format(BILL_CURSOR_TIME_FORMATTER);
    }

    private int normalizeBillEntryFallbackScanLimit(int normalizedLimit) {
        int scaledLimit = normalizedLimit * BILL_ENTRY_FALLBACK_SCAN_MULTIPLIER;
        int boundedLimit = Math.min(MAX_BILL_ENTRY_FALLBACK_SCAN_LIMIT, scaledLimit);
        return Math.max(normalizedLimit, boundedLimit);
    }

    private String normalizeBillMonth(String rawBillMonth) {
        String normalized = normalizeOptional(rawBillMonth);
        if (normalized == null) {
            return null;
        }
        if (!BILL_MONTH_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("billMonth format must be yyyy-MM");
        }
        return normalized;
    }

    private TradeBusinessDomainCode normalizeBusinessDomainCode(String rawBusinessDomainCode) {
        String normalized = normalizeOptional(rawBusinessDomainCode);
        if (normalized == null) {
            return null;
        }
        return TradeBusinessDomainCode.from(normalized.toUpperCase(Locale.ROOT));
    }

    private BillEntryDTO toBillEntryDTO(TradeBusinessIndex index, Map<Long, String> nicknameCache) {
        TradeBusinessDomainCode businessDomainCode = index.getBusinessDomainCode();
        String businessType = index.getBusinessType();
        String displayTitle = index.getDisplayTitle();
        String normalizedDisplayTitle = normalizeBrandDisplayText(displayTitle);
        String normalizedDisplaySubtitle = normalizeBrandDisplayText(index.getDisplaySubtitle());
        String resolvedDisplayTitle = resolveBillEntryDisplayTitle(
                businessDomainCode,
                businessType,
                normalizedDisplayTitle,
                normalizedDisplaySubtitle,
                index.getAmount(),
                index.getCounterpartyUserId(),
                nicknameCache,
                index.getTradeOrderNo()
        );
        String resolvedDisplaySubtitle = resolveBillEntryDisplaySubtitle(
                businessDomainCode,
                businessType,
                resolvedDisplayTitle,
                normalizedDisplaySubtitle
        );
        String resolvedTradeType = resolveBillEntryTradeType(
                businessDomainCode,
                businessType,
                resolvedDisplayTitle,
                resolvedDisplaySubtitle,
                null
        );
        String resolvedDirection = resolveBillEntryDirection(
                businessDomainCode,
                businessType,
                resolvedDisplayTitle,
                resolvedDisplaySubtitle,
                resolvedTradeType
        );
        return new BillEntryDTO(
                index.getTradeOrderNo(),
                businessDomainCode.name(),
                index.getBizOrderNo(),
                index.getProductType(),
                businessType,
                resolvedDirection,
                resolvedTradeType,
                index.getAccountNo(),
                index.getBillNo(),
                index.getBillMonth(),
                resolvedDisplayTitle,
                resolvedDisplaySubtitle,
                normalizeBillEntryAmount(
                        index.getAmount(),
                        businessDomainCode,
                        businessType,
                        resolvedDisplayTitle,
                        resolvedDisplaySubtitle
                ),
                null,
                null,
                null,
                index.getAmount().getCurrencyUnit().getCode(),
                index.getStatus().name(),
                index.getTradeTime()
        );
    }

    private BillEntryDTO toFallbackBillEntryDTO(TradeOrder tradeOrder) {
        LocalDateTime tradeTime = resolveBillEntryTradeTime(tradeOrder);
        Money amount = tradeOrder.getOriginalAmount().rounded(2, RoundingMode.HALF_UP);
        String businessDomainCode = resolveFallbackBusinessDomainCode(tradeOrder);
        String businessType = normalizeOptional(tradeOrder.getBusinessSceneCode());
        String normalizedBusinessType = businessType == null ? tradeOrder.getTradeType().name() : businessType;
        String displayTitle = normalizeOptional(tradeOrder.getBusinessSceneCode());
        String displaySubtitle = normalizeOptional(tradeOrder.getPaymentMethod());
        TradeBusinessDomainCode normalizedBusinessDomainCode = normalizeBusinessDomainCode(businessDomainCode);
        String resolvedDisplayTitle = resolveBillEntryDisplayTitle(
                normalizedBusinessDomainCode,
                normalizedBusinessType,
                normalizeBrandDisplayText(displayTitle),
                normalizeBrandDisplayText(displaySubtitle),
                amount,
                null,
                null,
                tradeOrder.getTradeOrderNo()
        );
        String resolvedDisplaySubtitle = resolveBillEntryDisplaySubtitle(
                normalizedBusinessDomainCode,
                normalizedBusinessType,
                resolvedDisplayTitle,
                normalizeBrandDisplayText(displaySubtitle)
        );
        String fallbackTradeType = tradeOrder.getTradeType() == null ? null : tradeOrder.getTradeType().name();
        String resolvedTradeType = resolveBillEntryTradeType(
                normalizedBusinessDomainCode,
                normalizedBusinessType,
                resolvedDisplayTitle,
                resolvedDisplaySubtitle,
                fallbackTradeType
        );
        String resolvedDirection = resolveBillEntryDirection(
                normalizedBusinessDomainCode,
                normalizedBusinessType,
                resolvedDisplayTitle,
                resolvedDisplaySubtitle,
                resolvedTradeType
        );
        return new BillEntryDTO(
                tradeOrder.getTradeOrderNo(),
                businessDomainCode,
                tradeOrder.getBizOrderNo(),
                TradeBusinessDomainCode.TRADE.name().equals(businessDomainCode) ? null : businessDomainCode,
                normalizedBusinessType,
                resolvedDirection,
                resolvedTradeType,
                null,
                null,
                resolveBillMonthByTradeTime(tradeTime),
                resolvedDisplayTitle,
                resolvedDisplaySubtitle,
                normalizeBillEntryAmount(
                        amount,
                        normalizedBusinessDomainCode,
                        normalizedBusinessType,
                        resolvedDisplayTitle,
                        resolvedDisplaySubtitle
                ),
                null,
                null,
                null,
                amount.getCurrencyUnit().getCode(),
                tradeOrder.getStatus().name(),
                tradeTime
        );
    }

    private String normalizeBillEntryAmount(Money amount,
                                            TradeBusinessDomainCode businessDomainCode,
                                            String businessType,
                                            String displayTitle,
                                            String displaySubtitle) {
        Money safeAmount = amount == null ? zeroMoney(CurrencyUnit.of(DEFAULT_CURRENCY)) : amount.rounded(2, RoundingMode.HALF_UP);
        BigDecimal normalized = safeAmount.getAmount().setScale(2, RoundingMode.HALF_UP);
        if (shouldPresentBillEntryAsCredit(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            normalized = normalized.abs();
        }
        return normalized.toPlainString();
    }

    private Money zeroMoney(CurrencyUnit currencyUnit) {
        return Money.of(currencyUnit, BigDecimal.ZERO);
    }

    private boolean isBillVisibleIndex(TradeBusinessIndex index) {
        return index != null && index.getStatus() == TradeStatus.SUCCEEDED;
    }

    private boolean shouldPresentBillEntryAsCredit(TradeBusinessDomainCode businessDomainCode,
                                                   String businessType,
                                                   String displayTitle,
                                                   String displaySubtitle) {
        if (isAiCashIncomeSettleBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return true;
        }
        if (isFundSubscribeScene(businessType)) {
            return true;
        }
        if (isAiCreditRepayBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return true;
        }
        String normalizedTitle = normalizeBrandDisplayText(normalizeOptional(displayTitle));
        if (normalizedTitle == null) {
            return false;
        }
        return "爱存-单次转入".equals(normalizedTitle)
                || (normalizedTitle.contains("爱存") && normalizedTitle.contains("单次转入"));
    }

    private String resolveBillEntryDisplayTitle(TradeBusinessDomainCode businessDomainCode,
                                                String businessType,
                                                String displayTitle,
                                                String displaySubtitle,
                                                Money amount,
                                                Long counterpartyUserId,
                                                Map<Long, String> nicknameCache,
                                                String tradeOrderNo) {
        if (isAiCashIncomeSettleBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return AICASH_INCOME_SETTLE_TITLE;
        }
        String resolvedAiCashRedeemTitle = resolveAiCashRedeemBillTitle(
                businessDomainCode,
                businessType,
                displayTitle,
                displaySubtitle
        );
        if (resolvedAiCashRedeemTitle != null) {
            return resolvedAiCashRedeemTitle;
        }
        if (isAiCreditRepayBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return "爱花还款";
        }
        if (isRedPacketClaimBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            String redPacketSenderName = resolveRedPacketSenderName(
                    displayTitle,
                    counterpartyUserId,
                    nicknameCache,
                    tradeOrderNo
            );
            if (redPacketSenderName != null) {
                return "领取" + redPacketSenderName + "的红包";
            }
            return "领取" + formatRedPacketTitleAmount(amount) + "的红包";
        }
        return displayTitle;
    }

    private String resolveBillEntryDisplaySubtitle(TradeBusinessDomainCode businessDomainCode,
                                                   String businessType,
                                                   String displayTitle,
                                                   String displaySubtitle) {
        if (isAiCashIncomeSettleBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return AICASH_INCOME_SETTLE_SUBTITLE;
        }
        if (isAiCreditRepayBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return "信用借还";
        }
        if (isRedPacketBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return RED_PACKET_BILL_SUBTITLE;
        }
        return displaySubtitle;
    }

    private String resolveAiCashRedeemBillTitle(TradeBusinessDomainCode businessDomainCode,
                                                String businessType,
                                                String displayTitle,
                                                String displaySubtitle) {
        if (!isAiCashRedeemBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return null;
        }
        String bankName = extractBankName(displayTitle, displaySubtitle);
        if (bankName != null) {
            return "爱存转出至" + bankName;
        }
        if (hasBankDestinationHint(displayTitle, displaySubtitle)) {
            return "爱存转出至银行卡";
        }
        return AICASH_REDEEM_TO_BALANCE_TITLE;
    }

    private String resolveBillEntryTradeType(TradeBusinessDomainCode businessDomainCode,
                                             String businessType,
                                             String displayTitle,
                                             String displaySubtitle,
                                             String fallbackTradeType) {
        if (isAiCashIncomeSettleBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return "FUND";
        }
        if (isRedPacketBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return "RED_PACKET";
        }
        String normalizedFallbackTradeType = normalizeOptional(fallbackTradeType);
        if (normalizedFallbackTradeType != null) {
            return normalizedFallbackTradeType.toUpperCase(Locale.ROOT);
        }
        String normalizedBusinessType = normalizeOptional(businessType);
        String normalizedTitle = normalizeOptional(displayTitle);
        String normalizedSubtitle = normalizeOptional(displaySubtitle);
        String upperBusinessType = normalizedBusinessType == null ? "" : normalizedBusinessType.toUpperCase(Locale.ROOT);
        String upperTitle = normalizedTitle == null ? "" : normalizedTitle.toUpperCase(Locale.ROOT);
        String upperSubtitle = normalizedSubtitle == null ? "" : normalizedSubtitle.toUpperCase(Locale.ROOT);
        String combined = upperBusinessType + " " + upperTitle + " " + upperSubtitle;

        if (combined.contains("WITHDRAW") || (normalizedTitle != null && normalizedTitle.contains("提现"))) {
            return "WITHDRAW";
        }
        if (combined.contains("DEPOSIT")
                || (normalizedTitle != null && normalizedTitle.contains("充值") && !normalizedTitle.contains("话费"))) {
            return "DEPOSIT";
        }
        if (combined.contains("REFUND") || (normalizedTitle != null && normalizedTitle.contains("退款"))) {
            return "REFUND";
        }
        if (combined.contains("TRANSFER")
                || (normalizedTitle != null && (normalizedTitle.contains("转账") || normalizedTitle.contains("红包")))) {
            return "TRANSFER";
        }
        if (combined.contains("LOAN")
                || (normalizedTitle != null
                && (normalizedTitle.contains("借款") || normalizedTitle.contains("放款") || normalizedTitle.contains("还款")))
                || businessDomainCode == TradeBusinessDomainCode.AILOAN) {
            return "LOAN";
        }
        if (combined.contains("FUND")
                || (normalizedTitle != null && normalizedTitle.contains("爱存"))
                || businessDomainCode == TradeBusinessDomainCode.AICASH) {
            return "FUND";
        }
        if (!upperBusinessType.isEmpty()) {
            return upperBusinessType;
        }
        return "PAY";
    }

    private String resolveBillEntryDirection(TradeBusinessDomainCode businessDomainCode,
                                             String businessType,
                                             String displayTitle,
                                             String displaySubtitle,
                                             String tradeType) {
        if (isAiCashIncomeSettleBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return "CREDIT";
        }
        if (isRedPacketClaimBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return "CREDIT";
        }
        if (isRedPacketSendBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return "DEBIT";
        }
        String normalizedTitle = normalizeOptional(displayTitle);
        String upperTitle = normalizedTitle == null ? "" : normalizedTitle.toUpperCase(Locale.ROOT);
        String upperBusinessType = normalizeOptional(businessType) == null
                ? ""
                : normalizeOptional(businessType).toUpperCase(Locale.ROOT);
        String upperSubtitle = normalizeOptional(displaySubtitle) == null
                ? ""
                : normalizeOptional(displaySubtitle).toUpperCase(Locale.ROOT);
        String combined = upperBusinessType + " " + upperTitle + " " + upperSubtitle;
        String normalizedTradeType = normalizeOptional(tradeType) == null
                ? ""
                : normalizeOptional(tradeType).toUpperCase(Locale.ROOT);

        if ((normalizedTitle != null && (
                normalizedTitle.contains("收到")
                        || normalizedTitle.contains("退款")
                        || normalizedTitle.contains("返还")
                        || normalizedTitle.contains("到账")
                        || normalizedTitle.contains("收益")
                        || normalizedTitle.contains("入账")
                        || normalizedTitle.contains("转入")
                        || normalizedTitle.contains("放款")))
                || combined.contains("TRANSFER_IN")
                || combined.contains("REFUND")
                || combined.contains("INCOME")
                || combined.contains("RECEIVE")
                || combined.contains("PROFIT")
                || combined.contains("YIELD")
                || combined.contains("DIVIDEND")
                || combined.contains("REDEEM")) {
            return "CREDIT";
        }

        if ((normalizedTitle != null
                && ((normalizedTitle.startsWith("向") && normalizedTitle.contains("转账"))
                || normalizedTitle.contains("还款")
                || normalizedTitle.contains("支付")
                || normalizedTitle.contains("消费")
                || normalizedTitle.contains("扣款")
                || normalizedTitle.contains("提现")
                || normalizedTitle.contains("转出")
                || normalizedTitle.contains("缴费")
                || normalizedTitle.contains("话费充值")))
                || combined.contains("TRANSFER_OUT")
                || combined.contains("REPAY")
                || combined.contains("WITHDRAW")
                || combined.contains("PURCHASE")
                || combined.contains("CONSUME")
                || combined.contains("TOP_UP")
                || combined.contains("TOPUP")
                || combined.contains("CHARGE")
                || combined.contains("SUBSCRIBE")) {
            return "DEBIT";
        }

        if ((normalizedTitle != null && normalizedTitle.contains("余额充值")) || combined.contains("DEPOSIT")) {
            return "CREDIT";
        }

        if (businessDomainCode == TradeBusinessDomainCode.AILOAN) {
            if (combined.contains("REPAY") || (normalizedTitle != null && normalizedTitle.contains("还款"))) {
                return "DEBIT";
            }
            return "CREDIT";
        }

        if ("DEPOSIT".equals(normalizedTradeType) || "REFUND".equals(normalizedTradeType)) {
            return "CREDIT";
        }
        if ("WITHDRAW".equals(normalizedTradeType)
                || "TRANSFER".equals(normalizedTradeType)
                || "PAY".equals(normalizedTradeType)) {
            return "DEBIT";
        }
        return "DEBIT";
    }

    private boolean isAiCreditRepayBillEntry(TradeBusinessDomainCode businessDomainCode,
                                             String businessType,
                                             String displayTitle,
                                             String displaySubtitle) {
        if (businessDomainCode != TradeBusinessDomainCode.AICREDIT) {
            return false;
        }
        return containsRepayKeyword(businessType)
                || containsRepayKeyword(displayTitle)
                || containsRepayKeyword(displaySubtitle);
    }

    private boolean isAiCashRedeemBillEntry(TradeBusinessDomainCode businessDomainCode,
                                            String businessType,
                                            String displayTitle,
                                            String displaySubtitle) {
        if (businessDomainCode != TradeBusinessDomainCode.AICASH) {
            return false;
        }
        if (isAiCashIncomeSettleBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return false;
        }
        String normalizedBusinessType = normalizeOptional(businessType);
        String normalizedTitle = normalizeOptional(displayTitle);
        String normalizedSubtitle = normalizeOptional(displaySubtitle);
        String upperBusinessType = normalizedBusinessType == null ? "" : normalizedBusinessType.toUpperCase(Locale.ROOT);
        String upperTitle = normalizedTitle == null ? "" : normalizedTitle.toUpperCase(Locale.ROOT);
        String upperSubtitle = normalizedSubtitle == null ? "" : normalizedSubtitle.toUpperCase(Locale.ROOT);
        if (upperBusinessType.contains("FUND_FAST_REDEEM") || upperBusinessType.contains("FUND_REDEEM")) {
            return true;
        }
        if (upperTitle.contains("FUND_FAST_REDEEM")
                || upperTitle.contains("FUND_REDEEM")
                || upperSubtitle.contains("FUND_FAST_REDEEM")
                || upperSubtitle.contains("FUND_REDEEM")) {
            return true;
        }
        if (normalizedTitle != null && normalizedTitle.contains("爱存") && normalizedTitle.contains("转出")) {
            return true;
        }
        return normalizedSubtitle != null && normalizedSubtitle.contains("爱存") && normalizedSubtitle.contains("转出");
    }

    private boolean isAiCashIncomeSettleBillEntry(TradeBusinessDomainCode businessDomainCode,
                                                   String businessType,
                                                   String displayTitle,
                                                   String displaySubtitle) {
        if (businessDomainCode != TradeBusinessDomainCode.AICASH) {
            return false;
        }
        return containsIncomeSettleKeyword(businessType)
                || containsIncomeSettleKeyword(displayTitle)
                || containsIncomeSettleKeyword(displaySubtitle);
    }

    private boolean containsRepayKeyword(String rawValue) {
        String normalized = normalizeOptional(rawValue);
        if (normalized == null) {
            return false;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        return upper.contains("REPAY") || normalized.contains("还款");
    }

    private boolean containsIncomeSettleKeyword(String rawValue) {
        String normalized = normalizeOptional(rawValue);
        if (normalized == null) {
            return false;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        return upper.contains("YIELD_SETTLE")
                || upper.contains("INCOME_SETTLE")
                || upper.contains("YIELD")
                || upper.contains("INCOME")
                || normalized.contains("收益发放")
                || normalized.contains("收益入账");
    }

    private String extractBankName(String displayTitle, String displaySubtitle) {
        String[] candidates = {displayTitle, displaySubtitle};
        for (String candidateRaw : candidates) {
            String candidate = normalizeOptional(candidateRaw);
            if (candidate == null || candidate.contains("余额")) {
                continue;
            }
            Matcher matcher = BANK_NAME_PATTERN.matcher(candidate);
            String matched = null;
            while (matcher.find()) {
                String current = normalizeOptional(matcher.group(1));
                if (current != null && !"银行".equals(current)) {
                    matched = current;
                }
            }
            if (matched != null) {
                String normalizedBankName = normalizeRedeemBankName(matched);
                if (normalizedBankName != null) {
                    return normalizedBankName;
                }
            }
        }
        return null;
    }

    private String normalizeRedeemBankName(String rawBankName) {
        String normalized = normalizeOptional(rawBankName);
        if (normalized == null) {
            return null;
        }
        String stripped = normalized.trim();
        while (true) {
            String next = stripped
                    .replaceFirst("^爱存转出至", "")
                    .replaceFirst("^爱存转出到", "")
                    .replaceFirst("^余额宝转出至", "")
                    .replaceFirst("^余额宝转出到", "")
                    .replaceFirst("^转出至", "")
                    .replaceFirst("^转出到", "")
                    .replaceFirst("^提现至", "")
                    .replaceFirst("^提现到", "")
                    .trim();
            if (next.equals(stripped)) {
                break;
            }
            stripped = next;
        }
        stripped = stripped
                .replaceFirst("^[：:\\s]+", "")
                .replaceFirst("[：:\\s]+$", "");
        if (stripped.isBlank() || "银行".equals(stripped) || "银行卡".equals(stripped)) {
            return null;
        }
        return stripped;
    }

    private boolean hasBankDestinationHint(String displayTitle, String displaySubtitle) {
        String normalizedTitle = normalizeOptional(displayTitle);
        String normalizedSubtitle = normalizeOptional(displaySubtitle);
        String combined = (
                Optional.ofNullable(normalizedTitle).orElse("")
                        + " "
                        + Optional.ofNullable(normalizedSubtitle).orElse("")
        ).toUpperCase(Locale.ROOT);
        return combined.contains("BANK")
                || combined.contains("BANK_CARD")
                || combined.contains("CARD")
                || containsBankCardHint(normalizedTitle)
                || containsBankCardHint(normalizedSubtitle);
    }

    private boolean containsBankCardHint(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return false;
        }
        return normalized.contains("银行卡")
                || normalized.contains("银行")
                || normalized.contains("借记卡")
                || normalized.contains("储蓄卡")
                || normalized.contains("尾号");
    }

    private boolean isRedPacketBillEntry(TradeBusinessDomainCode businessDomainCode,
                                         String businessType,
                                         String displayTitle,
                                         String displaySubtitle) {
        if (businessDomainCode == TradeBusinessDomainCode.RED_PACKET) {
            return true;
        }
        return containsRedPacketKeyword(businessType)
                || containsRedPacketKeyword(displayTitle)
                || containsRedPacketKeyword(displaySubtitle);
    }

    private boolean isRedPacketClaimBillEntry(TradeBusinessDomainCode businessDomainCode,
                                              String businessType,
                                              String displayTitle,
                                              String displaySubtitle) {
        if (!isRedPacketBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return false;
        }
        String combined = (
                Optional.ofNullable(normalizeOptional(businessType)).orElse("")
                        + " "
                        + Optional.ofNullable(normalizeOptional(displayTitle)).orElse("")
                        + " "
                        + Optional.ofNullable(normalizeOptional(displaySubtitle)).orElse("")
        ).toUpperCase(Locale.ROOT);
        return combined.contains("CLAIM") || combined.contains("领取");
    }

    private boolean isRedPacketSendBillEntry(TradeBusinessDomainCode businessDomainCode,
                                             String businessType,
                                             String displayTitle,
                                             String displaySubtitle) {
        if (!isRedPacketBillEntry(businessDomainCode, businessType, displayTitle, displaySubtitle)) {
            return false;
        }
        String combined = (
                Optional.ofNullable(normalizeOptional(businessType)).orElse("")
                        + " "
                        + Optional.ofNullable(normalizeOptional(displayTitle)).orElse("")
                        + " "
                        + Optional.ofNullable(normalizeOptional(displaySubtitle)).orElse("")
        ).toUpperCase(Locale.ROOT);
        return combined.contains("SEND") || combined.contains("发红包");
    }

    private boolean containsRedPacketKeyword(String rawValue) {
        String normalized = normalizeOptional(rawValue);
        if (normalized == null) {
            return false;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        return upper.contains("RED_PACKET") || normalized.contains("红包");
    }

    private String formatRedPacketTitleAmount(Money amount) {
        Money safeAmount = amount == null
                ? zeroMoney(CurrencyUnit.of(DEFAULT_CURRENCY))
                : amount.rounded(2, RoundingMode.HALF_UP);
        BigDecimal absolute = safeAmount.getAmount().abs().setScale(2, RoundingMode.HALF_UP);
        return absolute.toPlainString();
    }

    private String extractRedPacketSenderName(String displayTitle) {
        String normalizedTitle = normalizeOptional(displayTitle);
        if (normalizedTitle == null) {
            return null;
        }
        String receivedPrefix = "收到来自";
        String suffix = "的红包";
        if (normalizedTitle.startsWith(receivedPrefix) && normalizedTitle.endsWith(suffix)) {
            String senderName = normalizedTitle.substring(receivedPrefix.length(), normalizedTitle.length() - suffix.length()).trim();
            return senderName.isEmpty() ? null : senderName;
        }
        if (normalizedTitle.endsWith(suffix)) {
            int fromIndex = normalizedTitle.indexOf("来自");
            if (fromIndex >= 0) {
                String senderName = normalizedTitle.substring(fromIndex + 2, normalizedTitle.length() - suffix.length()).trim();
                return senderName.isEmpty() ? null : senderName;
            }
        }
        return null;
    }

    private String resolveRedPacketSenderName(String displayTitle,
                                              Long counterpartyUserId,
                                              Map<Long, String> nicknameCache,
                                              String claimTradeOrderNo) {
        String fromTitle = extractRedPacketSenderName(displayTitle);
        if (isUsableRedPacketSenderName(fromTitle)) {
            return fromTitle;
        }
        String fromCounterparty = resolveNicknameByUserId(counterpartyUserId, nicknameCache);
        if (isUsableRedPacketSenderName(fromCounterparty)) {
            return fromCounterparty;
        }
        String normalizedClaimTradeOrderNo = normalizeOptional(claimTradeOrderNo);
        if (normalizedClaimTradeOrderNo == null) {
            return null;
        }
        Long senderUserId = redPacketOrderRepository.findByClaimTradeNo(normalizedClaimTradeOrderNo)
                .map(order -> order == null ? null : order.getSenderUserId())
                .orElse(null);
        String fromClaimTrade = resolveNicknameByUserId(senderUserId, nicknameCache);
        if (isUsableRedPacketSenderName(fromClaimTrade)) {
            return fromClaimTrade;
        }
        return null;
    }

    private String resolveNicknameByUserId(Long userId, Map<Long, String> nicknameCache) {
        if (userId == null || userId <= 0) {
            return null;
        }
        if (nicknameCache != null && nicknameCache.containsKey(userId)) {
            return nicknameCache.get(userId);
        }
        String nickname = userRepository.findByUserId(userId)
                .map(UserAggregate::getProfile)
                .map(UserProfile::getNickname)
                .map(this::normalizeOptional)
                .orElse(null);
        if (nicknameCache != null) {
            nicknameCache.put(userId, nickname);
        }
        return nickname;
    }

    private boolean isUsableRedPacketSenderName(String senderName) {
        String normalized = normalizeOptional(senderName);
        if (normalized == null) {
            return false;
        }
        return !normalized.contains("红包中间账户");
    }

    private boolean isFundSubscribeScene(String businessSceneCode) {
        String normalized = normalizeOptional(businessSceneCode);
        if (normalized == null) {
            return false;
        }
        String upper = normalized.toUpperCase();
        return upper.contains("FUND_SUBSCRIBE")
                || upper.contains(FundProductCodes.AICASH + "_TRANSFER_IN");
    }

    private List<BillEntryDTO> enrichBillEntriesWithDiscount(List<BillEntryDTO> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        Map<String, Optional<PayOrder>> tradeOrderPayCache = new HashMap<>();
        Map<String, Optional<PayOrder>> bizOrderPayCache = new HashMap<>();
        Map<String, Optional<PayOrder>> payOrderNoCache = new HashMap<>();
        Map<String, Optional<PayOrder>> tradeSourceBizPayCache = new HashMap<>();
        preloadBillEntryPayOrderCaches(
                entries,
                tradeOrderPayCache,
                bizOrderPayCache,
                payOrderNoCache,
                tradeSourceBizPayCache
        );
        return entries.stream()
                .map(entry -> enrichBillEntryWithDiscount(
                        entry,
                        tradeOrderPayCache,
                        bizOrderPayCache,
                        payOrderNoCache,
                        tradeSourceBizPayCache
                ))
                .toList();
    }

    private void preloadBillEntryPayOrderCaches(List<BillEntryDTO> entries,
                                                Map<String, Optional<PayOrder>> tradeOrderPayCache,
                                                Map<String, Optional<PayOrder>> bizOrderPayCache,
                                                Map<String, Optional<PayOrder>> payOrderNoCache,
                                                Map<String, Optional<PayOrder>> tradeSourceBizPayCache) {
        List<String> tradeOrderNos = collectNormalizedEntryValues(entries, BillEntryDTO::tradeOrderNo);
        List<String> bizOrderNos = collectNormalizedEntryValues(entries, BillEntryDTO::bizOrderNo);

        Set<String> payOrderNoCandidates = new LinkedHashSet<>();
        for (BillEntryDTO entry : entries) {
            String normalizedTradeOrderNo = normalizeOptional(entry.tradeOrderNo());
            String normalizedBizOrderNo = normalizeOptional(entry.bizOrderNo());
            String normalizedBillNo = normalizeOptional(entry.billNo());
            String normalizedAccountNo = normalizeOptional(entry.accountNo());
            if (normalizedTradeOrderNo != null) {
                payOrderNoCandidates.add(normalizedTradeOrderNo);
            }
            if (normalizedBizOrderNo != null) {
                payOrderNoCandidates.add(normalizedBizOrderNo);
            }
            if (normalizedBillNo != null) {
                payOrderNoCandidates.add(normalizedBillNo);
            }
            if (normalizedAccountNo != null) {
                payOrderNoCandidates.add(normalizedAccountNo);
            }
        }
        List<String> payOrderNos = new ArrayList<>(payOrderNoCandidates);

        List<PayOrder> rawPayOrdersByTradeOrderNo = payOrderRepository.findLatestOrdersByTradeOrderNos(tradeOrderNos);
        boolean tradeOrderBatchEnabled = rawPayOrdersByTradeOrderNo != null;
        List<PayOrder> payOrdersByTradeOrderNo = sanitizePayOrders(rawPayOrdersByTradeOrderNo);
        for (PayOrder payOrder : payOrdersByTradeOrderNo) {
            String tradeOrderNo = normalizeOptional(payOrder.getTradeOrderNo());
            if (tradeOrderNo != null) {
                tradeOrderPayCache.put(tradeOrderNo, Optional.of(payOrder));
            }
        }
        if (tradeOrderBatchEnabled) {
            for (String tradeOrderNo : tradeOrderNos) {
                tradeOrderPayCache.putIfAbsent(tradeOrderNo, Optional.empty());
            }
        }

        List<PayOrder> rawPayOrdersByBizOrderNo = payOrderRepository.findOrdersByBizOrderNos(bizOrderNos);
        boolean bizOrderBatchEnabled = rawPayOrdersByBizOrderNo != null;
        List<PayOrder> payOrdersByBizOrderNo = sanitizePayOrders(rawPayOrdersByBizOrderNo);
        for (PayOrder payOrder : payOrdersByBizOrderNo) {
            String bizOrderNo = normalizeOptional(payOrder.getBizOrderNo());
            if (bizOrderNo != null) {
                bizOrderPayCache.put(bizOrderNo, Optional.of(payOrder));
            }
        }
        if (bizOrderBatchEnabled) {
            for (String bizOrderNo : bizOrderNos) {
                bizOrderPayCache.putIfAbsent(bizOrderNo, Optional.empty());
            }
        }

        List<PayOrder> rawPayOrdersByPayOrderNo = payOrderRepository.findOrdersByPayOrderNos(payOrderNos);
        boolean payOrderBatchEnabled = rawPayOrdersByPayOrderNo != null;
        List<PayOrder> payOrdersByPayOrderNo = sanitizePayOrders(rawPayOrdersByPayOrderNo);
        for (PayOrder payOrder : payOrdersByPayOrderNo) {
            String payOrderNo = normalizeOptional(payOrder.getPayOrderNo());
            if (payOrderNo != null) {
                payOrderNoCache.put(payOrderNo, Optional.of(payOrder));
            }
        }
        if (payOrderBatchEnabled) {
            for (String payOrderNo : payOrderNos) {
                payOrderNoCache.putIfAbsent(payOrderNo, Optional.empty());
            }
        }

        List<PayOrder> rawPayOrdersByTradeSourceBiz = payOrderRepository.findLatestOrdersBySourceBizNos("TRADE", tradeOrderNos);
        boolean tradeSourceBizBatchEnabled = rawPayOrdersByTradeSourceBiz != null;
        List<PayOrder> payOrdersByTradeSourceBiz = sanitizePayOrders(rawPayOrdersByTradeSourceBiz);
        for (PayOrder payOrder : payOrdersByTradeSourceBiz) {
            String sourceBizNo = normalizeOptional(payOrder.getSourceBizNo());
            if (sourceBizNo != null) {
                tradeSourceBizPayCache.put(sourceBizNo, Optional.of(payOrder));
            }
        }
        if (tradeSourceBizBatchEnabled) {
            for (String tradeOrderNo : tradeOrderNos) {
                tradeSourceBizPayCache.putIfAbsent(tradeOrderNo, Optional.empty());
            }
        }
    }

    private List<PayOrder> sanitizePayOrders(List<PayOrder> rawPayOrders) {
        if (rawPayOrders == null || rawPayOrders.isEmpty()) {
            return List.of();
        }
        return rawPayOrders.stream().filter(payOrder -> payOrder != null).toList();
    }

    private List<String> collectNormalizedEntryValues(List<BillEntryDTO> entries,
                                                      java.util.function.Function<BillEntryDTO, String> valueExtractor) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (BillEntryDTO entry : entries) {
            String value = normalizeOptional(valueExtractor.apply(entry));
            if (value != null) {
                values.add(value);
            }
        }
        if (values.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(values);
    }

    private BillEntryDTO enrichBillEntryWithDiscount(BillEntryDTO entry,
                                                     Map<String, Optional<PayOrder>> tradeOrderPayCache,
                                                     Map<String, Optional<PayOrder>> bizOrderPayCache,
                                                     Map<String, Optional<PayOrder>> payOrderNoCache,
                                                     Map<String, Optional<PayOrder>> tradeSourceBizPayCache) {
        Optional<PayOrder> matchedPayOrder = resolvePayOrderForBillEntry(
                entry,
                tradeOrderPayCache,
                bizOrderPayCache,
                payOrderNoCache,
                tradeSourceBizPayCache
        );
        if (matchedPayOrder.isEmpty()) {
            return entry;
        }
        PayOrder payOrder = matchedPayOrder.get();
        BigDecimal existingCouponDiscount = parsePositiveAmount(entry.couponDiscountAmount());
        BigDecimal existingDiscountAmount = parsePositiveAmount(entry.discountAmount());
        BigDecimal payDiscountAmount = parsePositiveAmount(payOrder.getDiscountAmount());
        BigDecimal resolvedDiscountAmount = maxPositiveAmount(existingCouponDiscount, existingDiscountAmount, payDiscountAmount);
        String normalizedDiscountAmount = formatOptionalAmount(resolvedDiscountAmount);
        String resolvedCouponNo = firstNonBlank(entry.couponNo(), payOrder.getCouponNo());
        return new BillEntryDTO(
                entry.tradeOrderNo(),
                entry.businessDomainCode(),
                entry.bizOrderNo(),
                entry.productType(),
                entry.businessType(),
                entry.direction(),
                entry.tradeType(),
                entry.accountNo(),
                entry.billNo(),
                entry.billMonth(),
                entry.displayTitle(),
                entry.displaySubtitle(),
                entry.amount(),
                normalizedDiscountAmount,
                normalizedDiscountAmount,
                resolvedCouponNo,
                entry.currencyCode(),
                entry.status(),
                entry.tradeTime()
        );
    }

    private Optional<PayOrder> resolvePayOrderForBillEntry(BillEntryDTO entry,
                                                           Map<String, Optional<PayOrder>> tradeOrderPayCache,
                                                           Map<String, Optional<PayOrder>> bizOrderPayCache,
                                                           Map<String, Optional<PayOrder>> payOrderNoCache,
                                                           Map<String, Optional<PayOrder>> tradeSourceBizPayCache) {
        String normalizedTradeOrderNo = normalizeOptional(entry.tradeOrderNo());
        if (normalizedTradeOrderNo != null) {
            Optional<PayOrder> matchedByTradeOrderNo = tradeOrderPayCache.computeIfAbsent(
                    normalizedTradeOrderNo,
                    this::findLatestPayOrderByTradeOrderNoSafely
            );
            if (matchedByTradeOrderNo.isPresent()) {
                return matchedByTradeOrderNo;
            }
            Optional<PayOrder> matchedByTradeOrderAsPayOrderNo = payOrderNoCache.computeIfAbsent(
                    normalizedTradeOrderNo,
                    this::findPayOrderByPayOrderNoSafely
            );
            if (matchedByTradeOrderAsPayOrderNo.isPresent()) {
                return matchedByTradeOrderAsPayOrderNo;
            }
            Optional<PayOrder> matchedByTradeSourceBiz = tradeSourceBizPayCache.computeIfAbsent(
                    normalizedTradeOrderNo,
                    this::findPayOrderByTradeSourceBizSafely
            );
            if (matchedByTradeSourceBiz.isPresent()) {
                return matchedByTradeSourceBiz;
            }
        }
        String normalizedBizOrderNo = normalizeOptional(entry.bizOrderNo());
        if (normalizedBizOrderNo != null) {
            Optional<PayOrder> matchedByBizOrderNo = bizOrderPayCache.computeIfAbsent(
                    normalizedBizOrderNo,
                    this::findPayOrderByBizOrderNoSafely
            );
            if (matchedByBizOrderNo.isPresent()) {
                return matchedByBizOrderNo;
            }
            // 一些历史账单把支付单号写入了 bizOrderNo 字段，这里按 payOrderNo 再兜底一次。
            Optional<PayOrder> matchedByPayOrderNo = payOrderNoCache.computeIfAbsent(
                    normalizedBizOrderNo,
                    this::findPayOrderByPayOrderNoSafely
            );
            if (matchedByPayOrderNo.isPresent()) {
                return matchedByPayOrderNo;
            }
        }
        String normalizedBillNo = normalizeOptional(entry.billNo());
        if (normalizedBillNo != null) {
            Optional<PayOrder> matchedByBillNo = payOrderNoCache.computeIfAbsent(
                    normalizedBillNo,
                    this::findPayOrderByPayOrderNoSafely
            );
            if (matchedByBillNo.isPresent()) {
                return matchedByBillNo;
            }
        }
        String normalizedAccountNo = normalizeOptional(entry.accountNo());
        if (normalizedAccountNo != null) {
            Optional<PayOrder> matchedByAccountNo = payOrderNoCache.computeIfAbsent(
                    normalizedAccountNo,
                    this::findPayOrderByPayOrderNoSafely
            );
            if (matchedByAccountNo.isPresent()) {
                return matchedByAccountNo;
            }
        }
        return Optional.empty();
    }

    private Optional<PayOrder> findLatestPayOrderByTradeOrderNoSafely(String tradeOrderNo) {
        try {
            return safeOptional(payOrderRepository.findLatestOrderByTradeOrderNo(tradeOrderNo));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private Optional<PayOrder> findPayOrderByBizOrderNoSafely(String bizOrderNo) {
        try {
            return safeOptional(payOrderRepository.findOrderByBizOrderNo(bizOrderNo));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private Optional<PayOrder> findPayOrderByPayOrderNoSafely(String payOrderNo) {
        try {
            return safeOptional(payOrderRepository.findOrderByPayOrderNo(payOrderNo));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private Optional<PayOrder> findPayOrderByTradeSourceBizSafely(String tradeOrderNo) {
        try {
            return safeOptional(payOrderRepository.findLatestOrderBySourceBiz("TRADE", tradeOrderNo));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private Optional<PayOrder> safeOptional(Optional<PayOrder> value) {
        return value == null ? Optional.empty() : value;
    }

    private BigDecimal parsePositiveAmount(Money value) {
        if (value == null) {
            return null;
        }
        BigDecimal normalized = value.getAmount().abs().setScale(2, RoundingMode.HALF_UP);
        return normalized.compareTo(BigDecimal.ZERO) > 0 ? normalized : null;
    }

    private BigDecimal parsePositiveAmount(String raw) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            return null;
        }
        try {
            BigDecimal parsed = new BigDecimal(normalized).abs().setScale(2, RoundingMode.HALF_UP);
            return parsed.compareTo(BigDecimal.ZERO) > 0 ? parsed : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private BigDecimal maxPositiveAmount(BigDecimal... values) {
        BigDecimal max = null;
        for (BigDecimal value : values) {
            if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (max == null || value.compareTo(max) > 0) {
                max = value;
            }
        }
        return max;
    }

    private String formatOptionalAmount(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String normalized = normalizeOptional(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private List<BillEntryDTO> mergeBillEntries(List<BillEntryDTO> indexedEntries,
                                                List<BillEntryDTO> fallbackEntries,
                                                int normalizedLimit) {
        Map<String, BillEntryDTO> mergedByTradeOrderNo = new LinkedHashMap<>();
        List<BillEntryDTO> entriesWithoutTradeOrderNo = new ArrayList<>();

        appendBillEntries(indexedEntries, mergedByTradeOrderNo, entriesWithoutTradeOrderNo, true);
        appendBillEntries(fallbackEntries, mergedByTradeOrderNo, entriesWithoutTradeOrderNo, false);

        List<BillEntryDTO> mergedEntries = new ArrayList<>(mergedByTradeOrderNo.values());
        mergedEntries.addAll(entriesWithoutTradeOrderNo);

        return mergedEntries.stream()
                .sorted(Comparator
                        .comparing(BillEntryDTO::tradeTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(BillEntryDTO::tradeOrderNo, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(normalizedLimit)
                .toList();
    }

    private void appendBillEntries(List<BillEntryDTO> source,
                                   Map<String, BillEntryDTO> mergedByTradeOrderNo,
                                   List<BillEntryDTO> entriesWithoutTradeOrderNo,
                                   boolean overwriteExisting) {
        for (BillEntryDTO entry : source) {
            String tradeOrderNo = normalizeOptional(entry.tradeOrderNo());
            if (tradeOrderNo == null) {
                entriesWithoutTradeOrderNo.add(entry);
                continue;
            }
            if (overwriteExisting) {
                mergedByTradeOrderNo.put(tradeOrderNo, entry);
            } else {
                mergedByTradeOrderNo.putIfAbsent(tradeOrderNo, entry);
            }
        }
    }

    private boolean matchesBillMonth(TradeOrder tradeOrder, String normalizedBillMonth) {
        if (normalizedBillMonth == null) {
            return true;
        }
        return normalizedBillMonth.equals(resolveBillMonthByTradeTime(resolveBillEntryTradeTime(tradeOrder)));
    }

    private boolean matchesBusinessDomainCode(TradeOrder tradeOrder, TradeBusinessDomainCode normalizedBusinessDomainCode) {
        if (normalizedBusinessDomainCode == null) {
            return true;
        }
        return normalizedBusinessDomainCode.name().equals(resolveFallbackBusinessDomainCode(tradeOrder));
    }

    private String resolveFallbackBusinessDomainCode(TradeOrder tradeOrder) {
        String raw = normalizeOptional(tradeOrder.getBusinessDomainCode());
        if (raw == null) {
            return TradeBusinessDomainCode.TRADE.name();
        }
        try {
            return TradeBusinessDomainCode.from(raw.toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException ignored) {
            return TradeBusinessDomainCode.TRADE.name();
        }
    }

    private LocalDateTime resolveBillEntryTradeTime(TradeOrder tradeOrder) {
        return tradeOrder.getUpdatedAt() == null ? tradeOrder.getCreatedAt() : tradeOrder.getUpdatedAt();
    }

    private String resolveBillMonthByTradeTime(LocalDateTime tradeTime) {
        if (tradeTime == null) {
            return null;
        }
        int month = tradeTime.getMonthValue();
        return tradeTime.getYear() + "-" + (month < 10 ? "0" : "") + month;
    }

    private String normalizeBrandDisplayText(String raw) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            return null;
        }
        return normalized
                .replace("爱付", "爱付")
                .replace("AiPay", "爱付")
                .replace("aipay", "爱付")
                .replace("AiPay", "爱付")
                .replace("爱存", "爱存")
                .replace("AiCash", "爱存")
                .replace("AiCash", "爱存")
                .replace("aicash", "爱存")
                .replace("AiCash", "爱存")
                .replace("爱花", "爱花")
                .replace("AiCredit", "爱花")
                .replace("aicredit", "爱花")
                .replace("AiCredit", "爱花")
                .replace("爱借", "爱借")
                .replace("AiLoan", "爱借")
                .replace("ailoan", "爱借")
                .replace("AiLoan", "爱借");
    }

    private record BillCursor(
            /** 游标时间 */
            LocalDateTime tradeTime,
            /** 游标ID */
            Long cursorId
    ) {
    }
}
