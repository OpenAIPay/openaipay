package cn.openaipay.application.admin.facade.impl;

import cn.openaipay.application.admin.command.AdminLoginCommand;
import cn.openaipay.application.admin.dto.AdminLoginResultDTO;
import cn.openaipay.application.admin.facade.AdminFacade;
import cn.openaipay.application.admin.service.AdminService;
import org.springframework.stereotype.Service;

/**
 * 后台管理门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Service
public class AdminFacadeImpl implements AdminFacade {

    /** 后台信息 */
    private final AdminService adminService;

    public AdminFacadeImpl(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 处理登录信息。
     */
    @Override
    public AdminLoginResultDTO login(AdminLoginCommand command) {
        return adminService.login(command);
    }
}
