package cn.openaipay.application.accounting.service.impl;

import cn.openaipay.application.accounting.command.AcceptAccountingEventCommand;
import cn.openaipay.application.accounting.command.ReverseAccountingVoucherCommand;
import cn.openaipay.application.accounting.command.SaveAccountingSubjectCommand;
import cn.openaipay.application.accounting.dto.AccountingEntryDTO;
import cn.openaipay.application.accounting.dto.AccountingEventDTO;
import cn.openaipay.application.accounting.dto.AccountingLegDTO;
import cn.openaipay.application.accounting.dto.AccountingSubjectDTO;
import cn.openaipay.application.accounting.dto.AccountingSubjectSyncResultDTO;
import cn.openaipay.application.accounting.dto.AccountingVoucherDTO;
import cn.openaipay.application.accounting.service.AccountingService;
import cn.openaipay.domain.accounting.model.AccountingAmountDirection;
import cn.openaipay.domain.accounting.model.AccountingEntry;
import cn.openaipay.domain.accounting.model.AccountingEntryQuery;
import cn.openaipay.domain.accounting.model.AccountingEvent;
import cn.openaipay.domain.accounting.model.AccountingEventQuery;
import cn.openaipay.domain.accounting.model.AccountingEventStatus;
import cn.openaipay.domain.accounting.model.AccountingLeg;
import cn.openaipay.domain.accounting.model.AccountingSubject;
import cn.openaipay.domain.accounting.model.AccountingSubjectQuery;
import cn.openaipay.domain.accounting.model.AccountingVoucher;
import cn.openaipay.domain.accounting.model.AccountingVoucherQuery;
import cn.openaipay.domain.accounting.model.AccountingVoucherStatus;
import cn.openaipay.domain.accounting.model.DebitCreditFlag;
import cn.openaipay.domain.accounting.model.StandardAccountingSubjectCatalog;
import cn.openaipay.domain.accounting.model.SubjectType;
import cn.openaipay.domain.accounting.repository.AccountingEntryRepository;
import cn.openaipay.domain.accounting.repository.AccountingEventRepository;
import cn.openaipay.domain.accounting.repository.AccountingSubjectRepository;
import cn.openaipay.domain.accounting.repository.AccountingVoucherRepository;
import cn.openaipay.domain.accounting.service.AccountingPostingDomainService;
import org.joda.money.CurrencyUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * 会计应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Service
public class AccountingServiceImpl implements AccountingService {

    /** 事件信息 */
    private final AccountingEventRepository accountingEventRepository;
    /** 核算凭证仓储信息 */
    private final AccountingVoucherRepository accountingVoucherRepository;
    /** 分录信息 */
    private final AccountingEntryRepository accountingEntryRepository;
    /** 科目信息 */
    private final AccountingSubjectRepository accountingSubjectRepository;
    /** 域信息 */
    private final AccountingPostingDomainService accountingPostingDomainService;

    public AccountingServiceImpl(AccountingEventRepository accountingEventRepository,
                                            AccountingVoucherRepository accountingVoucherRepository,
                                            AccountingEntryRepository accountingEntryRepository,
                                            AccountingSubjectRepository accountingSubjectRepository,
                                            AccountingPostingDomainService accountingPostingDomainService) {
        this.accountingEventRepository = accountingEventRepository;
        this.accountingVoucherRepository = accountingVoucherRepository;
        this.accountingEntryRepository = accountingEntryRepository;
        this.accountingSubjectRepository = accountingSubjectRepository;
        this.accountingPostingDomainService = accountingPostingDomainService;
    }

    /**
     * 处理事件信息。
     */
    @Override
    @Transactional
    public AccountingVoucherDTO acceptEvent(AcceptAccountingEventCommand command) {
        AccountingEvent event = buildEvent(command);
        AccountingEvent existing = accountingEventRepository.findByEventId(event.getEventId())
                .or(() -> accountingEventRepository.findByIdempotencyKey(event.getIdempotencyKey()))
                .orElse(null);
        if (existing == null) {
            existing = accountingEventRepository.save(event);
        }
        Optional<AccountingVoucher> existingVoucher = accountingVoucherRepository.findByEventId(existing.getEventId());
        if (existingVoucher.isPresent()) {
            return toVoucherDTO(existingVoucher.get(), true);
        }
        return toVoucherDTO(processEvent(existing), true);
    }

