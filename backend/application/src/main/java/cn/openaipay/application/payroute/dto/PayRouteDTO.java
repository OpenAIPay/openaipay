package cn.openaipay.application.payroute.dto;

/**
 * 支付产品路由结果。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record PayRouteDTO(
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
        /** 实际应操作的账户号。 */
        String accountNo,
        /** 授信链路操作类型：LEND/REPAY。 */
        String operationType,
        /** 授信链路资产类型。 */
        String assetCategory
) {
}
