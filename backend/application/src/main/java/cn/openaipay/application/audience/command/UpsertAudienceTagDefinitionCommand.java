package cn.openaipay.application.audience.command;

/**
 * 保存标签定义命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record UpsertAudienceTagDefinitionCommand(
        /** 标签编码 */
        String tagCode,
        /** 标签名称 */
        String tagName,
        /** 标签类型 */
        String tagType,
        /** 标签值域 */
        String valueScope,
        /** 标签描述 */
        String description,
        /** 是否启用 */
        Boolean enabled
) {
}
