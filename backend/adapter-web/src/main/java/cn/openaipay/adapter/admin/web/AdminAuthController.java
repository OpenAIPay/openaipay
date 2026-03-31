package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.web.request.AdminLoginRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.admin.command.AdminLoginCommand;
import cn.openaipay.application.admin.facade.AdminFacade;
import cn.openaipay.application.admin.dto.AdminLoginResultDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台管理认证控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    /** AdminFacade组件 */
    private final AdminFacade adminFacade;

    public AdminAuthController(AdminFacade adminFacade) {
        this.adminFacade = adminFacade;
    }

    /**
     * 处理登录信息。
     */
    @PostMapping("/login")
    public ApiResponse<AdminLoginResultDTO> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResultDTO result = adminFacade.login(
                new AdminLoginCommand(request.username(), request.password(), request.deviceId())
        );
        return ApiResponse.success(result);
    }
}
