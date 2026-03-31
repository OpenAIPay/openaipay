package cn.openaipay.application.admin.facade;

import cn.openaipay.application.admin.command.AdminLoginCommand;
import cn.openaipay.application.admin.dto.AdminLoginResultDTO;

/**
 * 后台管理门面接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface AdminFacade {

    /**
     * 处理登录信息。
     */
    AdminLoginResultDTO login(AdminLoginCommand command);
}
