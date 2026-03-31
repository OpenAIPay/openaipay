package cn.openaipay.application.agreement.service;

import cn.openaipay.application.agreement.command.OpenAiCreditWithAgreementCommand;
import cn.openaipay.application.agreement.command.OpenAiLoanWithAgreementCommand;
import cn.openaipay.application.agreement.dto.CreditProductOpenAgreementPackDTO;
import cn.openaipay.application.agreement.command.OpenFundAccountWithAgreementCommand;
import cn.openaipay.application.agreement.dto.FundAccountOpenAgreementPackDTO;
import cn.openaipay.application.agreement.dto.OpenCreditProductWithAgreementResultDTO;
import cn.openaipay.application.agreement.dto.OpenFundAccountWithAgreementResultDTO;

/**
 * 协议应用服务接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface AgreementService {

    /**
     * 查询基金账户开通协议包。
     */
    FundAccountOpenAgreementPackDTO getFundAccountOpenAgreementPack(Long userId, String fundCode, String currencyCode);

    /**
     * 查询爱花开通协议包。
     */
    CreditProductOpenAgreementPackDTO getAiCreditOpenAgreementPack(Long userId);

    /**
     * 查询爱借开通协议包。
     */
    CreditProductOpenAgreementPackDTO getAiLoanOpenAgreementPack(Long userId);

    /**
     * 爱花是否已完成协议开通。
     */
    boolean isAiCreditOpened(Long userId);

    /**
     * 爱借是否已完成协议开通。
     */
    boolean isAiLoanOpened(Long userId);

    /**
     * 签约并开通基金账户。
     */
    OpenFundAccountWithAgreementResultDTO openFundAccountWithAgreement(OpenFundAccountWithAgreementCommand command);

    /**
     * 签约并开通爱花。
     */
    OpenCreditProductWithAgreementResultDTO openAiCreditWithAgreement(OpenAiCreditWithAgreementCommand command);

    /**
     * 签约并开通爱借。
     */
    OpenCreditProductWithAgreementResultDTO openAiLoanWithAgreement(OpenAiLoanWithAgreementCommand command);
}