    /**
     * 获取事件信息。
     */
    @Override
    @Transactional(readOnly = true)
    public AccountingEventDTO getEvent(String eventId) {
        AccountingEvent event = accountingEventRepository.findByEventId(normalizeRequired(eventId, "eventId"))
                .orElseThrow(() -> new NoSuchElementException("accounting event not found: " + eventId));
        String postedVoucherNo = accountingVoucherRepository.findByEventId(event.getEventId())
                .map(AccountingVoucher::getVoucherNo)
                .orElse(null);
        return toEventDTO(event, postedVoucherNo);
    }

    /**
     * 查询事件信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountingEventDTO> listEvents(String eventId,
                                               String eventType,
                                               String sourceBizType,
                                               String sourceBizNo,
                                               String bizOrderNo,
                                               String tradeOrderNo,
                                               String payOrderNo,
                                               String status,
                                               Integer limit) {
        AccountingEventStatus eventStatus = parseEnum(status, AccountingEventStatus.class);
        return accountingEventRepository.list(new AccountingEventQuery(
                        normalizeOptional(eventId),
                        normalizeOptional(eventType),
                        normalizeOptional(sourceBizType),
                        normalizeOptional(sourceBizNo),
                        normalizeOptional(bizOrderNo),
                        normalizeOptional(tradeOrderNo),
                        normalizeOptional(payOrderNo),
                        eventStatus,
                        normalizeLimit(limit)
                )).stream()
                .map(event -> toEventDTO(
                        event,
                        accountingVoucherRepository.findByEventId(event.getEventId()).map(AccountingVoucher::getVoucherNo).orElse(null)
                ))
                .toList();
    }

    /**
     * 重试事件信息。
     */
    @Override
    @Transactional
    public AccountingVoucherDTO retryEvent(String eventId) {
        AccountingEvent event = accountingEventRepository.findByEventId(normalizeRequired(eventId, "eventId"))
                .orElseThrow(() -> new NoSuchElementException("accounting event not found: " + eventId));
        Optional<AccountingVoucher> existingVoucher = accountingVoucherRepository.findByEventId(event.getEventId());
        if (existingVoucher.isPresent()) {
            return toVoucherDTO(existingVoucher.get(), true);
        }
        return toVoucherDTO(processEvent(event), true);
    }

    /**
     * 获取凭证信息。
     */
    @Override
    @Transactional(readOnly = true)
    public AccountingVoucherDTO getVoucher(String voucherNo) {
        AccountingVoucher voucher = accountingVoucherRepository.findByVoucherNo(normalizeRequired(voucherNo, "voucherNo"))
                .orElseThrow(() -> new NoSuchElementException("accounting voucher not found: " + voucherNo));
        return toVoucherDTO(voucher, true);
    }

