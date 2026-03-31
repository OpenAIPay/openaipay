package cn.openaipay.application.pricing.facade.impl;

import cn.openaipay.application.pricing.command.ChangePricingRuleStatusCommand;
import cn.openaipay.application.pricing.command.CreatePricingRuleCommand;
import cn.openaipay.application.pricing.command.UpdatePricingRuleCommand;
import cn.openaipay.application.pricing.dto.PricingRuleDTO;
import cn.openaipay.application.pricing.facade.PricingRuleFacade;
import cn.openaipay.application.pricing.service.PricingRuleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Pricing规则门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class PricingRuleFacadeImpl implements PricingRuleFacade {

    /** PricingRuleService组件 */
    private final PricingRuleService pricingRuleService;

    public PricingRuleFacadeImpl(PricingRuleService pricingRuleService) {
        this.pricingRuleService = pricingRuleService;
    }

    /**
     * 创建规则。
     */
    @Override
    public PricingRuleDTO createRule(CreatePricingRuleCommand command) {
        return pricingRuleService.createRule(command);
    }

    /**
     * 更新规则。
     */
    @Override
    public PricingRuleDTO updateRule(UpdatePricingRuleCommand command) {
        return pricingRuleService.updateRule(command);
    }

    /**
     * 处理规则状态。
     */
    @Override
    public PricingRuleDTO changeRuleStatus(ChangePricingRuleStatusCommand command) {
        return pricingRuleService.changeRuleStatus(command);
    }

    /**
     * 获取规则。
     */
    @Override
    public PricingRuleDTO getRule(Long ruleId) {
        return pricingRuleService.getRule(ruleId);
    }

    /**
     * 查询规则列表。
     */
    @Override
    public List<PricingRuleDTO> listRules(String businessSceneCode, String paymentMethod, String status) {
        return pricingRuleService.listRules(businessSceneCode, paymentMethod, status);
    }
}
