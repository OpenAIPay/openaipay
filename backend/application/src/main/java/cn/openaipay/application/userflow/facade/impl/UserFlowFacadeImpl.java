package cn.openaipay.application.userflow.facade.impl;

import cn.openaipay.application.userflow.command.RegisterUserCommand;
import cn.openaipay.application.userflow.dto.RegisterPhoneCheckDTO;
import cn.openaipay.application.userflow.dto.UserRegistrationDTO;
import cn.openaipay.application.userflow.facade.UserFlowFacade;
import cn.openaipay.application.userflow.service.UserFlowService;
import org.springframework.stereotype.Service;

/**
 * 用户流程门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Service
public class UserFlowFacadeImpl implements UserFlowFacade {

    /** 用户流程应用服务。 */
    private final UserFlowService userFlowService;

    public UserFlowFacadeImpl(UserFlowService userFlowService) {
        this.userFlowService = userFlowService;
    }

    /**
     * 校验注册手机号状态。
     */
    @Override
    public RegisterPhoneCheckDTO checkRegisterPhone(String loginId, String deviceId) {
        return userFlowService.checkRegisterPhone(loginId, deviceId);
    }

    /**
     * 执行注册流程。
     */
    @Override
    public UserRegistrationDTO register(RegisterUserCommand command) {
        return userFlowService.register(command);
    }
}
