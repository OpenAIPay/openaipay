package cn.openaipay.application.cashier.facade;

import cn.openaipay.application.cashier.dto.CashierPricingPreviewDTO;
import cn.openaipay.application.cashier.dto.CashierViewDTO;
import org.joda.money.Money;

/**
 * 收银台门面接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface CashierFacade {

    /**
     * 查询业务数据。
     */
    CashierViewDTO queryCashier(Long userId, String sceneCode);

    /**
     * 处理计费信息。
     */
    CashierPricingPreviewDTO previewPricing(Long userId, String sceneCode, String paymentMethod, Money amount);
}
