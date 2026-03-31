package cn.openaipay.application.cashier.service;

import cn.openaipay.application.cashier.dto.CashierPricingPreviewDTO;
import cn.openaipay.application.cashier.dto.CashierViewDTO;
import org.joda.money.Money;

/**
 * 收银台应用服务接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface CashierService {

    /**
     * 查询业务数据。
     */
    CashierViewDTO queryCashier(Long userId, String sceneCode);

    /**
     * 处理计费信息。
     */
    CashierPricingPreviewDTO previewPricing(Long userId, String sceneCode, String paymentMethod, Money amount);
}
