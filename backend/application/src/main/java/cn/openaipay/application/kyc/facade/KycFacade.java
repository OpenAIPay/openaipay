package cn.openaipay.application.kyc.facade;

import cn.openaipay.application.kyc.command.SubmitKycCommand;
import cn.openaipay.application.kyc.dto.KycStatusDTO;

/**
 * 实名门面接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface KycFacade {

    /**
     * 查询实名状态。
     */
    KycStatusDTO getStatus(Long userId);

    /**
     * 提交实名认证。
     */
    KycStatusDTO submit(SubmitKycCommand command);
}
