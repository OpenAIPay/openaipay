package cn.openaipay.application.cashier.service.impl;

import cn.openaipay.application.bankcard.dto.BankCardDTO;
import cn.openaipay.application.bankcard.facade.BankCardFacade;
import cn.openaipay.application.cashier.dto.CashierPayToolDTO;
import cn.openaipay.application.cashier.dto.CashierCouponCandidateDTO;
import cn.openaipay.application.cashier.dto.CashierPricingPreviewDTO;
import cn.openaipay.application.cashier.dto.CashierSceneConfigurationDTO;
import cn.openaipay.application.cashier.dto.CashierViewDTO;
import cn.openaipay.application.cashier.service.CashierService;
import cn.openaipay.application.coupon.dto.CouponIssueDTO;
import cn.openaipay.application.coupon.facade.CouponFacade;
import cn.openaipay.application.pricing.command.PricingQuoteCommand;
import cn.openaipay.application.pricing.dto.PricingQuoteDTO;
import cn.openaipay.application.pricing.facade.PricingFacade;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.cashier.service.CashierBankCardProfile;
import cn.openaipay.domain.cashier.service.CashierDomainService;
import cn.openaipay.domain.cashier.service.CashierRecentPaymentHint;
import cn.openaipay.domain.cashier.model.CashierPayTool;
import cn.openaipay.domain.cashier.model.CashierSceneConfiguration;
import cn.openaipay.domain.trade.model.TradeOrder;
import cn.openaipay.domain.trade.repository.TradeRepository;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 收银台应用服务实现，负责协调外部卡信息、交易偏好与定价服务，并把选择策略下沉到领域层。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class CashierServiceImpl implements CashierService {

    private static final Logger log = LoggerFactory.getLogger(CashierServiceImpl.class);

    /** 为“最近使用银行卡”推导默认支付工具时的最大回看交易数。 */
    private static final int RECENT_BANK_CARD_LOOKBACK_LIMIT = 20;
    /** 收银台试算请求号业务类型编码 */
    private static final String CASHIER_PREVIEW_REQUEST_BIZ_TYPE = "95";
    /** 试算时返回的最大红包数量 */
    private static final int PREVIEW_COUPON_LIMIT = 20;

    /** BankCardFacade组件，用于查询用户当前可用银行卡。 */
    private final BankCardFacade bankCardFacade;
    /** TradeRepository组件，用于推导用户最近一次成功使用的银行卡。 */
    private final TradeRepository tradeRepository;
    /** PricingFacade组件，用于提现页手续费试算。 */
    private final PricingFacade pricingFacade;
    /** CouponFacade组件，用于收银台红包试算。 */
    private final CouponFacade couponFacade;
    /** CashierDomainService组件，负责支付工具选择与场景策略。 */
    private final CashierDomainService cashierDomainService;
    /** 全局ID生成器 */
    private final AiPayIdGenerator aiPayIdGenerator;

    public CashierServiceImpl(BankCardFacade bankCardFacade,
                                         TradeRepository tradeRepository,
                                         PricingFacade pricingFacade,
                                         CouponFacade couponFacade,
                                         CashierDomainService cashierDomainService,
                                         AiPayIdGenerator aiPayIdGenerator) {
        this.bankCardFacade = bankCardFacade;
        this.tradeRepository = tradeRepository;
        this.pricingFacade = pricingFacade;
        this.couponFacade = couponFacade;
        this.cashierDomainService = cashierDomainService;
        this.aiPayIdGenerator = aiPayIdGenerator;
    }

    /**
     * 查询业务数据。
     */
    @Override
    @Transactional(readOnly = true)
    public CashierViewDTO queryCashier(Long userId, String sceneCode) {
        Long normalizedUserId = requirePositive(userId, "userId");
        CashierSceneConfiguration sceneConfiguration = CashierSceneConfiguration.resolve(sceneCode);
        List<CashierBankCardProfile> bankCardProfiles = bankCardFacade.listUserActiveBankCards(normalizedUserId).stream()
                .map(this::toBankCardProfile)
                .toList();
        List<CashierRecentPaymentHint> recentPaymentHints = tradeRepository.findRecentSucceededTradesByUserId(
                        normalizedUserId,
                        RECENT_BANK_CARD_LOOKBACK_LIMIT
                ).stream()
                .map(this::toRecentPaymentHint)
                .toList();
        List<CashierPayTool> payTools = cashierDomainService.buildPayTools(
                sceneConfiguration,
                bankCardProfiles,
                recentPaymentHints
        );

        return new CashierViewDTO(
                normalizedUserId,
                sceneConfiguration.sceneCode(),
                toSceneConfigDTO(sceneConfiguration),
                payTools.stream().map(this::toDTO).toList(),
                LocalDateTime.now()
        );
    }

    /**
     * 处理计费信息。
     */
    @Override
    @Transactional
    public CashierPricingPreviewDTO previewPricing(Long userId, String sceneCode, String paymentMethod, Money amount) {
        Long normalizedUserId = requirePositive(userId, "userId");
        CashierSceneConfiguration sceneConfiguration = CashierSceneConfiguration.resolve(sceneCode);
        String normalizedPaymentMethod = cashierDomainService.normalizePaymentMethod(paymentMethod);
        Money normalizedAmount = normalizeAmount(amount, "amount");
        String pricingSceneCode = cashierDomainService.resolvePricingSceneCode(sceneConfiguration.sceneCode());
        PricingQuoteDTO quote = pricingFacade.quote(new PricingQuoteCommand(
                buildPricingPreviewRequestNo(normalizedUserId),
                pricingSceneCode,
                normalizedPaymentMethod,
                normalizedAmount
        ));
        Money payableAmount = quote.payableAmount() == null ? normalizedAmount : quote.payableAmount();
        List<CashierCouponCandidateDTO> availableCoupons = loadAvailableCoupons(normalizedUserId, normalizedAmount, payableAmount);
        CashierCouponCandidateDTO recommendedCoupon = availableCoupons.isEmpty() ? null : availableCoupons.getFirst();
        Money couponDeductAmount = resolveCouponDeductAmount(recommendedCoupon, payableAmount);
        Money payableAfterCoupon = payableAmount.minus(couponDeductAmount);
        return new CashierPricingPreviewDTO(
                normalizedUserId,
                sceneConfiguration.sceneCode(),
                pricingSceneCode,
                normalizedPaymentMethod,
                quote.quoteNo(),
                quote.ruleCode(),
                quote.ruleName(),
                quote.originalAmount(),
                quote.feeAmount(),
                payableAmount,
                quote.settleAmount(),
                quote.feeRate() == null ? BigDecimal.ZERO : quote.feeRate().toBigDecimal(),
                quote.feeBearer(),
                availableCoupons.size(),
                recommendedCoupon == null ? null : recommendedCoupon.couponNo(),
                recommendedCoupon == null ? null : recommendedCoupon.couponAmount(),
                couponDeductAmount,
                payableAfterCoupon,
                availableCoupons
        );
    }

    private List<CashierCouponCandidateDTO> loadAvailableCoupons(Long userId, Money requestAmount, Money payableAmount) {
        try {
            LocalDateTime now = LocalDateTime.now();
            return couponFacade.listUserCoupons(userId).stream()
                    .filter(coupon -> "UNUSED".equalsIgnoreCase(normalizeText(coupon.status())))
                    .filter(coupon -> coupon.couponAmount() != null)
                    .filter(coupon -> coupon.couponAmount().getAmount().compareTo(BigDecimal.ZERO) > 0)
                    .filter(coupon -> isNotExpired(coupon.expireAt(), now.toLocalDate()))
                    .filter(coupon -> hasSameCurrency(coupon.couponAmount(), requestAmount))
                    .filter(coupon -> !coupon.couponAmount().isGreaterThan(payableAmount))
                    .sorted(Comparator
                            .comparing((CouponIssueDTO coupon) -> coupon.couponAmount().getAmount()).reversed()
                            .thenComparing(CouponIssueDTO::expireAt, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(coupon -> normalizeText(coupon.couponNo()), Comparator.nullsLast(Comparator.naturalOrder())))
                    .limit(PREVIEW_COUPON_LIMIT)
                    .map(coupon -> new CashierCouponCandidateDTO(
                            coupon.couponNo(),
                            coupon.couponAmount(),
                            coupon.expireAt()
                    ))
                    .toList();
        } catch (RuntimeException ex) {
            log.warn("cashier coupon preview fallback, userId={}, message={}", userId, ex.getMessage());
            return List.of();
        }
    }

    private boolean hasSameCurrency(Money couponAmount, Money requestAmount) {
        if (couponAmount == null || requestAmount == null) {
            return false;
        }
        return couponAmount.getCurrencyUnit().equals(requestAmount.getCurrencyUnit());
    }

    private boolean isNotExpired(LocalDateTime expireAt, LocalDate today) {
        if (expireAt == null) {
            return true;
        }
        return !expireAt.toLocalDate().isBefore(today);
    }

    private Money resolveCouponDeductAmount(CashierCouponCandidateDTO couponCandidate, Money payableAmount) {
        if (payableAmount == null || payableAmount.isNegative()) {
            throw new IllegalArgumentException("payableAmount must be non-negative");
        }
        if (couponCandidate == null || couponCandidate.couponAmount() == null) {
            return Money.zero(payableAmount.getCurrencyUnit());
        }
        return couponCandidate.couponAmount().isGreaterThan(payableAmount)
                ? payableAmount
                : couponCandidate.couponAmount();
    }

    private String buildPricingPreviewRequestNo(Long userId) {
        return "CQ-" + aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_PAY,
                CASHIER_PREVIEW_REQUEST_BIZ_TYPE,
                String.valueOf(requirePositive(userId, "userId"))
        );
    }

    private CashierBankCardProfile toBankCardProfile(BankCardDTO bankCard) {
        return new CashierBankCardProfile(
                bankCard.cardNo(),
                bankCard.bankCode(),
                bankCard.bankName(),
                bankCard.cardType(),
                bankCard.reservedMobile(),
                bankCard.phoneTailNo(),
                bankCard.defaultCard(),
                bankCard.singleLimit(),
                bankCard.dailyLimit()
        );
    }

    private CashierRecentPaymentHint toRecentPaymentHint(TradeOrder tradeOrder) {
        return new CashierRecentPaymentHint(
                tradeOrder.getPaymentMethod(),
                tradeOrder.getMetadata()
        );
    }

    private CashierSceneConfigurationDTO toSceneConfigDTO(CashierSceneConfiguration sceneConfiguration) {
        return new CashierSceneConfigurationDTO(
                sceneConfiguration.supportedChannels().stream().map(Enum::name).toList(),
                sceneConfiguration.bankCardPolicy().name(),
                sceneConfiguration.emptyBankCardText()
        );
    }

    private CashierPayToolDTO toDTO(CashierPayTool payTool) {
        return new CashierPayToolDTO(
                payTool.getToolType().name(),
                payTool.getToolCode(),
                payTool.getToolName(),
                payTool.getToolDescription(),
                payTool.isDefaultSelected(),
                payTool.getSingleLimit(),
                payTool.getDailyLimit(),
                payTool.getBankCode(),
                payTool.getCardType(),
                payTool.getPhoneTailNo()
        );
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private Money normalizeAmount(Money value, String fieldName) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
