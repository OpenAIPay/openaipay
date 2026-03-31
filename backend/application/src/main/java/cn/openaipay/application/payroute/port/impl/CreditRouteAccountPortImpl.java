package cn.openaipay.application.payroute.port.impl;

import cn.openaipay.application.agreement.command.AgreementAcceptCommand;
import cn.openaipay.application.agreement.command.OpenAiCreditWithAgreementCommand;
import cn.openaipay.application.agreement.dto.AgreementTemplateDTO;
import cn.openaipay.application.agreement.dto.CreditProductOpenAgreementPackDTO;
import cn.openaipay.application.agreement.facade.AgreementFacade;
import cn.openaipay.application.auth.exception.ForbiddenException;
import cn.openaipay.application.creditaccount.command.CreditTccCancelCommand;
import cn.openaipay.application.creditaccount.command.CreditTccConfirmCommand;
import cn.openaipay.application.creditaccount.command.CreditTccTryCommand;
import cn.openaipay.application.creditaccount.service.CreditAccountService;
import cn.openaipay.application.payroute.port.CreditRouteAccountPort;
import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import java.util.List;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 信用账户路由能力端口实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
@Component
public class CreditRouteAccountPortImpl implements CreditRouteAccountPort {

    /** 日志组件。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(CreditRouteAccountPortImpl.class);
    /** 爱花自动开通幂等键前缀。 */
    private static final String AUTO_OPEN_AICREDIT_IDEMPOTENCY_PREFIX = "AUTO_PAY_AICREDIT";

    /** 协议门面。 */
    private final AgreementFacade agreementFacade;
    /** 信用账户应用服务。 */
    private final CreditAccountService creditAccountService;

    public CreditRouteAccountPortImpl(AgreementFacade agreementFacade,
                                      CreditAccountService creditAccountService) {
        this.agreementFacade = agreementFacade;
        this.creditAccountService = creditAccountService;
    }

    /**
     * 按用户ID和账户类型解析账户单号。
     */
    @Override
    public String getAccountNoByUserId(Long userId, CreditAccountType accountType) {
        validateUserId(userId);
        if (accountType == null) {
            throw new IllegalArgumentException("accountType must not be null");
        }
        ensureProductOpened(userId, accountType);
        return creditAccountService.getCreditAccountByUserId(userId, accountType).accountNo();
    }

    /**
     * 处理TCCTRY信息。
     */
    @Override
    public void tccTry(String xid,
                       String branchId,
                       String accountNo,
                       String operationType,
                       String assetCategory,
                       Money amount,
                       String businessNo) {
        creditAccountService.tccTry(new CreditTccTryCommand(
                xid,
                branchId,
                accountNo,
                operationType,
                assetCategory,
                amount,
                businessNo
        ));
    }

    /**
     * 处理TCC信息。
     */
    @Override
    public void tccConfirm(String xid, String branchId) {
        creditAccountService.tccConfirm(new CreditTccConfirmCommand(xid, branchId));
    }

    /**
     * 处理TCC信息。
     */
    @Override
    public void tccCancel(String xid,
                          String branchId,
                          String accountNo,
                          String operationType,
                          String assetCategory,
                          Money amount,
                          String businessNo) {
        creditAccountService.tccCancel(new CreditTccCancelCommand(
                xid,
                branchId,
                accountNo,
                operationType,
                assetCategory,
                amount,
                businessNo
        ));
    }

    private void ensureProductOpened(Long userId, CreditAccountType accountType) {
        if (accountType == CreditAccountType.LOAN_ACCOUNT) {
            if (!agreementFacade.isAiLoanOpened(userId)) {
                throw new ForbiddenException("爱借服务未开通，请先完成开通协议");
            }
            return;
        }
        if (agreementFacade.isAiCreditOpened(userId)) {
            return;
        }
        autoOpenAiCreditSilently(userId);
        if (!agreementFacade.isAiCreditOpened(userId)) {
            throw new ForbiddenException("爱花服务未开通，请先完成开通协议");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
    }

    private void autoOpenAiCreditSilently(Long userId) {
        try {
            CreditProductOpenAgreementPackDTO agreementPack = agreementFacade.getAiCreditOpenAgreementPack(userId);
            List<AgreementAcceptCommand> agreementAccepts = toAgreementAcceptCommands(agreementPack);
            if (agreementAccepts.isEmpty()) {
                return;
            }
            agreementFacade.openAiCreditWithAgreement(new OpenAiCreditWithAgreementCommand(
                    userId,
                    buildAutoOpenIdempotencyKey(userId),
                    agreementAccepts
            ));
        } catch (RuntimeException exception) {
            LOGGER.warn("auto open aicredit in payroute failed, userId={}, reason={}", userId, exception.getMessage());
        }
    }

    private List<AgreementAcceptCommand> toAgreementAcceptCommands(CreditProductOpenAgreementPackDTO agreementPack) {
        if (agreementPack == null || agreementPack.agreements() == null || agreementPack.agreements().isEmpty()) {
            return List.of();
        }
        return agreementPack.agreements().stream()
                .map(this::toAgreementAcceptCommandOrNull)
                .filter(command -> command != null)
                .toList();
    }

    private AgreementAcceptCommand toAgreementAcceptCommandOrNull(AgreementTemplateDTO template) {
        if (template == null) {
            return null;
        }
        String templateCode = normalizeOptional(template.templateCode());
        String templateVersion = normalizeOptional(template.templateVersion());
        if (templateCode == null || templateVersion == null) {
            return null;
        }
        return new AgreementAcceptCommand(templateCode, templateVersion);
    }

    private String buildAutoOpenIdempotencyKey(Long userId) {
        String key = AUTO_OPEN_AICREDIT_IDEMPOTENCY_PREFIX + "_" + userId;
        return key.length() <= 64 ? key : key.substring(0, 64);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
