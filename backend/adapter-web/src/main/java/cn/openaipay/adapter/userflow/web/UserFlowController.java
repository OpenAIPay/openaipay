package cn.openaipay.adapter.userflow.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.userflow.web.request.RegisterUserRequest;
import cn.openaipay.application.userflow.command.RegisterUserCommand;
import cn.openaipay.application.userflow.dto.RegisterPhoneCheckDTO;
import cn.openaipay.application.userflow.dto.UserRegistrationDTO;
import cn.openaipay.application.userflow.facade.UserFlowFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户流程控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@RestController
@RequestMapping("/api/user-flows")
public class UserFlowController {

    /** 用户流程门面。 */
    private final UserFlowFacade userFlowFacade;

    public UserFlowController(UserFlowFacade userFlowFacade) {
        this.userFlowFacade = userFlowFacade;
    }

    /**
     * 校验注册手机号状态。
     */
    @GetMapping("/register/check")
    public ApiResponse<RegisterPhoneCheckDTO> checkRegisterPhone(@RequestParam("loginId") String loginId,
                                                                 @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        return ApiResponse.success(userFlowFacade.checkRegisterPhone(loginId, deviceId));
    }

    /**
     * 执行注册流程。
     */
    @PostMapping("/registrations")
    public ApiResponse<UserRegistrationDTO> register(@RequestBody RegisterUserRequest request) {
        return ApiResponse.success(userFlowFacade.register(new RegisterUserCommand(
                request.deviceId(),
                request.legacyDeviceIds(),
                request.loginId(),
                request.userTypeCode(),
                request.accountSource(),
                request.nickname(),
                request.avatarUrl(),
                request.countryCode(),
                request.mobile(),
                request.realName(),
                request.idCardNo(),
                request.loginPassword()
        )));
    }
}
