package cn.openaipay.application.agreement.service.impl;

import cn.openaipay.application.agreement.command.AgreementAcceptCommand;
import cn.openaipay.application.agreement.command.OpenFundAccountWithAgreementCommand;
import cn.openaipay.application.agreement.command.OpenAiCreditWithAgreementCommand;
import cn.openaipay.application.agreement.command.OpenAiLoanWithAgreementCommand;
import cn.openaipay.application.agreement.dto.AgreementTemplateDTO;
import cn.openaipay.application.agreement.dto.CreditProductOpenAgreementPackDTO;
import cn.openaipay.application.agreement.dto.FundAccountOpenAgreementPackDTO;
import cn.openaipay.application.agreement.dto.OpenCreditProductWithAgreementResultDTO;
import cn.openaipay.application.agreement.dto.OpenFundAccountWithAgreementResultDTO;
import cn.openaipay.application.agreement.service.AgreementService;
import cn.openaipay.application.creditaccount.dto.CreditAccountDTO;
import cn.openaipay.application.creditaccount.service.CreditAccountService;
import cn.openaipay.application.fundaccount.command.CreateFundAccountCommand;
import cn.openaipay.application.fundaccount.dto.FundAccountDTO;
import cn.openaipay.application.fundaccount.service.FundAccountService;
import cn.openaipay.application.loanaccount.dto.LoanAccountDTO;
import cn.openaipay.application.loanaccount.service.LoanAccountService;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import cn.openaipay.domain.agreement.model.AgreementBizType;
import cn.openaipay.domain.agreement.model.AgreementSignItem;
import cn.openaipay.domain.agreement.model.AgreementSignRecord;
import cn.openaipay.domain.agreement.model.AgreementSignStatus;
import cn.openaipay.domain.agreement.model.AgreementTemplate;
import cn.openaipay.domain.agreement.repository.AgreementRepository;
import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import cn.openaipay.domain.creditaccount.model.CreditProductCodes;
import cn.openaipay.domain.fundaccount.model.FundProductCodes;
import cn.openaipay.domain.user.model.UserFeatureCode;
import cn.openaipay.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * 协议应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Service
public class AgreementServiceImpl implements AgreementService {

    /** 资金编码 */
    private static final String DEFAULT_FUND_CODE = FundProductCodes.DEFAULT_FUND_CODE;
    /** 业务编码 */
    private static final String DEFAULT_CURRENCY_CODE = "CNY";
    /** 产品编码 */
    private static final String PRODUCT_CODE_AICREDIT = CreditProductCodes.AICREDIT;
    /** 产品编码 */
    private static final String PRODUCT_CODE_AILOAN = CreditProductCodes.AILOAN;
    /** 资金签约业务类型 */
    private static final String FUND_ACCOUNT_OPEN_SIGN_BIZ_TYPE = "91";
    /** 签约业务类型 */
    private static final String AICREDIT_OPEN_SIGN_BIZ_TYPE = "92";
    /** 签约业务类型 */
    private static final String AILOAN_OPEN_SIGN_BIZ_TYPE = "93";

    /** 协议信息 */
    private final AgreementRepository agreementRepository;
    /** 资金信息 */
    private final FundAccountService fundAccountService;
    /** 信用信息 */
    private final CreditAccountService creditAccountService;
    /** 借款信息 */
    private final LoanAccountService loanAccountService;
    /** 用户信息 */
    private final UserRepository userRepository;
    /** AI支付ID */
    private final AiPayIdGenerator aiPayIdGenerator;

    public AgreementServiceImpl(AgreementRepository agreementRepository,
                                           FundAccountService fundAccountService,
                                           CreditAccountService creditAccountService,
                                           LoanAccountService loanAccountService,
                                           UserRepository userRepository,
                                           AiPayIdGenerator aiPayIdGenerator) {
        this.agreementRepository = agreementRepository;
        this.fundAccountService = fundAccountService;
        this.creditAccountService = creditAccountService;
        this.loanAccountService = loanAccountService;
        this.userRepository = userRepository;
        this.aiPayIdGenerator = aiPayIdGenerator;
    }

