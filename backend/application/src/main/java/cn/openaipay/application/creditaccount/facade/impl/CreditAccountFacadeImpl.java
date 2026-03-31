package cn.openaipay.application.creditaccount.facade.impl;

import cn.openaipay.application.agreement.command.AgreementAcceptCommand;
import cn.openaipay.application.agreement.command.OpenAiCreditWithAgreementCommand;
import cn.openaipay.application.agreement.dto.AgreementTemplateDTO;
import cn.openaipay.application.agreement.dto.CreditProductOpenAgreementPackDTO;
import cn.openaipay.application.agreement.facade.AgreementFacade;
import cn.openaipay.application.auth.exception.ForbiddenException;
import cn.openaipay.application.creditaccount.command.CreateCreditAccountCommand;
import cn.openaipay.application.creditaccount.command.CreditTccCancelCommand;
import cn.openaipay.application.creditaccount.command.CreditTccConfirmCommand;
import cn.openaipay.application.creditaccount.command.CreditTccTryCommand;
import cn.openaipay.application.creditaccount.dto.CreditAccountDTO;
import cn.openaipay.application.creditaccount.dto.CreditCurrentBillDetailDTO;
import cn.openaipay.application.creditaccount.dto.CreditTccBranchDTO;
import cn.openaipay.application.creditaccount.facade.CreditAccountFacade;
import cn.openaipay.application.creditaccount.service.CreditAccountService;
import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import cn.openaipay.domain.creditaccount.repository.CreditAccountRepository;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

/**
 * 信用账户门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class CreditAccountFacadeImpl implements CreditAccountFacade {

    /** 日志组件。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(CreditAccountFacadeImpl.class);
    /** 爱花自动开通幂等键前缀。 */
    private static final String AUTO_OPEN_AICREDIT_IDEMPOTENCY_PREFIX = "AUTO_OPEN_AICREDIT";

    /** CreditAccountService组件 */
    private final CreditAccountService creditAccountService;
    /** CreditAccountRepository组件 */
    private final CreditAccountRepository creditAccountRepository;
    /** 协议门面。 */
    private final AgreementFacade agreementFacade;

    public CreditAccountFacadeImpl(CreditAccountService creditAccountService,
                                   CreditAccountRepository creditAccountRepository,
                                   AgreementFacade agreementFacade) {
        this.creditAccountService = creditAccountService;
        this.creditAccountRepository = creditAccountRepository;
        this.agreementFacade = agreementFacade;
    }

    /**
     * 创建信用账户信息。
     */
    @Override
    public String createCreditAccount(CreateCreditAccountCommand command) {
        return creditAccountService.createCreditAccount(command);
    }

    /**
     * 获取信用账户信息。
     */
    @Override
    public CreditAccountDTO getCreditAccount(String accountNo) {
        return creditAccountService.getCreditAccount(accountNo);
    }

    /**
     * 按用户ID获取信用信息。
     */
    @Override
    public CreditAccountDTO getCreditAccountByUserId(Long userId) {
        ensureAiCreditOpened(userId);
        return creditAccountService.getCreditAccountByUserId(userId);
    }

    /**
     * 按用户ID获取当前明细信息。
     */
    @Override
    public CreditCurrentBillDetailDTO getCurrentBillDetailByUserId(Long userId) {
        ensureAiCreditOpened(userId);
        return creditAccountService.getCurrentBillDetailByUserId(userId);
    }

    /**
     * 按用户ID获取明细信息。
     */
    @Override
    public CreditCurrentBillDetailDTO getNextBillDetailByUserId(Long userId) {
        ensureAiCreditOpened(userId);
        return creditAccountService.getNextBillDetailByUserId(userId);
    }

    /**
     * 按用户ID获取单号。
     */
    @Override
    public String getAccountNoByUserId(Long userId) {
        return getAccountNoByUserId(userId, CreditAccountType.AICREDIT);
    }

    /**
     * 按用户ID获取单号。
     */
    @Override
    public String getAccountNoByUserId(Long userId, CreditAccountType accountType) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        if (accountType == null) {
            throw new IllegalArgumentException("accountType must not be null");
        }
        ensureCreditProductOpened(userId, accountType);
        return creditAccountRepository.findByUserIdAndType(userId, accountType)
                .orElseThrow(() -> new NoSuchElementException(
                        "credit account not found for userId=" + userId + ", type=" + accountType.name()
                ))
                .getAccountNo();
    }

    /**
     * 处理TCCTRY信息。
     */
    @Override
    public CreditTccBranchDTO tccTry(String xid,
                                     String branchId,
                                     String accountNo,
                                     String operationType,
                                     String assetCategory,
                                     Money amount,
                                     String businessNo) {
        return creditAccountService.tccTry(new CreditTccTryCommand(
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
    public CreditTccBranchDTO tccConfirm(String xid, String branchId) {
        return creditAccountService.tccConfirm(new CreditTccConfirmCommand(xid, branchId));
    }

    /**
     * 处理TCC信息。
     */
    @Override
    public CreditTccBranchDTO tccCancel(String xid,
                                        String branchId,
                                        String accountNo,
                                        String operationType,
                                        String assetCategory,
                                        Money amount,
                                        String businessNo) {
        return creditAccountService.tccCancel(new CreditTccCancelCommand(
                xid,
                branchId,
                accountNo,
                operationType,
                assetCategory,
                amount,
                businessNo
        ));
    }

    private void ensureCreditProductOpened(Long userId, CreditAccountType accountType) {
        if (accountType == CreditAccountType.LOAN_ACCOUNT) {
            ensureAiLoanOpened(userId);
            return;
        }
        ensureAiCreditOpened(userId);
    }

    private void ensureAiCreditOpened(Long userId) {
        if (agreementFacade.isAiCreditOpened(userId)) {
            return;
        }
        autoOpenAiCreditSilently(userId);
        if (!agreementFacade.isAiCreditOpened(userId)) {
            throw new ForbiddenException("爱花服务未开通，请先完成开通协议");
        }
    }

    private void ensureAiLoanOpened(Long userId) {
        if (!agreementFacade.isAiLoanOpened(userId)) {
            throw new ForbiddenException("爱借服务未开通，请先完成开通协议");
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
            LOGGER.warn("auto open aicredit failed, userId={}, reason={}", userId, exception.getMessage());
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