    /**
     * 查询凭证信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountingVoucherDTO> listVouchers(String voucherNo,
                                                   String sourceBizType,
                                                   String sourceBizNo,
                                                   String bizOrderNo,
                                                   String tradeOrderNo,
                                                   String payOrderNo,
                                                   String status,
                                                   Integer limit) {
        AccountingVoucherStatus voucherStatus = parseEnum(status, AccountingVoucherStatus.class);
        return accountingVoucherRepository.list(new AccountingVoucherQuery(
                        normalizeOptional(voucherNo),
                        normalizeOptional(sourceBizType),
                        normalizeOptional(sourceBizNo),
                        normalizeOptional(bizOrderNo),
                        normalizeOptional(tradeOrderNo),
                        normalizeOptional(payOrderNo),
                        voucherStatus,
                        normalizeLimit(limit)
                )).stream()
                .map(voucher -> toVoucherDTO(voucher, false))
                .toList();
    }

    /**
     * 查询业务数据列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountingEntryDTO> listEntries(String voucherNo,
                                                String subjectCode,
                                                String ownerType,
                                                Long ownerId,
                                                String bizOrderNo,
                                                String tradeOrderNo,
                                                String payOrderNo,
                                                String sourceBizType,
                                                String sourceBizNo,
                                                Integer limit) {
        Map<String, String> subjectNameCache = new HashMap<>();
        return accountingEntryRepository.list(new AccountingEntryQuery(
                        normalizeOptional(voucherNo),
                        normalizeOptional(subjectCode),
                        normalizeOptional(ownerType),
                        ownerId,
                        normalizeOptional(bizOrderNo),
                        normalizeOptional(tradeOrderNo),
                        normalizeOptional(payOrderNo),
                        normalizeOptional(sourceBizType),
                        normalizeOptional(sourceBizNo),
                        normalizeLimit(limit)
                )).stream()
                .map(entry -> toEntryDTO(entry, subjectNameCache))
                .toList();
    }

    /**
     * 处理凭证信息。
     */
    @Override
    @Transactional
    public AccountingVoucherDTO reverseVoucher(ReverseAccountingVoucherCommand command) {
        String voucherNo = normalizeRequired(command.voucherNo(), "voucherNo");
        AccountingVoucher originalVoucher = accountingVoucherRepository.findByVoucherNo(voucherNo)
                .orElseThrow(() -> new NoSuchElementException("accounting voucher not found: " + voucherNo));
        String reverseEventId = "REV-" + voucherNo;
        Optional<AccountingVoucher> existingReverse = accountingVoucherRepository.findByEventId(reverseEventId);
        if (existingReverse.isPresent()) {
            return toVoucherDTO(existingReverse.get(), true);
        }
        AccountingVoucher reverseVoucher = accountingPostingDomainService.buildReverseVoucher(
                originalVoucher,
                reverseEventId,
                normalizeOptional(command.reverseReason()),
                normalizeOptional(command.operator())
        );
        reverseVoucher.markPosted(LocalDateTime.now());
        AccountingVoucher savedReverse = accountingVoucherRepository.save(reverseVoucher);
        accountingEntryRepository.saveAll(reverseVoucher.getEntries());
        originalVoucher.markReversed(LocalDateTime.now());
        accountingVoucherRepository.save(originalVoucher);
        return toVoucherDTO(savedReverse, true);
    }

    /**
     * 查询科目信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountingSubjectDTO> listSubjects(Boolean enabled, String subjectType, Integer limit) {
        return accountingSubjectRepository.list(new AccountingSubjectQuery(
                        enabled,
                        normalizeOptional(subjectType),
                        normalizeLimit(limit)
                )).stream()
                .map(this::toSubjectDTO)
                .toList();
    }

    /**
     * 保存科目信息。
     */
    @Override
    @Transactional
    public AccountingSubjectDTO saveSubject(SaveAccountingSubjectCommand command) {
        String subjectCode = normalizeRequired(command.subjectCode(), "subjectCode");
        String subjectName = normalizeRequired(command.subjectName(), "subjectName");
        SubjectType subjectType = parseEnum(command.subjectType(), SubjectType.class, SubjectType.ASSET);
        DebitCreditFlag balanceDirection = parseEnum(command.balanceDirection(), DebitCreditFlag.class, DebitCreditFlag.DEBIT);
        String parentSubjectCode = normalizeOptional(command.parentSubjectCode());
        String remark = normalizeOptional(command.remark());
        boolean enabled = Boolean.TRUE.equals(command.enabled());
        List<AccountingSubject> allSubjects = listAllSubjects();
        Map<String, AccountingSubject> subjectMap = buildSubjectMap(allSubjects);
        LocalDateTime now = LocalDateTime.now();
        AccountingSubject existing = accountingSubjectRepository.findBySubjectCode(subjectCode).orElse(null);
        validateSubjectStructure(subjectCode, subjectType, balanceDirection, parentSubjectCode, command.levelNo(), enabled, existing, subjectMap);
        int resolvedLevelNo = resolveLevelNo(parentSubjectCode, command.levelNo(), subjectMap);
        AccountingSubject subject = new AccountingSubject(
                existing == null ? null : existing.getId(),
                subjectCode,
                subjectName,
                subjectType,
                balanceDirection,
                parentSubjectCode,
                resolvedLevelNo,
                enabled,
                remark,
                existing == null ? now : existing.getCreatedAt(),
                now
        );
        return toSubjectDTO(accountingSubjectRepository.save(subject));
    }

