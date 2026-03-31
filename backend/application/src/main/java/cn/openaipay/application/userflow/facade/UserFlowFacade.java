package cn.openaipay.application.userflow.facade;

import cn.openaipay.application.userflow.command.RegisterUserCommand;
import cn.openaipay.application.userflow.dto.RegisterPhoneCheckDTO;
import cn.openaipay.application.userflow.dto.UserRegistrationDTO;

/**
 * 用户流程门面接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface UserFlowFacade {

    /**
     * 校验注册手机号状态。
     */
    RegisterPhoneCheckDTO checkRegisterPhone(String loginId, String deviceId);

    /**
     * 执行注册流程。
     */
    UserRegistrationDTO register(RegisterUserCommand command);
}
