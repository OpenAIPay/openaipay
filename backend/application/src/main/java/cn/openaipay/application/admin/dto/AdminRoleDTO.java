package cn.openaipay.application.admin.dto;

import java.util.List;
/**
 * 后台管理角色数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AdminRoleDTO(
        /** 角色编码 */
        String roleCode,
        /** 角色名称 */
        String roleName,
        /** 角色信息 */
        String roleScope,
        /** 角色状态 */
        String roleStatus,
        /** 内置标记 */
        boolean builtin,
        /** 角色信息 */
        String roleDesc,
        /** 权限信息 */
        List<String> permissionCodes,
        /** 菜单信息 */
        List<String> menuCodes
) {
}
