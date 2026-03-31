package cn.openaipay.adapter.agreement.web;

import cn.openaipay.adapter.agreement.web.request.AgreementAcceptRequest;
import cn.openaipay.adapter.agreement.web.request.OpenCreditProductWithAgreementRequest;
import cn.openaipay.adapter.agreement.web.request.OpenFundAccountWithAgreementRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.agreement.command.AgreementAcceptCommand;
import cn.openaipay.application.agreement.command.OpenFundAccountWithAgreementCommand;
import cn.openaipay.application.agreement.command.OpenAiCreditWithAgreementCommand;
import cn.openaipay.application.agreement.command.OpenAiLoanWithAgreementCommand;
import cn.openaipay.application.agreement.dto.CreditProductOpenAgreementPackDTO;
import cn.openaipay.application.agreement.dto.FundAccountOpenAgreementPackDTO;
import cn.openaipay.application.agreement.dto.OpenCreditProductWithAgreementResultDTO;
import cn.openaipay.application.agreement.dto.OpenFundAccountWithAgreementResultDTO;
import cn.openaipay.application.agreement.facade.AgreementFacade;
import cn.openaipay.domain.creditaccount.model.CreditProductCodes;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

/**
 * 协议控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@RestController
@RequestMapping("/api/agreements")
public class AgreementController {

    /** 协议信息 */
    private final AgreementFacade agreementFacade;

    public AgreementController(AgreementFacade agreementFacade) {
        this.agreementFacade = agreementFacade;
    }

    /**
     * 查询爱存开通协议包。
     */
    @GetMapping("/packs/fund-account-open")
    public ApiResponse<FundAccountOpenAgreementPackDTO> getFundAccountOpenAgreementPack(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "fundCode", required = false) String fundCode,
            @RequestParam(value = "currencyCode", required = false) String currencyCode) {
        return ApiResponse.success(agreementFacade.getFundAccountOpenAgreementPack(userId, fundCode, currencyCode));
    }

    /**
     * 查询信用产品开通协议包。
     */
    @GetMapping("/packs/credit-product-open")
    public ApiResponse<CreditProductOpenAgreementPackDTO> getCreditProductOpenAgreementPack(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "productCode", required = false) String productCode) {
        return ApiResponse.success(resolveCreditOpenAgreementPack(userId, productCode));
    }

    /**
     * 签约并开通爱存。
     */
    @PostMapping("/sign/fund-account-open")
    public ApiResponse<OpenFundAccountWithAgreementResultDTO> openFundAccountWithAgreement(
            @Valid @RequestBody OpenFundAccountWithAgreementRequest request) {
        List<AgreementAcceptCommand> accepts = request.agreementAccepts().stream()
                .map(this::toAcceptCommand)
                .toList();
        OpenFundAccountWithAgreementResultDTO result = agreementFacade.openFundAccountWithAgreement(
                new OpenFundAccountWithAgreementCommand(
                        request.userId(),
                        request.fundCode(),
                        request.currencyCode(),
                        request.idempotencyKey(),
                        accepts
                )
        );
        return ApiResponse.success(result);
    }

    /**
     * 签约并开通信用产品。
     */
    @PostMapping("/sign/credit-product-open")
    public ApiResponse<OpenCreditProductWithAgreementResultDTO> openCreditProductWithAgreement(
            @Valid @RequestBody OpenCreditProductWithAgreementRequest request) {
        return ApiResponse.success(openCreditProductWithAgreement(request, request.productCode()));
    }

    private CreditProductOpenAgreementPackDTO resolveCreditOpenAgreementPack(Long userId, String productCode) {
        return CreditProductCodes.AILOAN.equals(normalizeCreditProductCode(productCode))
                ? agreementFacade.getAiLoanOpenAgreementPack(userId)
                : agreementFacade.getAiCreditOpenAgreementPack(userId);
    }

    private OpenCreditProductWithAgreementResultDTO openCreditProductWithAgreement(
            OpenCreditProductWithAgreementRequest request,
            String productCode) {
        List<AgreementAcceptCommand> accepts = request.agreementAccepts().stream()
                .map(this::toAcceptCommand)
                .toList();
        String normalizedProductCode = normalizeCreditProductCode(productCode);
        if (CreditProductCodes.AILOAN.equals(normalizedProductCode)) {
            return agreementFacade.openAiLoanWithAgreement(
                    new OpenAiLoanWithAgreementCommand(
                            request.userId(),
                            request.idempotencyKey(),
                            accepts
                    )
            );
        }
        return agreementFacade.openAiCreditWithAgreement(
                new OpenAiCreditWithAgreementCommand(
                        request.userId(),
                        request.idempotencyKey(),
                        accepts
                )
        );
    }

    private String normalizeCreditProductCode(String productCode) {
        String normalized = productCode == null ? "" : productCode.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()
                || CreditProductCodes.AICREDIT.equals(normalized)) {
            return CreditProductCodes.AICREDIT;
        }
        if (CreditProductCodes.AILOAN.equals(normalized)) {
            return CreditProductCodes.AILOAN;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productCode 不支持: " + productCode);
    }

    private AgreementAcceptCommand toAcceptCommand(AgreementAcceptRequest request) {
        return new AgreementAcceptCommand(request.templateCode(), request.templateVersion());
    }
}
