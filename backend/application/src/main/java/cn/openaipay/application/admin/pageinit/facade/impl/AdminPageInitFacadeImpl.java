package cn.openaipay.application.admin.pageinit.facade.impl;

import cn.openaipay.application.admin.dto.AdminMenuDTO;
import cn.openaipay.application.admin.dto.AdminPageInitDTO;
import cn.openaipay.application.admin.pageinit.facade.AdminPageInitFacade;
import cn.openaipay.application.admin.pageinit.service.AdminPageInitService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 后台页面初始化门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Service
public class AdminPageInitFacadeImpl implements AdminPageInitFacade {

    /** 后台信息 */
    private final AdminPageInitService adminPageInitService;

    public AdminPageInitFacadeImpl(AdminPageInitService adminPageInitService) {
        this.adminPageInitService = adminPageInitService;
    }

    /**
     * 处理页面初始化信息。
     */
    @Override
    public AdminPageInitDTO pageInit(Long adminId) {
        return adminPageInitService.pageInit(adminId);
    }

    /**
     * 查询菜单信息列表。
     */
    @Override
    public List<AdminMenuDTO> listVisibleMenus(Long adminId) {
        return adminPageInitService.listVisibleMenus(adminId);
    }
}