    /**
     * 获取基金协议信息。
     */
    @Override
    @Transactional(readOnly = true)
    public FundAccountOpenAgreementPackDTO getFundAccountOpenAgreementPack(Long userId, String fundCode, String currencyCode) {
        Long normalizedUserId = requirePositiveUserId(userId);
        String normalizedFundCode = normalizeFundCode(fundCode);
        String normalizedCurrencyCode = normalizeCurrencyCode(currencyCode);
        List<AgreementTemplate> templates = loadActiveTemplates(AgreementBizType.FUND_ACCOUNT_OPEN);
        return new FundAccountOpenAgreementPackDTO(
                normalizedUserId,
                AgreementBizType.FUND_ACCOUNT_OPEN.name(),
                normalizedFundCode,
                normalizedCurrencyCode,
                templates.stream().map(this::toTemplateDTO).toList()
        );
    }

    /**
     * 获取协议信息。
     */
    @Override
    @Transactional(readOnly = true)
    public CreditProductOpenAgreementPackDTO getAiCreditOpenAgreementPack(Long userId) {
        Long normalizedUserId = requirePositiveUserId(userId);
        List<AgreementTemplate> templates = loadActiveTemplates(AgreementBizType.AICREDIT_OPEN);
        return new CreditProductOpenAgreementPackDTO(
                normalizedUserId,
                AgreementBizType.AICREDIT_OPEN.name(),
                PRODUCT_CODE_AICREDIT,
                templates.stream().map(this::toTemplateDTO).toList()
        );
    }

    /**
     * 获取协议信息。
     */
    @Override
    @Transactional(readOnly = true)
    public CreditProductOpenAgreementPackDTO getAiLoanOpenAgreementPack(Long userId) {
        Long normalizedUserId = requirePositiveUserId(userId);
        List<AgreementTemplate> templates = loadActiveTemplates(AgreementBizType.AILOAN_OPEN);
        return new CreditProductOpenAgreementPackDTO(
                normalizedUserId,
                AgreementBizType.AILOAN_OPEN.name(),
                PRODUCT_CODE_AILOAN,
                templates.stream().map(this::toTemplateDTO).toList()
        );
    }

