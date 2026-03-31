package cn.openaipay.application.admin.dto;

import java.util.List;
/**
 * 后台管理登录Result数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AdminLoginResultDTO(
        /** 访问令牌 */
        String accessToken,
        /** 业务类型 */
        String tokenType,
        /** expiresINseconds信息 */
        long expiresInSeconds,
        /** 后台ID */
        String adminId,
        /** 用户名 */
        String username,
        /** 展示名称 */
        String displayName,
        /** 角色列表 */
        List<String> roles,
        /** 权限列表 */
        List<String> permissions
) {
}
