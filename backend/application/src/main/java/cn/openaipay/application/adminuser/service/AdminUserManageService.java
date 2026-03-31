package cn.openaipay.application.adminuser.service;

import cn.openaipay.application.adminuser.dto.AdminUserCenterSummaryDTO;
import cn.openaipay.application.adminuser.dto.AdminUserDetailDTO;
import cn.openaipay.application.adminuser.dto.AdminUserSummaryDTO;

import java.util.List;

/**
 * 用户管理服务
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public interface AdminUserManageService {

    /**
     * 查询用户中心汇总。
     */
    AdminUserCenterSummaryDTO summary();

    /**
     * 查询用户列表。
     */
    List<AdminUserSummaryDTO> listUsers(String keyword, String accountStatus, String kycLevel, Integer pageNo, Integer pageSize);

    /**
     * 查询用户详情。
     */
    AdminUserDetailDTO getUserDetail(Long userId);

    /**
     * 修改用户状态。
     */
    AdminUserSummaryDTO changeUserStatus(Long userId, String status);
}
