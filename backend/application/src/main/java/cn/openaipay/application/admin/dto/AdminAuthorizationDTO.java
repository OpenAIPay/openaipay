package cn.openaipay.application.admin.dto;

import java.util.List;
/**
 * 后台管理Authorization数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AdminAuthorizationDTO(
        /** 后台ID */
        String adminId,
        /** 角色信息 */
        List<String> roleCodes,
        /** 权限信息 */
        List<String> permissionCodes,
        /** 菜单信息 */
        List<String> menuCodes
) {
}
