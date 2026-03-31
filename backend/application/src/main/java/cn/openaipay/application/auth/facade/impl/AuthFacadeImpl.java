package cn.openaipay.application.auth.facade.impl;

import cn.openaipay.application.auth.command.LoginCommand;
import cn.openaipay.application.auth.dto.LoginPresetAccountDTO;
import cn.openaipay.application.auth.dto.LoginResultDTO;
import cn.openaipay.application.auth.facade.AuthFacade;
import cn.openaipay.application.auth.service.AuthService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 认证门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Service
public class AuthFacadeImpl implements AuthFacade {

    /** 认证信息 */
    private final AuthService authService;

    public AuthFacadeImpl(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 本机号码验证登录（免密）。
     */
    @Override
    public LoginResultDTO mobileVerifyLogin(LoginCommand command) {
        return authService.mobileVerifyLogin(command);
    }

    /**
     * 查询登录下拉账号。
     */
    @Override
    public List<LoginPresetAccountDTO> listPresetLoginAccounts(String deviceId, List<String> legacyDeviceIds) {
        return authService.listPresetLoginAccounts(deviceId, legacyDeviceIds);
    }
}