    /**
     * 判断是否业务数据。
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isAiCreditOpened(Long userId) {
        return userRepository.isFeatureEnabled(requirePositiveUserId(userId), UserFeatureCode.AICREDIT_OPENED);
    }

    /**
     * 判断是否业务数据。
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isAiLoanOpened(Long userId) {
        return userRepository.isFeatureEnabled(requirePositiveUserId(userId), UserFeatureCode.AILOAN_OPENED);
    }

    /**
     * 开通基金协议信息。
     */
    @Override
    @Transactional
    public OpenFundAccountWithAgreementResultDTO openFundAccountWithAgreement(OpenFundAccountWithAgreementCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        Long userId = requirePositiveUserId(command.userId());
        String fundCode = normalizeFundCode(command.fundCode());
        String currencyCode = normalizeCurrencyCode(command.currencyCode());
        String idempotencyKey = normalizeIdempotencyKey(command.idempotencyKey());
        AgreementBizType bizType = AgreementBizType.FUND_ACCOUNT_OPEN;
        List<AgreementTemplate> activeTemplates = loadActiveTemplates(bizType);
        Map<String, AgreementTemplate> activeTemplateMap = buildTemplateKeyMap(activeTemplates);
        List<AgreementTemplate> acceptedTemplates = resolveAcceptedTemplates(command.agreementAccepts(), activeTemplateMap, activeTemplates);
        Optional<AgreementSignRecord> existingOpt = agreementRepository.findSignRecordByIdempotencyKey(
                userId,
                bizType,
                idempotencyKey
        );

        FundAccountDTO existingFundAccount = findExistingFundAccount(userId, fundCode);
        if (existingFundAccount != null) {
            AgreementSignRecord existingRecord = ensureSucceededAgreementRecord(
                    userId,
                    fundCode,
                    currencyCode,
                    idempotencyKey,
                    acceptedTemplates,
                    existingOpt,
                    bizType,
                    FUND_ACCOUNT_OPEN_SIGN_BIZ_TYPE
            );
            userRepository.markFeatureEnabled(
                    userId,
                    UserFeatureCode.FUND_ACCOUNT_OPENED,
                    existingRecord.getOpenedAt() == null ? LocalDateTime.now() : existingRecord.getOpenedAt()
            );
            return toFundOpenResult(existingRecord, true);
        }
        if (existingOpt.isPresent() && existingOpt.get().getSignStatus() == AgreementSignStatus.SUCCEEDED) {
            AgreementSignRecord existingRecord = existingOpt.get();
            openFundAccountIdempotent(userId, fundCode, currencyCode);
            userRepository.markFeatureEnabled(
                    userId,
                    UserFeatureCode.FUND_ACCOUNT_OPENED,
                    existingRecord.getOpenedAt() == null ? LocalDateTime.now() : existingRecord.getOpenedAt()
            );
            return toFundOpenResult(existingRecord, true);
        }

        LocalDateTime now = LocalDateTime.now();
        AgreementSignRecord signRecord = existingOpt.orElseGet(() -> AgreementSignRecord.pending(
                aiPayIdGenerator.generate(AiPayIdGenerator.DOMAIN_FUND_ACCOUNT, FUND_ACCOUNT_OPEN_SIGN_BIZ_TYPE, String.valueOf(userId)),
                userId,
                bizType,
                fundCode,
                currencyCode,
                idempotencyKey,
                now
        ));
        signRecord = agreementRepository.saveSignRecord(signRecord);

        List<AgreementSignItem> signItems = new ArrayList<>();
        for (AgreementTemplate acceptedTemplate : acceptedTemplates) {
            signItems.add(AgreementSignItem.accepted(signRecord.getSignNo(), acceptedTemplate, now));
        }
        agreementRepository.replaceSignItems(signRecord.getSignNo(), signItems);

        openFundAccountIdempotent(userId, fundCode, currencyCode);
        LocalDateTime successAt = LocalDateTime.now();
        signRecord.markSucceeded(successAt);
        AgreementSignRecord savedRecord = agreementRepository.saveSignRecord(signRecord);
        userRepository.markFeatureEnabled(userId, UserFeatureCode.FUND_ACCOUNT_OPENED, successAt);
        return toFundOpenResult(savedRecord, true);
    }

    /**
     * 开通协议信息。
     */
    @Override
    @Transactional
    public OpenCreditProductWithAgreementResultDTO openAiCreditWithAgreement(OpenAiCreditWithAgreementCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        return openCreditProductWithAgreement(
                requirePositiveUserId(command.userId()),
                normalizeIdempotencyKey(command.idempotencyKey()),
                command.agreementAccepts(),
                AgreementBizType.AICREDIT_OPEN,
                PRODUCT_CODE_AICREDIT,
                AICREDIT_OPEN_SIGN_BIZ_TYPE,
                UserFeatureCode.AICREDIT_OPENED,
                this::findExistingAiCreditAccountNo,
                this::openAiCreditAccountIdempotent
        );
    }

    /**
     * 开通协议信息。
     */
    @Override
    @Transactional
    public OpenCreditProductWithAgreementResultDTO openAiLoanWithAgreement(OpenAiLoanWithAgreementCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        return openCreditProductWithAgreement(
                requirePositiveUserId(command.userId()),
                normalizeIdempotencyKey(command.idempotencyKey()),
                command.agreementAccepts(),
                AgreementBizType.AILOAN_OPEN,
                PRODUCT_CODE_AILOAN,
                AILOAN_OPEN_SIGN_BIZ_TYPE,
                UserFeatureCode.AILOAN_OPENED,
                this::findExistingAiLoanAccountNo,
                this::openAiLoanAccountIdempotent
        );
    }

