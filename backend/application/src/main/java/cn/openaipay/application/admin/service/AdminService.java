package cn.openaipay.application.admin.service;

import cn.openaipay.application.admin.command.AdminLoginCommand;
import cn.openaipay.application.admin.dto.AdminLoginResultDTO;

/**
 * 后台管理应用服务接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface AdminService {

    /**
     * 处理登录信息。
     */
    AdminLoginResultDTO login(AdminLoginCommand command);
}
