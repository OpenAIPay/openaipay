package cn.openaipay.application.adminuser.facade.impl;

import cn.openaipay.application.adminuser.dto.AdminUserCenterSummaryDTO;
import cn.openaipay.application.adminuser.dto.AdminUserDetailDTO;
import cn.openaipay.application.adminuser.dto.AdminUserSummaryDTO;
import cn.openaipay.application.adminuser.facade.AdminUserManageFacade;
import cn.openaipay.application.adminuser.service.AdminUserManageService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户管理门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Service
public class AdminUserManageFacadeImpl implements AdminUserManageFacade {

    private final AdminUserManageService adminUserManageService;

    public AdminUserManageFacadeImpl(AdminUserManageService adminUserManageService) {
        this.adminUserManageService = adminUserManageService;
    }

    @Override
    public AdminUserCenterSummaryDTO summary() {
        return adminUserManageService.summary();
    }

    @Override
    public List<AdminUserSummaryDTO> listUsers(String keyword, String accountStatus, String kycLevel, Integer pageNo, Integer pageSize) {
        return adminUserManageService.listUsers(keyword, accountStatus, kycLevel, pageNo, pageSize);
    }

    @Override
    public AdminUserDetailDTO getUserDetail(Long userId) {
        return adminUserManageService.getUserDetail(userId);
    }

    @Override
    public AdminUserSummaryDTO changeUserStatus(Long userId, String status) {
        return adminUserManageService.changeUserStatus(userId, status);
    }
}