    private OpenCreditProductWithAgreementResultDTO openCreditProductWithAgreement(Long userId,
                                                                                    String idempotencyKey,
                                                                                    List<AgreementAcceptCommand> agreementAccepts,
                                                                                    AgreementBizType bizType,
                                                                                    String productCode,
                                                                                    String signBizType,
                                                                                    UserFeatureCode featureCode,
                                                                                    AccountNoLoader existingAccountLoader,
                                                                                    AccountNoLoader openAccountLoader) {
        List<AgreementTemplate> activeTemplates = loadActiveTemplates(bizType);
        Map<String, AgreementTemplate> activeTemplateMap = buildTemplateKeyMap(activeTemplates);
        List<AgreementTemplate> acceptedTemplates = resolveAcceptedTemplates(agreementAccepts, activeTemplateMap, activeTemplates);
        Optional<AgreementSignRecord> existingOpt = agreementRepository.findSignRecordByIdempotencyKey(
                userId,
                bizType,
                idempotencyKey
        );

        String existingAccountNo = existingAccountLoader.load(userId);
        if (existingAccountNo != null) {
            AgreementSignRecord existingRecord = ensureSucceededAgreementRecord(
                    userId,
                    productCode,
                    DEFAULT_CURRENCY_CODE,
                    idempotencyKey,
                    acceptedTemplates,
                    existingOpt,
                    bizType,
                    signBizType
            );
            userRepository.markFeatureEnabled(
                    userId,
                    featureCode,
                    existingRecord.getOpenedAt() == null ? LocalDateTime.now() : existingRecord.getOpenedAt()
            );
            return toCreditOpenResult(existingRecord, productCode, existingAccountNo, true);
        }

        if (existingOpt.isPresent() && existingOpt.get().getSignStatus() == AgreementSignStatus.SUCCEEDED) {
            AgreementSignRecord existingRecord = existingOpt.get();
            String accountNo = requireAccountNo(openAccountLoader.load(userId), productCode);
            userRepository.markFeatureEnabled(
                    userId,
                    featureCode,
                    existingRecord.getOpenedAt() == null ? LocalDateTime.now() : existingRecord.getOpenedAt()
            );
            return toCreditOpenResult(existingRecord, productCode, accountNo, true);
        }

        LocalDateTime now = LocalDateTime.now();
        AgreementSignRecord signRecord = existingOpt.orElseGet(() -> AgreementSignRecord.pending(
                aiPayIdGenerator.generate(AiPayIdGenerator.DOMAIN_CREDIT_ACCOUNT, signBizType, String.valueOf(userId)),
                userId,
                bizType,
                productCode,
                DEFAULT_CURRENCY_CODE,
                idempotencyKey,
                now
        ));
        signRecord = agreementRepository.saveSignRecord(signRecord);

        agreementRepository.replaceSignItems(signRecord.getSignNo(), buildAcceptedSignItems(signRecord.getSignNo(), acceptedTemplates, now));

        String accountNo = requireAccountNo(openAccountLoader.load(userId), productCode);
        LocalDateTime successAt = LocalDateTime.now();
        signRecord.markSucceeded(successAt);
        AgreementSignRecord savedRecord = agreementRepository.saveSignRecord(signRecord);
        userRepository.markFeatureEnabled(userId, featureCode, successAt);
        return toCreditOpenResult(savedRecord, productCode, accountNo, true);
    }

    private AgreementSignRecord ensureSucceededAgreementRecord(Long userId,
                                                               String productCode,
                                                               String currencyCode,
                                                               String idempotencyKey,
                                                               List<AgreementTemplate> acceptedTemplates,
                                                               Optional<AgreementSignRecord> existingOpt,
                                                               AgreementBizType bizType,
                                                               String signBizType) {
        LocalDateTime now = LocalDateTime.now();
        AgreementSignRecord signRecord = existingOpt.orElseGet(() -> AgreementSignRecord.pending(
                aiPayIdGenerator.generate(resolveSignDomainCode(bizType), signBizType, String.valueOf(userId)),
                userId,
                bizType,
                productCode,
                currencyCode,
                idempotencyKey,
                now
        ));
        agreementRepository.replaceSignItems(signRecord.getSignNo(), buildAcceptedSignItems(signRecord.getSignNo(), acceptedTemplates, now));
        if (signRecord.getSignStatus() != AgreementSignStatus.SUCCEEDED) {
            signRecord = agreementRepository.saveSignRecord(signRecord);
            signRecord.markSucceeded(now);
            return agreementRepository.saveSignRecord(signRecord);
        }
        return signRecord;
    }

