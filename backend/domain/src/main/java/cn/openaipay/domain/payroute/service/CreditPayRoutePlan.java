package cn.openaipay.domain.payroute.service;

/**
 * 信用产品路由计划。
 *
 * 业务场景：统一描述爱花/爱借在 pay、trade 等上层编排中的目标模块、账户归属人与操作语义。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record CreditPayRoutePlan(
        /** 业务域编码：AICREDIT/AILOAN。 */
        String businessDomainCode,
        /** 产品编码：AICREDIT/AILOAN。 */
        String productCode,
        /** 账户模块编码：CREDIT_ACCOUNT/LOAN_ACCOUNT。 */
        String accountModuleCode,
        /** 账户类型编码：AICREDIT/LOAN_ACCOUNT。 */
        String accountTypeCode,
        /** 实际应操作的账户所属用户。 */
        Long accountOwnerUserId,
        /** 授信链路操作类型：LEND/REPAY。 */
        String operationType,
        /** 授信链路资产类型。 */
        String assetCategory
) {
}
