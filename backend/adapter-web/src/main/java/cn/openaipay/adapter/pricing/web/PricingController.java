package cn.openaipay.adapter.pricing.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.pricing.web.request.PricingQuoteRequest;
import cn.openaipay.application.pricing.command.PricingQuoteCommand;
import cn.openaipay.application.pricing.dto.PricingQuoteDTO;
import cn.openaipay.application.pricing.facade.PricingFacade;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pricing控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/pricing/quotes")
public class PricingController {

    /** PricingFacade组件 */
    private final PricingFacade pricingFacade;

    public PricingController(PricingFacade pricingFacade) {
        this.pricingFacade = pricingFacade;
    }

    /**
     * 处理业务数据。
     */
    @PostMapping
    public ApiResponse<PricingQuoteDTO> quote(@Valid @RequestBody PricingQuoteRequest request) {
        PricingQuoteDTO result = pricingFacade.quote(new PricingQuoteCommand(
                request.requestNo(),
                request.businessSceneCode(),
                request.paymentMethod(),
                request.originalAmount()
        ));
        return ApiResponse.success(result);
    }

    /**
     * 获取业务数据。
     */
    @GetMapping("/{quoteNo}")
    public ApiResponse<PricingQuoteDTO> getQuote(@PathVariable("quoteNo") String quoteNo) {
        return ApiResponse.success(pricingFacade.getQuote(quoteNo));
    }

    /**
     * 按请求单号获取记录。
     */
    @GetMapping("/by-request/{requestNo}")
    public ApiResponse<PricingQuoteDTO> getQuoteByRequestNo(@PathVariable("requestNo") String requestNo) {
        return ApiResponse.success(pricingFacade.getQuoteByRequestNo(requestNo));
    }
}