    private String resolveSignDomainCode(AgreementBizType bizType) {
        if (bizType == AgreementBizType.FUND_ACCOUNT_OPEN) {
            return AiPayIdGenerator.DOMAIN_FUND_ACCOUNT;
        }
        return AiPayIdGenerator.DOMAIN_CREDIT_ACCOUNT;
    }

    private List<AgreementSignItem> buildAcceptedSignItems(String signNo,
                                                           List<AgreementTemplate> acceptedTemplates,
                                                           LocalDateTime now) {
        List<AgreementSignItem> signItems = new ArrayList<>();
        for (AgreementTemplate acceptedTemplate : acceptedTemplates) {
            signItems.add(AgreementSignItem.accepted(signNo, acceptedTemplate, now));
        }
        return signItems;
    }

    private List<AgreementTemplate> loadActiveTemplates(AgreementBizType bizType) {
        List<AgreementTemplate> templates = agreementRepository.listActiveTemplates(bizType);
        if (templates.isEmpty()) {
            throw new IllegalArgumentException("agreement template not configured for bizType: " + bizType.name());
        }
        return templates;
    }

    private Map<String, AgreementTemplate> buildTemplateKeyMap(List<AgreementTemplate> templates) {
        Map<String, AgreementTemplate> map = new LinkedHashMap<>();
        for (AgreementTemplate template : templates) {
            map.put(buildTemplateKey(template.getTemplateCode(), template.getTemplateVersion()), template);
        }
        return map;
    }

    private List<AgreementTemplate> resolveAcceptedTemplates(List<AgreementAcceptCommand> accepts,
                                                             Map<String, AgreementTemplate> templateMap,
                                                             List<AgreementTemplate> templates) {
        if (accepts == null || accepts.isEmpty()) {
            throw new IllegalArgumentException("agreementAccepts must not be empty");
        }
        Map<String, AgreementTemplate> accepted = new LinkedHashMap<>();
        for (AgreementAcceptCommand accept : accepts) {
            if (accept == null) {
                continue;
            }
            String templateCode = normalizeRequired(accept.templateCode(), "templateCode");
            String templateVersion = normalizeRequired(accept.templateVersion(), "templateVersion");
            String key = buildTemplateKey(templateCode, templateVersion);
            AgreementTemplate template = templateMap.get(key);
            if (template == null) {
                throw new IllegalArgumentException("unsupported agreement acceptance: " + templateCode + "/" + templateVersion);
            }
            accepted.putIfAbsent(key, template);
        }
        if (accepted.isEmpty()) {
            throw new IllegalArgumentException("agreementAccepts must not be empty");
        }
        for (AgreementTemplate template : templates) {
            if (!template.isRequired()) {
                continue;
            }
            String requiredKey = buildTemplateKey(template.getTemplateCode(), template.getTemplateVersion());
            if (!accepted.containsKey(requiredKey)) {
                throw new IllegalArgumentException("required agreement not accepted: " + template.getTemplateCode());
            }
        }
        return new ArrayList<>(accepted.values());
    }

    private void openFundAccountIdempotent(Long userId, String fundCode, String currencyCode) {
        try {
            fundAccountService.createFundAccount(new CreateFundAccountCommand(userId, fundCode, currencyCode));
        } catch (IllegalArgumentException ex) {
            String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase(Locale.ROOT);
            if (!message.contains("fund account already exists")) {
                throw ex;
            }
        }
    }

    private FundAccountDTO findExistingFundAccount(Long userId, String fundCode) {
        try {
            return fundAccountService.getFundAccount(userId, fundCode);
        } catch (NoSuchElementException notFound) {
            return null;
        }
    }

    private String findExistingAiCreditAccountNo(Long userId) {
        try {
            CreditAccountDTO accountDTO = creditAccountService.getCreditAccountByUserId(userId, CreditAccountType.AICREDIT);
            return normalizeExistingAccountNo(accountDTO == null ? null : accountDTO.accountNo());
        } catch (NoSuchElementException notFound) {
            return null;
        }
    }

