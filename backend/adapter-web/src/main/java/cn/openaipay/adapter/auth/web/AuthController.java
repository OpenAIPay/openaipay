package cn.openaipay.adapter.auth.web;

import cn.openaipay.adapter.auth.web.request.MobileVerifyLoginRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.auth.dto.LoginPresetAccountDTO;
import cn.openaipay.application.auth.command.LoginCommand;
import cn.openaipay.application.auth.dto.LoginResultDTO;
import cn.openaipay.application.auth.facade.AuthFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** 旧设备标识请求头。 */
    private static final String LEGACY_DEVICE_IDS_HEADER_NAME = "X-Legacy-Device-Ids";

    /** AuthFacade组件 */
    private final AuthFacade authFacade;

    public AuthController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    /**
     * 本机号码验证登录（免密）。
     */
    @PostMapping("/mobile-verify-login")
    public ApiResponse<LoginResultDTO> mobileVerifyLogin(@Valid @RequestBody MobileVerifyLoginRequest request,
                                                         HttpServletRequest httpRequest) {
        LoginResultDTO loginResult = authFacade.mobileVerifyLogin(
                new LoginCommand(
                        request.loginId(),
                        request.deviceId(),
                        resolveLegacyDeviceIds(httpRequest)
                )
        );
        return ApiResponse.success(loginResult);
    }

    /**
     * 查询登录下拉账号。
     */
    @GetMapping("/preset-login-accounts")
    public ApiResponse<List<LoginPresetAccountDTO>> listPresetLoginAccounts(@RequestParam("deviceId") String deviceId,
                                                                            HttpServletRequest httpRequest) {
        return ApiResponse.success(authFacade.listPresetLoginAccounts(deviceId, resolveLegacyDeviceIds(httpRequest)));
    }

    private List<String> resolveLegacyDeviceIds(HttpServletRequest request) {
        if (request == null) {
            return List.of();
        }
        String rawHeader = normalizeOptional(request.getHeader(LEGACY_DEVICE_IDS_HEADER_NAME));
        if (rawHeader == null) {
            return List.of();
        }
        return Arrays.stream(rawHeader.split(","))
                .map(this::normalizeOptional)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
