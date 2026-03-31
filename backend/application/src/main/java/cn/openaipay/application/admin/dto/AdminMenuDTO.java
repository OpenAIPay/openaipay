package cn.openaipay.application.admin.dto;
/**
 * 后台管理菜单数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AdminMenuDTO(
        /** 菜单编码 */
        String menuCode,
        /** 业务编码 */
        String parentCode,
        /** 菜单名称 */
        String menuName,
        /** 路径 */
        String path,
        /** 图标标识 */
        String icon,
        /** 业务单号 */
        Integer sortNo
) {
}
