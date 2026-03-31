package cn.openaipay.infrastructure.pricing;

import cn.openaipay.domain.pricing.model.PricingFeeBearer;
import cn.openaipay.domain.pricing.model.PricingFeeMode;
import cn.openaipay.domain.pricing.model.PricingQuote;
import cn.openaipay.domain.pricing.model.PricingRule;
import cn.openaipay.domain.pricing.model.PricingRuleStatus;
import cn.openaipay.domain.pricing.repository.PricingRepository;
import cn.openaipay.infrastructure.pricing.dataobject.PricingQuoteDO;
import cn.openaipay.infrastructure.pricing.dataobject.PricingRuleDO;
import cn.openaipay.infrastructure.pricing.mapper.PricingQuoteMapper;
import cn.openaipay.infrastructure.pricing.mapper.PricingRuleMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Pricing仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class PricingRepositoryImpl implements PricingRepository {

    /** PricingRuleMapper组件 */
    private final PricingRuleMapper pricingRuleMapper;
    /** PricingQuoteMapper组件 */
    private final PricingQuoteMapper pricingQuoteMapper;

    public PricingRepositoryImpl(PricingRuleMapper pricingRuleMapper,
                                 PricingQuoteMapper pricingQuoteMapper) {
        this.pricingRuleMapper = pricingRuleMapper;
        this.pricingQuoteMapper = pricingQuoteMapper;
    }

    /**
     * 保存规则。
     */
    @Override
    @Transactional
    public PricingRule saveRule(PricingRule rule) {
        PricingRuleDO entity = rule.getRuleId() == null
                ? pricingRuleMapper.findByRuleCode(rule.getRuleCode()).orElse(new PricingRuleDO())
                : pricingRuleMapper.findById(rule.getRuleId()).orElse(new PricingRuleDO());
        fillRuleDO(entity, rule);
        return toDomainRule(pricingRuleMapper.save(entity));
    }

    /**
     * 按ID查找规则。
     */
    @Override
    public Optional<PricingRule> findRuleById(Long ruleId) {
        return pricingRuleMapper.findById(ruleId).map(this::toDomainRule);
    }

    /**
     * 按编码查找规则。
     */
    @Override
    public Optional<PricingRule> findRuleByCode(String ruleCode) {
        return pricingRuleMapper.findByRuleCode(ruleCode).map(this::toDomainRule);
    }

    /**
     * 查找规则。
     */
    @Override
    public List<PricingRule> findRules(String businessSceneCode, String paymentMethod, String status) {
        return pricingRuleMapper.findByFilters(businessSceneCode, paymentMethod, status)
                .stream()
                .map(this::toDomainRule)
                .toList();
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public PricingQuote saveQuote(PricingQuote quote) {
        if (quote.getQuoteId() == null) {
            PricingQuoteDO entity = new PricingQuoteDO();
            fillQuoteDO(entity, quote);
            pricingQuoteMapper.insert(entity);
            return toDomainQuote(entity);
        }

        PricingQuoteDO entity = pricingQuoteMapper.findById(quote.getQuoteId()).orElse(new PricingQuoteDO());
        fillQuoteDO(entity, quote);
        return toDomainQuote(pricingQuoteMapper.save(entity));
    }

    /**
     * 按单号查找记录。
     */
    @Override
    public Optional<PricingQuote> findQuoteByQuoteNo(String quoteNo) {
        return pricingQuoteMapper.findByQuoteNo(quoteNo).map(this::toDomainQuote);
    }

    /**
     * 按请求单号查找记录。
     */
    @Override
    public Optional<PricingQuote> findQuoteByRequestNo(String requestNo) {
        return pricingQuoteMapper.findByRequestNo(requestNo).map(this::toDomainQuote);
    }

    private PricingRule toDomainRule(PricingRuleDO entity) {
        return new PricingRule(
                entity.getId(),
                entity.getRuleCode(),
                entity.getRuleName(),
                entity.getBusinessSceneCode(),
                entity.getPaymentMethod(),
                entity.getCurrencyCode(),
                PricingFeeMode.from(entity.getFeeMode()),
                entity.getFeeRate(),
                entity.getFixedFee(),
                entity.getMinFee(),
                entity.getMaxFee(),
                PricingFeeBearer.from(entity.getFeeBearer()),
                entity.getPriority(),
                PricingRuleStatus.from(entity.getStatus()),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.getRulePayload(),
                entity.getCreatedBy(),
                entity.getUpdatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private PricingQuote toDomainQuote(PricingQuoteDO entity) {
        return new PricingQuote(
                entity.getId(),
                entity.getQuoteNo(),
                entity.getRequestNo(),
                entity.getRuleId(),
                entity.getRuleCode(),
                entity.getRuleName(),
                entity.getBusinessSceneCode(),
                entity.getPaymentMethod(),
                entity.getOriginalAmount(),
                entity.getFeeAmount(),
                entity.getPayableAmount(),
                entity.getSettleAmount(),
                PricingFeeMode.from(entity.getFeeMode()),
                PricingFeeBearer.from(entity.getFeeBearer()),
                entity.getFeeRate(),
                entity.getFixedFee(),
                entity.getRulePayload(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillRuleDO(PricingRuleDO entity, PricingRule rule) {
        LocalDateTime now = LocalDateTime.now();
        entity.setRuleCode(rule.getRuleCode());
        entity.setRuleName(rule.getRuleName());
        entity.setBusinessSceneCode(rule.getBusinessSceneCode());
        entity.setPaymentMethod(rule.getPaymentMethod());
        entity.setCurrencyCode(rule.getCurrencyCode());
        entity.setFeeMode(rule.getFeeMode().name());
        entity.setFeeRate(rule.getFeeRate());
        entity.setFixedFee(rule.getFixedFee());
        entity.setMinFee(rule.getMinFee());
        entity.setMaxFee(rule.getMaxFee());
        entity.setFeeBearer(rule.getFeeBearer().name());
        entity.setPriority(rule.getPriority());
        entity.setStatus(rule.getStatus().name());
        entity.setValidFrom(rule.getValidFrom());
        entity.setValidTo(rule.getValidTo());
        entity.setRulePayload(rule.getRulePayload());
        entity.setCreatedBy(rule.getCreatedBy());
        entity.setUpdatedBy(rule.getUpdatedBy());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(rule.getCreatedAt() == null ? now : rule.getCreatedAt());
        }
        entity.setUpdatedAt(rule.getUpdatedAt() == null ? now : rule.getUpdatedAt());
    }

    private void fillQuoteDO(PricingQuoteDO entity, PricingQuote quote) {
        LocalDateTime now = LocalDateTime.now();
        entity.setQuoteNo(quote.getQuoteNo());
        entity.setRequestNo(quote.getRequestNo());
        entity.setRuleId(quote.getRuleId());
        entity.setRuleCode(quote.getRuleCode());
        entity.setRuleName(quote.getRuleName());
        entity.setBusinessSceneCode(quote.getBusinessSceneCode());
        entity.setPaymentMethod(quote.getPaymentMethod());
        entity.setOriginalAmount(quote.getOriginalAmount());
        entity.setFeeAmount(quote.getFeeAmount());
        entity.setPayableAmount(quote.getPayableAmount());
        entity.setSettleAmount(quote.getSettleAmount());
        entity.setFeeMode(quote.getFeeMode().name());
        entity.setFeeBearer(quote.getFeeBearer().name());
        entity.setFeeRate(quote.getFeeRate());
        entity.setFixedFee(quote.getFixedFee());
        entity.setRulePayload(quote.getRulePayload());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(quote.getCreatedAt() == null ? now : quote.getCreatedAt());
        }
        entity.setUpdatedAt(quote.getUpdatedAt() == null ? now : quote.getUpdatedAt());
    }
}
