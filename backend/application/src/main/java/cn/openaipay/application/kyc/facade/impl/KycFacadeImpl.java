package cn.openaipay.application.kyc.facade.impl;

import cn.openaipay.application.kyc.command.SubmitKycCommand;
import cn.openaipay.application.kyc.dto.KycStatusDTO;
import cn.openaipay.application.kyc.facade.KycFacade;
import cn.openaipay.application.kyc.service.KycService;
import org.springframework.stereotype.Service;

/**
 * 实名门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Service
public class KycFacadeImpl implements KycFacade {

    /** 实名应用服务。 */
    private final KycService kycService;

    public KycFacadeImpl(KycService kycService) {
        this.kycService = kycService;
    }

    /**
     * 查询实名状态。
     */
    @Override
    public KycStatusDTO getStatus(Long userId) {
        return kycService.getStatus(userId);
    }

    /**
     * 提交实名认证。
     */
    @Override
    public KycStatusDTO submit(SubmitKycCommand command) {
        return kycService.submit(command);
    }
}
