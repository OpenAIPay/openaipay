package cn.openaipay.application.auth.facade;

import cn.openaipay.application.auth.command.LoginCommand;
import cn.openaipay.application.auth.dto.LoginPresetAccountDTO;
import cn.openaipay.application.auth.dto.LoginResultDTO;
import java.util.List;

/**
 * 认证门面接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface AuthFacade {

    /**
     * 本机号码验证登录（免密）。
     */
    LoginResultDTO mobileVerifyLogin(LoginCommand command);

    /**
     * 查询登录下拉账号。
     */
    List<LoginPresetAccountDTO> listPresetLoginAccounts(String deviceId, List<String> legacyDeviceIds);
}
