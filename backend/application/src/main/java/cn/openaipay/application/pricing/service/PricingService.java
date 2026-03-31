package cn.openaipay.application.pricing.service;

import cn.openaipay.application.pricing.command.PricingQuoteCommand;
import cn.openaipay.application.pricing.dto.PricingQuoteDTO;
/**
 * Pricing应用服务接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface PricingService {

    /**
     * 处理业务数据。
     */
    PricingQuoteDTO quote(PricingQuoteCommand command);

    /**
     * 获取业务数据。
     */
    PricingQuoteDTO getQuote(String quoteNo);

    /**
     * 按请求单号获取记录。
     */
    PricingQuoteDTO getQuoteByRequestNo(String requestNo);
}