    /**
     * 更新科目状态。
     */
    @Override
    @Transactional
    public AccountingSubjectDTO updateSubjectStatus(String subjectCode, Boolean enabled) {
        String normalizedSubjectCode = normalizeRequired(subjectCode, "subjectCode");
        boolean targetEnabled = Boolean.TRUE.equals(enabled);
        AccountingSubject existing = accountingSubjectRepository.findBySubjectCode(normalizedSubjectCode)
                .orElseThrow(() -> new NoSuchElementException("会计科目不存在：" + normalizedSubjectCode));
        if (existing.isEnabled() == targetEnabled) {
            return toSubjectDTO(existing);
        }
        List<AccountingSubject> allSubjects = listAllSubjects();
        Map<String, AccountingSubject> subjectMap = buildSubjectMap(allSubjects);
        validateSubjectStatusChange(existing, targetEnabled, subjectMap);
        AccountingSubject updated = new AccountingSubject(
                existing.getId(),
                existing.getSubjectCode(),
                existing.getSubjectName(),
                existing.getSubjectType(),
                existing.getBalanceDirection(),
                existing.getParentSubjectCode(),
                existing.getLevelNo(),
                targetEnabled,
                existing.getRemark(),
                existing.getCreatedAt(),
                LocalDateTime.now()
        );
        return toSubjectDTO(accountingSubjectRepository.save(updated));
    }

    /**
     * 初始化科目信息。
     */
    @Override
    @Transactional
    public AccountingSubjectSyncResultDTO initializeStandardSubjects() {
        List<AccountingSubject> allSubjects = listAllSubjects();
        Map<String, AccountingSubject> existingMap = buildSubjectMap(allSubjects);
        int addedCount = 0;
        int unchangedCount = 0;
        LocalDateTime now = LocalDateTime.now();
        for (StandardAccountingSubjectCatalog.SubjectDefinition definition : StandardAccountingSubjectCatalog.definitions()) {
            if (existingMap.containsKey(definition.subjectCode())) {
                unchangedCount += 1;
                continue;
            }
            accountingSubjectRepository.save(definition.toSubject(now));
            addedCount += 1;
        }
        int standardSubjectCount = StandardAccountingSubjectCatalog.definitions().size();
        return new AccountingSubjectSyncResultDTO(
                "INITIALIZE",
                standardSubjectCount,
                addedCount,
                0,
                0,
                unchangedCount,
                "标准科目初始化完成：新增 " + addedCount + " 条，已存在 " + unchangedCount + " 条"
        );
    }

