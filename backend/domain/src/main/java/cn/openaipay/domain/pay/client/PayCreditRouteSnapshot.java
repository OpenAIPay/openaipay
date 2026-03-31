package cn.openaipay.domain.pay.client;

/**
 * PayCreditRouteSnapshot 记录模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record PayCreditRouteSnapshot(
        /** 业务域编码 */
        String businessDomainCode,
        /** 产品编码 */
        String productCode,
        /** 业务编码 */
        String accountModuleCode,
        /** 类型编码 */
        String accountTypeCode,
        /** 所属用户ID */
        Long accountOwnerUserId,
        /** 业务单号 */
        String accountNo,
        /** 业务类型 */
        String operationType,
        /** 资源信息 */
        String assetCategory
) {
}
