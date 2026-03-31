package cn.openaipay.application.admin.pageinit.facade;

import cn.openaipay.application.admin.dto.AdminMenuDTO;
import cn.openaipay.application.admin.dto.AdminPageInitDTO;
import java.util.List;

/**
 * 后台页面初始化门面。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface AdminPageInitFacade {

    /**
     * 查询后台页面初始化数据。
     */
    AdminPageInitDTO pageInit(Long adminId);

    /**
     * 查询当前管理员可见菜单。
     */
    List<AdminMenuDTO> listVisibleMenus(Long adminId);
}
