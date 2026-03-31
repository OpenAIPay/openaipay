package cn.openaipay.adapter.cashier.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.cashier.dto.CashierPricingPreviewDTO;
import cn.openaipay.application.cashier.dto.CashierViewDTO;
import cn.openaipay.application.cashier.facade.CashierFacade;
import java.math.BigDecimal;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收银台控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/cashier")
public class CashierController {

    /** CashierFacade组件 */
    private final CashierFacade cashierFacade;

    public CashierController(CashierFacade cashierFacade) {
        this.cashierFacade = cashierFacade;
    }

    /**
     * 查询业务数据。
     */
    @GetMapping("/users/{userId}/payment-tools")
    public ApiResponse<CashierViewDTO> queryCashier(@PathVariable("userId") Long userId,
                                                    @RequestParam(value = "sceneCode", required = false) String sceneCode) {
        return ApiResponse.success(cashierFacade.queryCashier(userId, sceneCode));
    }

    /**
     * 处理计费信息。
     */
    @GetMapping("/users/{userId}/pricing-preview")
    public ApiResponse<CashierPricingPreviewDTO> previewPricing(@PathVariable("userId") Long userId,
                                                                @RequestParam(value = "sceneCode", required = false) String sceneCode,
                                                                @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
                                                                @RequestParam("amount") BigDecimal amount,
                                                                @RequestParam(value = "currencyCode", required = false, defaultValue = "CNY") String currencyCode) {
        CurrencyUnit currency = CurrencyUnit.of(currencyCode == null || currencyCode.isBlank() ? "CNY" : currencyCode.trim().toUpperCase());
        Money money = Money.of(currency, amount == null ? BigDecimal.ZERO : amount);
        return ApiResponse.success(cashierFacade.previewPricing(userId, sceneCode, paymentMethod, money));
    }
}
