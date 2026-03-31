package cn.openaipay.application.pricing.facade.impl;

import cn.openaipay.application.pricing.command.PricingQuoteCommand;
import cn.openaipay.application.pricing.dto.PricingQuoteDTO;
import cn.openaipay.application.pricing.facade.PricingFacade;
import cn.openaipay.application.pricing.service.PricingService;
import org.springframework.stereotype.Service;

/**
 * Pricing门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class PricingFacadeImpl implements PricingFacade {
    /** 定价应用服务。 */
    private final PricingService pricingService;

    public PricingFacadeImpl(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    /**
     * 处理业务数据。
     */
    @Override
    public PricingQuoteDTO quote(PricingQuoteCommand command) {
        return pricingService.quote(command);
    }

    /**
     * 获取业务数据。
     */
    @Override
    public PricingQuoteDTO getQuote(String quoteNo) {
        return pricingService.getQuote(quoteNo);
    }

    /**
     * 按请求单号获取记录。
     */
    @Override
    public PricingQuoteDTO getQuoteByRequestNo(String requestNo) {
        return pricingService.getQuoteByRequestNo(requestNo);
    }
}
