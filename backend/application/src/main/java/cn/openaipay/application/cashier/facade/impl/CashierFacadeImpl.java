package cn.openaipay.application.cashier.facade.impl;

import cn.openaipay.application.cashier.dto.CashierPricingPreviewDTO;
import cn.openaipay.application.cashier.dto.CashierViewDTO;
import cn.openaipay.application.cashier.facade.CashierFacade;
import cn.openaipay.application.cashier.service.CashierService;
import org.joda.money.Money;
import org.springframework.stereotype.Service;

/**
 * 收银台门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class CashierFacadeImpl implements CashierFacade {

    /** CashierService组件 */
    private final CashierService cashierService;

    public CashierFacadeImpl(CashierService cashierService) {
        this.cashierService = cashierService;
    }

    /**
     * 查询收银台信息。
     */
    @Override
    public CashierViewDTO queryCashier(Long userId, String sceneCode) {
        return cashierService.queryCashier(userId, sceneCode);
    }

    /**
     * 处理计费信息。
     */
    @Override
    public CashierPricingPreviewDTO previewPricing(Long userId, String sceneCode, String paymentMethod, Money amount) {
        return cashierService.previewPricing(userId, sceneCode, paymentMethod, amount);
    }
}
