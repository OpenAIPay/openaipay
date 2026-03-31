package cn.openaipay.application.pricing.service.impl;

import cn.openaipay.application.pricing.command.PricingQuoteCommand;
import cn.openaipay.application.pricing.dto.PricingQuoteDTO;
import cn.openaipay.application.pricing.service.PricingService;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.pricing.model.PricingQuote;
import cn.openaipay.domain.pricing.model.PricingRule;
import cn.openaipay.domain.pricing.model.PricingRuleStatus;
import cn.openaipay.domain.pricing.repository.PricingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.joda.money.Money;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pricing应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class PricingServiceImpl implements PricingService {
    /** 计费规则缓存TTL（毫秒） */
    private static final long PRICING_RULE_CACHE_TTL_MILLIS = 30_000L;
    /** 定价单号业务类型编码 */
    private static final String QUOTE_ID_BIZ_TYPE = "94";

    /** PricingRepository组件 */
    private final PricingRepository pricingRepository;
    /** 全局ID生成器 */
    private final AiPayIdGenerator aiPayIdGenerator;
    /** 计费规则缓存 */
    private final Map<String, CachedPricingRules> pricingRuleCache = new ConcurrentHashMap<>();

    public PricingServiceImpl(PricingRepository pricingRepository,
                              AiPayIdGenerator aiPayIdGenerator) {
        this.pricingRepository = pricingRepository;
        this.aiPayIdGenerator = aiPayIdGenerator;
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional
    public PricingQuoteDTO quote(PricingQuoteCommand command) {
        String requestNo = normalizeRequired(command.requestNo(), "requestNo");
        PricingQuote existingQuote = pricingRepository.findQuoteByRequestNo(requestNo).orElse(null);
        if (existingQuote != null) {
            return toQuoteDTO(existingQuote);
        }

        String businessSceneCode = normalizeDimension(command.businessSceneCode(), "businessSceneCode");
        String paymentMethod = normalizeDimension(command.paymentMethod(), "paymentMethod");
        Money originalAmount = normalizeAmount(command.originalAmount(), "originalAmount");
        String currencyCode = normalizeDimension(originalAmount.getCurrencyUnit().getCode(), "currencyCode");
        LocalDateTime now = LocalDateTime.now();

        PricingRule matchedRule = pickBestRule(businessSceneCode, paymentMethod, currencyCode, now);
        PricingQuote quote = PricingQuote.create(
                buildQuoteNo(),
                requestNo,
                matchedRule,
                originalAmount,
                now
        );
        return toQuoteDTO(pricingRepository.saveQuote(quote));
    }

    /**
     * 获取业务数据。
     */
    @Override
    @Transactional(readOnly = true)
    public PricingQuoteDTO getQuote(String quoteNo) {
        PricingQuote quote = pricingRepository.findQuoteByQuoteNo(normalizeRequired(quoteNo, "quoteNo"))
                .orElseThrow(() -> new NoSuchElementException("pricing quote not found: " + quoteNo));
        return toQuoteDTO(quote);
    }

    /**
     * 按请求单号获取记录。
     */
    @Override
    @Transactional(readOnly = true)
    public PricingQuoteDTO getQuoteByRequestNo(String requestNo) {
        PricingQuote quote = pricingRepository.findQuoteByRequestNo(normalizeRequired(requestNo, "requestNo"))
                .orElseThrow(() -> new NoSuchElementException("pricing quote not found for requestNo: " + requestNo));
        return toQuoteDTO(quote);
    }

    private PricingRule pickBestRule(String businessSceneCode,
                                     String paymentMethod,
                                     String currencyCode,
                                     LocalDateTime now) {
        List<PricingRule> candidates = getCachedRules(
                businessSceneCode,
                paymentMethod,
                PricingRuleStatus.ACTIVE.name()
        );

        return candidates.stream()
                .filter(rule -> rule.matches(businessSceneCode, paymentMethod, currencyCode, now))
                .max(Comparator.comparing(PricingRule::getPriority)
                        .thenComparing(PricingRule::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow(() -> new NoSuchElementException(
                        "no active pricing rule matched for scene=" + businessSceneCode
                                + ", paymentMethod=" + paymentMethod
                                + ", currency=" + currencyCode
                ));
    }

    private List<PricingRule> getCachedRules(String businessSceneCode,
                                             String paymentMethod,
                                             String status) {
        String cacheKey = businessSceneCode + '|' + paymentMethod + '|' + status;
        long nowMillis = System.currentTimeMillis();
        CachedPricingRules cached = pricingRuleCache.get(cacheKey);
        if (cached != null && nowMillis < cached.expireAtMillis()) {
            return cached.rules();
        }

        List<PricingRule> loaded = pricingRepository.findRules(businessSceneCode, paymentMethod, status);
        pricingRuleCache.put(
                cacheKey,
                new CachedPricingRules(List.copyOf(loaded), nowMillis + PRICING_RULE_CACHE_TTL_MILLIS)
        );
        return loaded;
    }

    private PricingQuoteDTO toQuoteDTO(PricingQuote quote) {
        return new PricingQuoteDTO(
                quote.getQuoteNo(),
                quote.getRequestNo(),
                quote.getRuleId(),
                quote.getRuleCode(),
                quote.getRuleName(),
                quote.getBusinessSceneCode(),
                quote.getPaymentMethod(),
                quote.getOriginalAmount(),
                quote.getFeeAmount(),
                quote.getPayableAmount(),
                quote.getSettleAmount(),
                quote.getFeeMode().name(),
                quote.getFeeBearer().name(),
                quote.getFeeRate(),
                quote.getFixedFee(),
                quote.getRulePayload(),
                quote.getCreatedAt()
        );
    }

    private String buildQuoteNo() {
        return "PRQ" + aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_PAY,
                QUOTE_ID_BIZ_TYPE,
                "0"
        );
    }

    private String normalizeRequired(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return raw.trim();
    }

    private String normalizeDimension(String raw, String fieldName) {
        String normalized = normalizeRequired(raw, fieldName).toUpperCase(Locale.ROOT);
        if (normalized.length() > 64) {
            throw new IllegalArgumentException(fieldName + " length must be <= 64");
        }
        return normalized;
    }

    private Money normalizeAmount(Money source, String fieldName) {
        if (source == null || source.getAmount().signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return source.rounded(2, RoundingMode.HALF_UP);
    }

    private record CachedPricingRules(
        /** rules信息 */
        List<PricingRule> rules,
        /** expireATmillis信息 */
        long expireAtMillis
    ) {
    }
}
