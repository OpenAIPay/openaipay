package cn.openaipay.application.kyc.service;

import cn.openaipay.application.kyc.command.SubmitKycCommand;
import cn.openaipay.application.kyc.dto.KycStatusDTO;

/**
 * 实名应用服务接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface KycService {

    /**
     * 查询实名状态。
     */
    KycStatusDTO getStatus(Long userId);

    /**
     * 提交实名认证。
     */
    KycStatusDTO submit(SubmitKycCommand command);
}
