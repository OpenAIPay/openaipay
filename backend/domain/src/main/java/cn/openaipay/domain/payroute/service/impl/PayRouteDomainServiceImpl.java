package cn.openaipay.domain.payroute.service.impl;

import cn.openaipay.domain.creditaccount.model.CreditProductCodes;
import cn.openaipay.domain.payroute.service.CreditPayRoutePlan;
import cn.openaipay.domain.payroute.service.PayRouteDomainService;
import java.util.Locale;

/**
 * 支付产品路由领域服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class PayRouteDomainServiceImpl implements PayRouteDomainService {

    /** 业务域信息 */
    private static final String BUSINESS_DOMAIN_AICREDIT = CreditProductCodes.AICREDIT;
    /** 业务域信息 */
    private static final String BUSINESS_DOMAIN_AILOAN = CreditProductCodes.AILOAN;
    /** 产品信息 */
    private static final String PRODUCT_AICREDIT = CreditProductCodes.AICREDIT;
    /** 产品信息 */
    private static final String PRODUCT_AILOAN = CreditProductCodes.AILOAN;
    /** 信用信息 */
    private static final String ACCOUNT_MODULE_CREDIT = "CREDIT_ACCOUNT";
    /** 借款信息 */
    private static final String ACCOUNT_MODULE_LOAN = "LOAN_ACCOUNT";
    /** 类型 */
    private static final String ACCOUNT_TYPE_AICREDIT = "AICREDIT";
    /** 类型借款信息 */
    private static final String ACCOUNT_TYPE_LOAN = "LOAN_ACCOUNT";
    /** 操作放款信息 */
    private static final String OPERATION_LEND = "LEND";
    /** 操作还款信息 */
    private static final String OPERATION_REPAY = "REPAY";
    /** 资源信息 */
    private static final String ASSET_CATEGORY_PRINCIPAL = "PRINCIPAL";

    /**
     * 处理信用用于支付信息。
     */
    @Override
    public CreditPayRoutePlan routeCreditForPay(String businessSceneCode,
                                                String paymentMethod,
                                                Long payerUserId,
                                                Long payeeUserId) {
        boolean repayScene = isCreditRepayScene(businessSceneCode);
        Long accountOwnerUserId = resolveAccountOwnerUserId(repayScene, payerUserId, payeeUserId);
        if (containsLoanKeyword(paymentMethod) || containsLoanKeyword(businessSceneCode)) {
            return buildPlan(BUSINESS_DOMAIN_AILOAN, accountOwnerUserId, repayScene);
        }
        return buildPlan(BUSINESS_DOMAIN_AICREDIT, accountOwnerUserId, repayScene);
    }

    /**
     * 处理信用用于交易信息。
     */
    @Override
    public CreditPayRoutePlan routeCreditForTrade(String businessDomainCode,
                                                  String businessSceneCode,
                                                  String paymentMethod,
                                                  Long payerUserId,
                                                  Long payeeUserId) {
        boolean repayScene = isCreditRepayScene(businessSceneCode);
        Long accountOwnerUserId = resolveAccountOwnerUserId(repayScene, payerUserId, payeeUserId);
        String normalizedBusinessDomainCode = CreditProductCodes.normalizeNullable(businessDomainCode);
        if (BUSINESS_DOMAIN_AICREDIT.equals(normalizedBusinessDomainCode)
                || BUSINESS_DOMAIN_AILOAN.equals(normalizedBusinessDomainCode)) {
            return buildPlan(normalizedBusinessDomainCode, accountOwnerUserId, repayScene);
        }
        if (containsLoanKeyword(paymentMethod) || containsLoanKeyword(businessSceneCode)) {
            return buildPlan(BUSINESS_DOMAIN_AILOAN, accountOwnerUserId, repayScene);
        }
        if (containsAiCreditKeyword(paymentMethod)
                || containsAiCreditKeyword(businessSceneCode)
                || containsGenericCreditKeyword(paymentMethod)
                || containsGenericCreditKeyword(businessSceneCode)
                || repayScene) {
            return buildPlan(BUSINESS_DOMAIN_AICREDIT, accountOwnerUserId, repayScene);
        }
        return null;
    }

    private CreditPayRoutePlan buildPlan(String businessDomainCode,
                                         Long accountOwnerUserId,
                                         boolean repayScene) {
        String operationType = repayScene ? OPERATION_REPAY : OPERATION_LEND;
        if (BUSINESS_DOMAIN_AILOAN.equals(businessDomainCode)) {
            return new CreditPayRoutePlan(
                    BUSINESS_DOMAIN_AILOAN,
                    PRODUCT_AILOAN,
                    ACCOUNT_MODULE_LOAN,
                    ACCOUNT_TYPE_LOAN,
                    accountOwnerUserId,
                    operationType,
                    ASSET_CATEGORY_PRINCIPAL
            );
        }
        return new CreditPayRoutePlan(
                BUSINESS_DOMAIN_AICREDIT,
                PRODUCT_AICREDIT,
                ACCOUNT_MODULE_CREDIT,
                ACCOUNT_TYPE_AICREDIT,
                accountOwnerUserId,
                operationType,
                ASSET_CATEGORY_PRINCIPAL
        );
    }

    private Long resolveAccountOwnerUserId(boolean repayScene, Long payerUserId, Long payeeUserId) {
        if (repayScene && payeeUserId != null && payeeUserId > 0) {
            return payeeUserId;
        }
        return requirePositive(payerUserId, "payerUserId");
    }

    private boolean containsLoanKeyword(String rawValue) {
        String upper = normalizeUpper(rawValue);
        return upper.contains("LOAN_ACCOUNT")
                || upper.contains(CreditProductCodes.AILOAN)
                || upper.contains("AILOAN")
                || upper.contains("LOAN");
    }

    private boolean containsAiCreditKeyword(String rawValue) {
        String upper = normalizeUpper(rawValue);
        return upper.contains(CreditProductCodes.AICREDIT);
    }

    private boolean containsGenericCreditKeyword(String rawValue) {
        String upper = normalizeUpper(rawValue);
        return upper.contains("CREDIT_ACCOUNT") || upper.contains("CREDIT");
    }

    private boolean isCreditRepayScene(String rawValue) {
        String upper = normalizeUpper(rawValue);
        return "APP_CREDIT_REPAY".equals(upper)
                || (upper.contains("CREDIT") && upper.contains("REPAY"))
                || (upper.contains(CreditProductCodes.AICREDIT) && upper.contains("REPAY"))
                || (upper.contains("AICREDIT") && upper.contains("REPAY"))
                || (upper.contains("LOAN") && upper.contains("REPAY"))
                || (upper.contains(CreditProductCodes.AILOAN) && upper.contains("REPAY"))
                || (upper.contains("AILOAN") && upper.contains("REPAY"));
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private String normalizeUpper(String rawValue) {
        if (rawValue == null) {
            return "";
        }
        String trimmed = rawValue.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }
}
