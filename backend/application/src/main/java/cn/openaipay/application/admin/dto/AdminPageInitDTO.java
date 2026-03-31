package cn.openaipay.application.admin.dto;

import cn.openaipay.application.coupon.dto.CouponOpsSummaryDTO;

import java.util.List;
/**
 * 后台管理页面初始化数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AdminPageInitDTO(
        /** 后台信息 */
        AdminProfileDTO admin,
        /** 菜单列表 */
        List<AdminMenuDTO> menus,
        /** 优惠券汇总信息 */
        CouponOpsSummaryDTO couponSummary,
        /** 角色列表 */
        List<String> roles,
        /** 权限列表 */
        List<String> permissions
) {
}