    private String openAiCreditAccountIdempotent(Long userId) {
        CreditAccountDTO accountDTO = creditAccountService.getOrCreateCreditAccountByUserId(
                userId,
                CreditAccountType.AICREDIT,
                null,
                null
        );
        return requireAccountNo(accountDTO == null ? null : accountDTO.accountNo(), PRODUCT_CODE_AICREDIT);
    }

    private String findExistingAiLoanAccountNo(Long userId) {
        try {
            LoanAccountDTO accountDTO = loanAccountService.getLoanAccountByUserId(userId);
            return normalizeExistingAccountNo(accountDTO == null ? null : accountDTO.accountNo());
        } catch (NoSuchElementException notFound) {
            return null;
        }
    }

    private String openAiLoanAccountIdempotent(Long userId) {
        LoanAccountDTO accountDTO = loanAccountService.getOrCreateLoanAccountByUserId(userId, null, null);
        return requireAccountNo(accountDTO == null ? null : accountDTO.accountNo(), PRODUCT_CODE_AILOAN);
    }

    private String normalizeExistingAccountNo(String accountNo) {
        if (accountNo == null || accountNo.isBlank()) {
            return null;
        }
        return accountNo.trim();
    }

    private String requireAccountNo(String accountNo, String productCode) {
        if (accountNo == null || accountNo.isBlank()) {
            throw new IllegalStateException("open " + productCode + " account failed: empty accountNo");
        }
        return accountNo.trim();
    }

    private String buildTemplateKey(String templateCode, String templateVersion) {
        return normalizeRequired(templateCode, "templateCode").toUpperCase(Locale.ROOT)
                + "#"
                + normalizeRequired(templateVersion, "templateVersion").toUpperCase(Locale.ROOT);
    }

    private Long requirePositiveUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        return userId;
    }

    private String normalizeFundCode(String fundCode) {
        if (fundCode == null || fundCode.isBlank()) {
            return DEFAULT_FUND_CODE;
        }
        return FundProductCodes.normalizeOrDefault(fundCode);
    }

    private String normalizeCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return DEFAULT_CURRENCY_CODE;
        }
        return currencyCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey must not be blank");
        }
        String normalized = idempotencyKey.trim();
        if (normalized.length() > 64) {
            throw new IllegalArgumentException("idempotencyKey length must be <= 64");
        }
        return normalized;
    }

    private String normalizeRequired(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return raw.trim();
    }

    private AgreementTemplateDTO toTemplateDTO(AgreementTemplate template) {
        return new AgreementTemplateDTO(
                template.getTemplateCode(),
                template.getTemplateVersion(),
                template.getBizType().name(),
                template.getTitle(),
                template.getContentUrl(),
                template.getContentHash(),
                template.isRequired()
        );
    }

    private OpenFundAccountWithAgreementResultDTO toFundOpenResult(AgreementSignRecord signRecord, boolean featureEnabled) {
        return new OpenFundAccountWithAgreementResultDTO(
                signRecord.getUserId(),
                signRecord.getFundCode(),
                signRecord.getCurrencyCode(),
                signRecord.getSignNo(),
                signRecord.getSignStatus().name(),
                signRecord.getSignedAt(),
                signRecord.getOpenedAt(),
                featureEnabled
        );
    }

    private OpenCreditProductWithAgreementResultDTO toCreditOpenResult(AgreementSignRecord signRecord,
                                                                        String productCode,
                                                                        String accountNo,
                                                                        boolean featureEnabled) {
        return new OpenCreditProductWithAgreementResultDTO(
                signRecord.getUserId(),
                productCode,
                accountNo,
                signRecord.getSignNo(),
                signRecord.getSignStatus().name(),
                signRecord.getSignedAt(),
                signRecord.getOpenedAt(),
                featureEnabled
        );
    }

    @FunctionalInterface
    private interface AccountNoLoader {
        String load(Long userId);
    }
}
