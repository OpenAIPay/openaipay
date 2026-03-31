package cn.openaipay.application.agreement.dto;

/**
 * 协议模板DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record AgreementTemplateDTO(
        /** 模板编码 */
        String templateCode,
        /** 模板版本号 */
        String templateVersion,
        /** 业务类型 */
        String bizType,
        /** 标题 */
        String title,
        /** 业务地址 */
        String contentUrl,
        /** 内容摘要 */
        String contentHash,
        /** 必填标记 */
        boolean required
) {
}