    /**
     * 重置科目信息。
     */
    @Override
    @Transactional
    public AccountingSubjectSyncResultDTO resetStandardSubjects() {
        List<AccountingSubject> allSubjects = listAllSubjects();
        Map<String, AccountingSubject> existingMap = buildSubjectMap(allSubjects);
        Set<String> standardSubjectCodes = StandardAccountingSubjectCatalog.subjectCodes();
        LocalDateTime now = LocalDateTime.now();
        int addedCount = 0;
        int updatedCount = 0;
        int disabledCount = 0;
        int unchangedCount = 0;

        for (StandardAccountingSubjectCatalog.SubjectDefinition definition : StandardAccountingSubjectCatalog.definitions()) {
            AccountingSubject existing = existingMap.get(definition.subjectCode());
            AccountingSubject standardSubject = definition.toSubject(now);
            if (existing == null) {
                accountingSubjectRepository.save(standardSubject);
                addedCount += 1;
                continue;
            }
            AccountingSubject merged = new AccountingSubject(
                    existing.getId(),
                    definition.subjectCode(),
                    definition.subjectName(),
                    definition.subjectType(),
                    definition.balanceDirection(),
                    definition.parentSubjectCode(),
                    definition.levelNo(),
                    definition.enabled(),
                    definition.remark(),
                    existing.getCreatedAt(),
                    now
            );
            if (sameSubjectDefinition(existing, merged)) {
                unchangedCount += 1;
                continue;
            }
            accountingSubjectRepository.save(merged);
            updatedCount += 1;
        }

        for (AccountingSubject subject : allSubjects) {
            if (standardSubjectCodes.contains(subject.getSubjectCode()) || !subject.isEnabled()) {
                continue;
            }
            AccountingSubject disabledSubject = new AccountingSubject(
                    subject.getId(),
                    subject.getSubjectCode(),
                    subject.getSubjectName(),
                    subject.getSubjectType(),
                    subject.getBalanceDirection(),
                    subject.getParentSubjectCode(),
                    subject.getLevelNo(),
                    false,
                    subject.getRemark(),
                    subject.getCreatedAt(),
                    now
            );
            accountingSubjectRepository.save(disabledSubject);
            disabledCount += 1;
        }

        int standardSubjectCount = StandardAccountingSubjectCatalog.definitions().size();
        return new AccountingSubjectSyncResultDTO(
                "RESET",
                standardSubjectCount,
                addedCount,
                updatedCount,
                disabledCount,
                unchangedCount,
                "标准科目重置完成：新增 " + addedCount + " 条，更新 " + updatedCount + " 条，停用非标准科目 " + disabledCount + " 条"
        );
    }

    private AccountingVoucher processEvent(AccountingEvent event) {
        try {
            event.markProcessing(LocalDateTime.now());
            accountingEventRepository.save(event);
            AccountingVoucher voucher = accountingPostingDomainService.buildVoucher(event);
            voucher.markPosted(LocalDateTime.now());
            AccountingVoucher savedVoucher = accountingVoucherRepository.save(voucher);
            accountingEntryRepository.saveAll(voucher.getEntries());
            event.markPosted(LocalDateTime.now());
            accountingEventRepository.save(event);
            return savedVoucher;
        } catch (RuntimeException ex) {
            event.markFailed(LocalDateTime.now());
            accountingEventRepository.save(event);
            throw ex;
        }
    }

    private AccountingEvent buildEvent(AcceptAccountingEventCommand command) {
        String currencyCode = normalizeOptional(command.currencyCode());
        CurrencyUnit currencyUnit = CurrencyUnit.of(currencyCode == null ? "CNY" : currencyCode);
        List<AccountingLeg> legs = command.legs() == null ? List.of() : command.legs().stream()
                .map(leg -> new AccountingLeg(
                        leg.legNo(),
                        leg.accountDomain(),
                        leg.accountType(),
                        leg.accountNo(),
                        leg.ownerType(),
                        leg.ownerId(),
                        leg.amount(),
                        parseEnum(leg.direction(), AccountingAmountDirection.class, AccountingAmountDirection.OUT),
                        leg.bizRole(),
                        leg.subjectHint(),
                        leg.referenceNo(),
                        leg.metadata()
                ))
                .toList();
        return new AccountingEvent(
                null,
                normalizeRequired(command.eventId(), "eventId"),
                normalizeRequired(command.eventType(), "eventType"),
                command.eventVersion(),
                normalizeRequired(defaultValue(command.bookId(), "BOOK_DEFAULT"), "bookId"),
                normalizeRequired(defaultValue(command.sourceSystem(), "PAY"), "sourceSystem"),
                normalizeRequired(command.sourceBizType(), "sourceBizType"),
                normalizeRequired(command.sourceBizNo(), "sourceBizNo"),
                normalizeOptional(command.bizOrderNo()),
                normalizeOptional(command.requestNo()),
                normalizeOptional(command.tradeOrderNo()),
                normalizeOptional(command.payOrderNo()),
                normalizeOptional(command.businessSceneCode()),
                normalizeOptional(command.businessDomainCode()),
                command.payerUserId(),
                command.payeeUserId(),
                currencyUnit,
                command.occurredAt(),
                command.occurredAt() == null ? null : command.occurredAt().toLocalDate(),
                normalizeRequired(command.idempotencyKey(), "idempotencyKey"),
                normalizeOptional(command.globalTxId()),
                normalizeOptional(command.traceId()),
                normalizeOptional(command.payload()),
                AccountingEventStatus.NEW,
                legs,
                command.occurredAt(),
                command.occurredAt()
        );
    }

