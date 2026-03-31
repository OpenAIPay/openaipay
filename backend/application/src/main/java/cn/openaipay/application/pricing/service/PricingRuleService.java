package cn.openaipay.application.pricing.service;

import cn.openaipay.application.pricing.command.ChangePricingRuleStatusCommand;
import cn.openaipay.application.pricing.command.CreatePricingRuleCommand;
import cn.openaipay.application.pricing.command.UpdatePricingRuleCommand;
import cn.openaipay.application.pricing.dto.PricingRuleDTO;

import java.util.List;
/**
 * Pricing规则应用服务接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface PricingRuleService {

    /**
     * 创建规则。
     */
    PricingRuleDTO createRule(CreatePricingRuleCommand command);

    /**
     * 更新规则。
     */
    PricingRuleDTO updateRule(UpdatePricingRuleCommand command);

    /**
     * 处理规则状态。
     */
    PricingRuleDTO changeRuleStatus(ChangePricingRuleStatusCommand command);

    /**
     * 获取规则。
     */
    PricingRuleDTO getRule(Long ruleId);

    /**
     * 查询规则列表。
     */
    List<PricingRuleDTO> listRules(String businessSceneCode, String paymentMethod, String status);
}
