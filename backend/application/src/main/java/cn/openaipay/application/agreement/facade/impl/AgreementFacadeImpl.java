package cn.openaipay.application.agreement.facade.impl;

import cn.openaipay.application.agreement.command.OpenAiCreditWithAgreementCommand;
import cn.openaipay.application.agreement.command.OpenAiLoanWithAgreementCommand;
import cn.openaipay.application.agreement.dto.CreditProductOpenAgreementPackDTO;
import cn.openaipay.application.agreement.command.OpenFundAccountWithAgreementCommand;
import cn.openaipay.application.agreement.dto.FundAccountOpenAgreementPackDTO;
import cn.openaipay.application.agreement.dto.OpenCreditProductWithAgreementResultDTO;
import cn.openaipay.application.agreement.dto.OpenFundAccountWithAgreementResultDTO;
import cn.openaipay.application.agreement.facade.AgreementFacade;
import cn.openaipay.application.agreement.service.AgreementService;
import org.springframework.stereotype.Service;

/**
 * 协议门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Service
public class AgreementFacadeImpl implements AgreementFacade {

    /** 协议信息 */
    private final AgreementService agreementService;

    public AgreementFacadeImpl(AgreementService agreementService) {
        this.agreementService = agreementService;
    }

    /**
     * 获取基金协议信息。
     */
    @Override
    public FundAccountOpenAgreementPackDTO getFundAccountOpenAgreementPack(Long userId, String fundCode, String currencyCode) {
        return agreementService.getFundAccountOpenAgreementPack(userId, fundCode, currencyCode);
    }

    /**
     * 获取协议信息。
     */
    @Override
    public CreditProductOpenAgreementPackDTO getAiCreditOpenAgreementPack(Long userId) {
        return agreementService.getAiCreditOpenAgreementPack(userId);
    }

    /**
     * 获取协议信息。
     */
    @Override
    public CreditProductOpenAgreementPackDTO getAiLoanOpenAgreementPack(Long userId) {
        return agreementService.getAiLoanOpenAgreementPack(userId);
    }

    /**
     * 判断是否业务数据。
     */
    @Override
    public boolean isAiCreditOpened(Long userId) {
        return agreementService.isAiCreditOpened(userId);
    }

    /**
     * 判断是否业务数据。
     */
    @Override
    public boolean isAiLoanOpened(Long userId) {
        return agreementService.isAiLoanOpened(userId);
    }

    /**
     * 开通基金协议信息。
     */
    @Override
    public OpenFundAccountWithAgreementResultDTO openFundAccountWithAgreement(OpenFundAccountWithAgreementCommand command) {
        return agreementService.openFundAccountWithAgreement(command);
    }

    /**
     * 开通协议信息。
     */
    @Override
    public OpenCreditProductWithAgreementResultDTO openAiCreditWithAgreement(OpenAiCreditWithAgreementCommand command) {
        return agreementService.openAiCreditWithAgreement(command);
    }

    /**
     * 开通协议信息。
     */
    @Override
    public OpenCreditProductWithAgreementResultDTO openAiLoanWithAgreement(OpenAiLoanWithAgreementCommand command) {
        return agreementService.openAiLoanWithAgreement(command);
    }
}