    private AccountingEventDTO toEventDTO(AccountingEvent event, String postedVoucherNo) {
        Map<String, String> subjectNameCache = new HashMap<>();
        return new AccountingEventDTO(
                event.getEventId(),
                event.getEventType(),
                event.getEventVersion(),
                event.getBookId(),
                event.getSourceSystem(),
                event.getSourceBizType(),
                event.getSourceBizNo(),
                event.getBizOrderNo(),
                event.getRequestNo(),
                event.getTradeOrderNo(),
                event.getPayOrderNo(),
                event.getBusinessSceneCode(),
                event.getBusinessDomainCode(),
                event.getPayerUserId(),
                event.getPayeeUserId(),
                event.getCurrencyUnit().getCode(),
                event.getOccurredAt(),
                event.getPostingDate(),
                event.getIdempotencyKey(),
                event.getGlobalTxId(),
                event.getTraceId(),
                event.getPayload(),
                event.getStatus().name(),
                postedVoucherNo,
                event.getCreatedAt(),
                event.getUpdatedAt(),
                event.getLegs().stream().map(leg -> toLegDTO(leg, subjectNameCache)).toList()
        );
    }

    private AccountingVoucherDTO toVoucherDTO(AccountingVoucher voucher, boolean includeEntries) {
        Map<String, String> subjectNameCache = new HashMap<>();
        List<AccountingEntryDTO> entries = includeEntries
                ? accountingEntryRepository.list(new AccountingEntryQuery(
                        voucher.getVoucherNo(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        500
                )).stream().map(entry -> toEntryDTO(entry, subjectNameCache)).toList()
                : List.of();
        return new AccountingVoucherDTO(
                voucher.getVoucherNo(),
                voucher.getBookId(),
                voucher.getVoucherType().name(),
                voucher.getEventId(),
                voucher.getSourceBizType(),
                voucher.getSourceBizNo(),
                voucher.getBizOrderNo(),
                voucher.getTradeOrderNo(),
                voucher.getPayOrderNo(),
                voucher.getBusinessSceneCode(),
                voucher.getBusinessDomainCode(),
                voucher.getStatus().name(),
                voucher.getCurrencyUnit().getCode(),
                voucher.getTotalDebitAmount(),
                voucher.getTotalCreditAmount(),
                voucher.getOccurredAt(),
                voucher.getPostingDate(),
                voucher.getReversedVoucherNo(),
                voucher.getCreatedAt(),
                voucher.getUpdatedAt(),
                entries
        );
    }

    private AccountingEntryDTO toEntryDTO(AccountingEntry entry, Map<String, String> subjectNameCache) {
        return new AccountingEntryDTO(
                entry.getVoucherNo(),
                entry.getLineNo(),
                entry.getSubjectCode(),
                resolveSubjectName(entry.getSubjectCode(), subjectNameCache),
                entry.getDcFlag().name(),
                entry.getAmount(),
                entry.getOwnerType(),
                entry.getOwnerId(),
                entry.getAccountDomain(),
                entry.getAccountType(),
                entry.getAccountNo(),
                entry.getBizRole(),
                entry.getBizOrderNo(),
                entry.getTradeOrderNo(),
                entry.getPayOrderNo(),
                entry.getSourceBizType(),
                entry.getSourceBizNo(),
                entry.getReferenceNo(),
                entry.getEntryMemo(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }

    private AccountingLegDTO toLegDTO(AccountingLeg leg, Map<String, String> subjectNameCache) {
        return new AccountingLegDTO(
                leg.getLegNo(),
                leg.getAccountDomain(),
                leg.getAccountType(),
                leg.getAccountNo(),
                leg.getOwnerType(),
                leg.getOwnerId(),
                leg.getAmount(),
                leg.getDirection().name(),
                leg.getBizRole(),
                leg.getSubjectHint(),
                resolveSubjectName(leg.getSubjectHint(), subjectNameCache),
                leg.getReferenceNo(),
                leg.getMetadata()
        );
    }

    private String resolveSubjectName(String subjectCode, Map<String, String> subjectNameCache) {
        String normalized = normalizeOptional(subjectCode);
        if (normalized == null) {
            return null;
        }
        if (subjectNameCache != null && subjectNameCache.containsKey(normalized)) {
            return subjectNameCache.get(normalized);
        }
        String subjectName = accountingSubjectRepository.findBySubjectCode(normalized)
                .map(AccountingSubject::getSubjectName)
                .orElse(null);
        if (subjectNameCache != null) {
            subjectNameCache.put(normalized, subjectName);
        }
        return subjectName;
    }

    private AccountingSubjectDTO toSubjectDTO(AccountingSubject subject) {
        return new AccountingSubjectDTO(
                subject.getSubjectCode(),
                subject.getSubjectName(),
                subject.getSubjectType().name(),
                subject.getBalanceDirection().name(),
                subject.getParentSubjectCode(),
                subject.getLevelNo(),
                subject.isEnabled(),
                subject.getRemark(),
                subject.getCreatedAt(),
                subject.getUpdatedAt()
        );
    }

    private List<AccountingSubject> listAllSubjects() {
        return accountingSubjectRepository.list(new AccountingSubjectQuery(null, null, 1000));
    }

    private Map<String, AccountingSubject> buildSubjectMap(List<AccountingSubject> subjects) {
        Map<String, AccountingSubject> subjectMap = new LinkedHashMap<>();
        for (AccountingSubject subject : subjects) {
            subjectMap.put(subject.getSubjectCode(), subject);
        }
        return subjectMap;
    }

    private void validateSubjectStructure(String subjectCode,
                                          SubjectType subjectType,
                                          DebitCreditFlag balanceDirection,
                                          String parentSubjectCode,
                                          Integer levelNo,
                                          boolean enabled,
                                          AccountingSubject existing,
                                          Map<String, AccountingSubject> subjectMap) {
        if (parentSubjectCode != null && parentSubjectCode.equals(subjectCode)) {
            throw new IllegalArgumentException("父级科目不能是自己");
        }
        AccountingSubject parent = parentSubjectCode == null ? null : subjectMap.get(parentSubjectCode);
        if (parentSubjectCode != null && parent == null) {
            throw new IllegalArgumentException("父级科目不存在：" + parentSubjectCode);
        }
        if (parent != null) {
            if (parent.getSubjectType() != subjectType) {
                throw new IllegalArgumentException("子科目类型必须与父级科目一致");
            }
            if (parent.getBalanceDirection() != balanceDirection) {
                throw new IllegalArgumentException("子科目余额方向必须与父级科目一致");
            }
            if (enabled && !parent.isEnabled()) {
                throw new IllegalArgumentException("父级科目未启用，不能启用当前科目");
            }
            if (wouldCreateCycle(subjectCode, parentSubjectCode, subjectMap)) {
                throw new IllegalArgumentException("父级科目不能选择当前科目的下级");
            }
        }
        if (existing != null && hasDirectChildren(subjectCode, subjectMap) && isStructureChanged(existing, parentSubjectCode, levelNo, subjectType, balanceDirection, subjectMap)) {
            throw new IllegalArgumentException("当前科目存在下级科目，不允许修改父级、层级、科目类型或余额方向");
        }
        if (!enabled && hasEnabledDirectChildren(subjectCode, subjectMap)) {
            throw new IllegalArgumentException("存在已启用的下级科目，不能停用当前科目");
        }
        resolveLevelNo(parentSubjectCode, levelNo, subjectMap);
    }

    private void validateSubjectStatusChange(AccountingSubject subject,
                                             boolean targetEnabled,
                                             Map<String, AccountingSubject> subjectMap) {
        if (!targetEnabled && hasEnabledDirectChildren(subject.getSubjectCode(), subjectMap)) {
            throw new IllegalArgumentException("存在已启用的下级科目，不能停用当前科目");
        }
        if (targetEnabled && subject.getParentSubjectCode() != null) {
            AccountingSubject parent = subjectMap.get(subject.getParentSubjectCode());
            if (parent == null) {
                throw new IllegalArgumentException("父级科目不存在：" + subject.getParentSubjectCode());
            }
            if (!parent.isEnabled()) {
                throw new IllegalArgumentException("父级科目未启用，不能启用当前科目");
            }
        }
    }

    private int resolveLevelNo(String parentSubjectCode,
                               Integer requestedLevelNo,
                               Map<String, AccountingSubject> subjectMap) {
        int expectedLevelNo = 1;
        if (parentSubjectCode != null) {
            AccountingSubject parent = subjectMap.get(parentSubjectCode);
            if (parent == null) {
                throw new IllegalArgumentException("父级科目不存在：" + parentSubjectCode);
            }
            expectedLevelNo = parent.getLevelNo() + 1;
        }
        if (requestedLevelNo != null && requestedLevelNo > 0 && requestedLevelNo != expectedLevelNo) {
            throw new IllegalArgumentException("层级必须为 " + expectedLevelNo);
        }
        return expectedLevelNo;
    }

    private boolean sameSubjectDefinition(AccountingSubject left, AccountingSubject right) {
        return left.isEnabled() == right.isEnabled()
                && left.getLevelNo().equals(right.getLevelNo())
                && left.getSubjectCode().equals(right.getSubjectCode())
                && left.getSubjectName().equals(right.getSubjectName())
                && left.getSubjectType() == right.getSubjectType()
                && left.getBalanceDirection() == right.getBalanceDirection()
                && equalsNullable(left.getParentSubjectCode(), right.getParentSubjectCode())
                && equalsNullable(left.getRemark(), right.getRemark());
    }

    private boolean isStructureChanged(AccountingSubject existing,
                                       String nextParentSubjectCode,
                                       Integer nextLevelNo,
                                       SubjectType nextSubjectType,
                                       DebitCreditFlag nextBalanceDirection,
                                       Map<String, AccountingSubject> subjectMap) {
        return !equalsNullable(existing.getParentSubjectCode(), nextParentSubjectCode)
                || existing.getLevelNo() != resolveLevelNo(nextParentSubjectCode, nextLevelNo, subjectMap)
                || existing.getSubjectType() != nextSubjectType
                || existing.getBalanceDirection() != nextBalanceDirection;
    }

    private boolean hasDirectChildren(String subjectCode, Map<String, AccountingSubject> subjectMap) {
        for (AccountingSubject subject : subjectMap.values()) {
            if (subjectCode.equals(subject.getParentSubjectCode())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasEnabledDirectChildren(String subjectCode, Map<String, AccountingSubject> subjectMap) {
        for (AccountingSubject subject : subjectMap.values()) {
            if (subjectCode.equals(subject.getParentSubjectCode()) && subject.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    private boolean wouldCreateCycle(String subjectCode,
                                     String parentSubjectCode,
                                     Map<String, AccountingSubject> subjectMap) {
        String currentParentCode = parentSubjectCode;
        while (currentParentCode != null) {
            if (currentParentCode.equals(subjectCode)) {
                return true;
            }
            AccountingSubject parent = subjectMap.get(currentParentCode);
            currentParentCode = parent == null ? null : parent.getParentSubjectCode();
        }
        return false;
    }

    private boolean equalsNullable(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }

    private Integer normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 50;
        }
        return Math.min(limit, 200);
    }

    private String normalizeRequired(String value, String fieldName) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String defaultValue(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumType) {
        return parseEnum(value, enumType, null);
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumType, E defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase());
        } catch (IllegalArgumentException ignore) {
            return defaultValue;
        }
    }
}
