package cn.openaipay.domain.pricing.repository;

import cn.openaipay.domain.pricing.model.PricingQuote;
import cn.openaipay.domain.pricing.model.PricingRule;

import java.util.List;
import java.util.Optional;
/**
 * Pricing仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface PricingRepository {

    /**
     * 保存计费规则。
     */
    PricingRule saveRule(PricingRule rule);

    /**
     * 按规则ID查询计费规则。
     */
    Optional<PricingRule> findRuleById(Long ruleId);

    /**
     * 按规则编码查询计费规则。
     */
    Optional<PricingRule> findRuleByCode(String ruleCode);

    /**
     * 按场景、支付方式和状态查询规则列表。
     */
    List<PricingRule> findRules(String businessSceneCode, String paymentMethod, String status);

    /**
     * 保存计费报价快照。
     */
    PricingQuote saveQuote(PricingQuote quote);

    /**
     * 按报价号查询报价快照。
     */
    Optional<PricingQuote> findQuoteByQuoteNo(String quoteNo);

    /**
     * 按请求号查询报价快照。
     */
    Optional<PricingQuote> findQuoteByRequestNo(String requestNo);
}
