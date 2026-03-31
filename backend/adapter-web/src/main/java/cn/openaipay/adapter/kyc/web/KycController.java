package cn.openaipay.adapter.kyc.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.kyc.web.request.SubmitKycRequest;
import cn.openaipay.application.kyc.command.SubmitKycCommand;
import cn.openaipay.application.kyc.dto.KycStatusDTO;
import cn.openaipay.application.kyc.facade.KycFacade;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 实名控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@RestController
@RequestMapping("/api/kyc/users")
public class KycController {

    /** 实名门面。 */
    private final KycFacade kycFacade;

    public KycController(KycFacade kycFacade) {
        this.kycFacade = kycFacade;
    }

    /**
     * 查询实名状态。
     */
    @GetMapping("/{userId}/status")
    public ApiResponse<KycStatusDTO> getStatus(@PathVariable("userId") Long userId) {
        return ApiResponse.success(kycFacade.getStatus(userId));
    }

    /**
     * 提交实名认证。
     */
    @PostMapping("/{userId}/submissions")
    public ApiResponse<KycStatusDTO> submit(@PathVariable("userId") Long userId,
                                            @Valid @RequestBody SubmitKycRequest request) {
        return ApiResponse.success(kycFacade.submit(new SubmitKycCommand(
                userId,
                request.realName(),
                request.idCardNo()
        )));
    }
}
