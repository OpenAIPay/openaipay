package cn.openaipay.domain.cashier.service;

import cn.openaipay.domain.cashier.model.CashierPayTool;
import cn.openaipay.domain.cashier.model.CashierSceneConfiguration;
import java.util.List;

/**
 * 收银台领域服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface CashierDomainService {

    List<CashierPayTool> buildPayTools(
            CashierSceneConfiguration sceneConfiguration,
            List<CashierBankCardProfile> bankCardProfiles,
            List<CashierRecentPaymentHint> recentPaymentHints
    );

    /**
     * 规范化业务数据。
     */
    String normalizePaymentMethod(String paymentMethod);

    /**
     * 解析计费场景编码。
     */
    String resolvePricingSceneCode(String sceneCode);
}
