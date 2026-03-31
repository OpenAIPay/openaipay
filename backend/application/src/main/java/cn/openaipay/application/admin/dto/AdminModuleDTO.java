package cn.openaipay.application.admin.dto;
/**
 * 后台管理模块数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AdminModuleDTO(
        /** 业务编码 */
        String moduleCode,
        /** 业务名称 */
        String moduleName,
        /** 模块描述 */
        String moduleDesc,
        /** 启用标记 */
        boolean enabled,
        /** 业务单号 */
        Integer sortNo
) {
}
