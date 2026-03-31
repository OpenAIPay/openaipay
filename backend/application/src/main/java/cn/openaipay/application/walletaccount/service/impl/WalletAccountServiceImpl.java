package cn.openaipay.application.walletaccount.service.impl;

import cn.openaipay.application.walletaccount.command.CreateWalletAccountCommand;
import cn.openaipay.application.walletaccount.command.WalletFreezeDeductCommand;
import cn.openaipay.application.walletaccount.command.WalletFreezeHoldCommand;
import cn.openaipay.application.walletaccount.command.WalletFreezeQueryCommand;
import cn.openaipay.application.walletaccount.command.WalletFreezeReleaseCommand;
import cn.openaipay.application.walletaccount.command.WalletTccCancelCommand;
import cn.openaipay.application.walletaccount.command.WalletTccConfirmCommand;
import cn.openaipay.application.walletaccount.command.WalletTccTryCommand;
import cn.openaipay.application.walletaccount.dto.WalletAccountDTO;
import cn.openaipay.application.walletaccount.dto.WalletFreezeOperationDTO;
import cn.openaipay.application.walletaccount.dto.WalletFreezeRecordDTO;
import cn.openaipay.application.walletaccount.dto.WalletFreezeSummaryDTO;
import cn.openaipay.application.walletaccount.dto.WalletFreezeSummaryItemDTO;
import cn.openaipay.application.walletaccount.dto.WalletTccBranchDTO;
import cn.openaipay.application.walletaccount.service.WalletAccountService;
import cn.openaipay.domain.walletaccount.model.TccOperationType;
import cn.openaipay.domain.walletaccount.model.WalletAccount;
import cn.openaipay.domain.walletaccount.model.WalletFreezeRecord;
import cn.openaipay.domain.walletaccount.model.WalletFreezeStatus;
import cn.openaipay.domain.walletaccount.model.WalletFreezeType;
import cn.openaipay.domain.walletaccount.model.WalletTccBranch;
import cn.openaipay.domain.walletaccount.model.WalletTccBranchStatus;
import cn.openaipay.domain.walletaccount.repository.WalletAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.joda.money.Money;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * 钱包账户应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class WalletAccountServiceImpl implements WalletAccountService {

    /** 默认币种常量 */
    private static final String DEFAULT_CURRENCY = "CNY";
    /** WalletAccountRepository组件 */
    private final WalletAccountRepository walletAccountRepository;

    public WalletAccountServiceImpl(WalletAccountRepository walletAccountRepository) {
        this.walletAccountRepository = walletAccountRepository;
    }

    /**
     * 创建钱包信息。
     */
    @Override
    @Transactional
    public Long createWalletAccount(CreateWalletAccountCommand command) {
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        String currencyCode = normalizeCurrency(command.currencyCode());
        if (walletAccountRepository.findByUserIdAndCurrency(command.userId(), currencyCode).isPresent()) {
            throw new IllegalArgumentException("wallet account already exists: " + command.userId() + ":" + currencyCode);
        }

        LocalDateTime now = LocalDateTime.now();
        WalletAccount walletAccount = WalletAccount.open(
                command.userId(),
                currencyCode,
                now
        );
        walletAccountRepository.save(walletAccount);
        return walletAccount.getUserId();
    }

    /**
     * 获取钱包信息。
     */
    @Override
    @Transactional(readOnly = true)
    public WalletAccountDTO getWalletAccount(Long userId) {
        WalletAccount walletAccount = walletAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("wallet account not found: " + userId));
        return toWalletAccountDTO(walletAccount);
    }

    /**
     * 获取钱包信息。
     */
    @Override
    @Transactional(readOnly = true)
    public WalletAccountDTO getWalletAccount(Long userId, String currencyCode) {
        WalletAccount walletAccount = walletAccountRepository.findByUserIdAndCurrency(userId, normalizeCurrency(currencyCode))
                .orElseThrow(() -> new NoSuchElementException("wallet account not found: " + userId + ":" + currencyCode));
        return toWalletAccountDTO(walletAccount);
    }

    /**
     * 获取钱包信息，不存在时自动补齐默认账户。
     */
    @Override
    @Transactional
    public WalletAccountDTO getOrCreateWalletAccount(Long userId, String currencyCode) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        String normalizedCurrency = normalizeCurrency(currencyCode);
        return walletAccountRepository.findByUserIdAndCurrency(userId, normalizedCurrency)
                .map(this::toWalletAccountDTO)
                .orElseGet(() -> toWalletAccountDTO(createWalletAccountOrLoadExisting(userId, normalizedCurrency)));
    }

    /**
     * 处理TCCTRY信息。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WalletTccBranchDTO tccTry(WalletTccTryCommand command) {
        validateXidAndBranchId(command.xid(), command.branchId());
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        TccOperationType operationType = parseOperationType(command.operationType());
        WalletFreezeType freezeType = parseFreezeType(command.freezeType());
        Money amount = normalizeAmount(command.amount());
        LocalDateTime now = LocalDateTime.now();

        WalletTccBranch existingBranch = walletAccountRepository.findBranchForUpdate(command.xid(), command.branchId())
                .orElse(null);
        if (existingBranch != null) {
            if (existingBranch.getBranchStatus() == WalletTccBranchStatus.CANCELED) {
                throw new IllegalStateException("branch has been canceled, try is not allowed");
            }
            return new WalletTccBranchDTO(
                    existingBranch.getXid(),
                    existingBranch.getBranchId(),
                    existingBranch.getBranchStatus().name(),
                    "try duplicated, idempotent return"
            );
        }

        WalletAccount walletAccount = walletAccountRepository.findByUserIdAndCurrencyForUpdate(
                        command.userId(),
                        amount.getCurrencyUnit().getCode())
                .orElseThrow(() -> new NoSuchElementException("wallet account not found: " + command.userId()));
        ensureCurrencyMatched(walletAccount, amount);

        walletAccount.hold(operationType, amount, now);
        walletAccountRepository.save(walletAccount);

        WalletTccBranch branch = WalletTccBranch.newTry(
                command.xid(),
                command.branchId(),
                command.userId(),
                operationType,
                freezeType,
                amount,
                command.businessNo(),
                now
        );
        WalletFreezeRecord freezeRecord = WalletFreezeRecord.newFrozen(
                command.xid(),
                command.branchId(),
                command.userId(),
                freezeType,
                operationType,
                amount,
                command.businessNo(),
                now
        );
        walletAccountRepository.createBranch(branch);
        walletAccountRepository.createFreezeRecord(freezeRecord);
        return new WalletTccBranchDTO(branch.getXid(), branch.getBranchId(), branch.getBranchStatus().name(), "try success");
    }

    /**
     * 处理TCC信息。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WalletTccBranchDTO tccConfirm(WalletTccConfirmCommand command) {
        validateXidAndBranchId(command.xid(), command.branchId());
        WalletTccBranch branch = walletAccountRepository.findBranchForUpdate(command.xid(), command.branchId())
                .orElse(null);
        if (branch == null) {
            return new WalletTccBranchDTO(command.xid(), command.branchId(), WalletTccBranchStatus.CONFIRMED.name(),
                    "empty confirm, ignored");
        }
        if (branch.getBranchStatus() == WalletTccBranchStatus.CONFIRMED) {
            return new WalletTccBranchDTO(branch.getXid(), branch.getBranchId(), branch.getBranchStatus().name(),
                    "confirm duplicated, idempotent return");
        }
        if (branch.getBranchStatus() == WalletTccBranchStatus.CANCELED) {
            return new WalletTccBranchDTO(branch.getXid(), branch.getBranchId(), branch.getBranchStatus().name(),
                    "branch already canceled, confirm ignored");
        }

        WalletAccount walletAccount = walletAccountRepository.findByUserIdAndCurrencyForUpdate(
                        branch.getUserId(),
                        branch.getAmount().getCurrencyUnit().getCode())
                .orElseThrow(() -> new NoSuchElementException("wallet account not found: " + branch.getUserId()));
        LocalDateTime now = LocalDateTime.now();
        walletAccount.confirm(branch.getOperationType(), branch.getAmount(), now);
        branch.markConfirmed(now);
        WalletFreezeRecord freezeRecord = walletAccountRepository.findFreezeRecordForUpdate(branch.getXid(), branch.getBranchId())
                .orElse(null);
        if (freezeRecord == null) {
            freezeRecord = WalletFreezeRecord.newFrozen(
                    branch.getXid(),
                    branch.getBranchId(),
                    branch.getUserId(),
                    branch.getFreezeType(),
                    branch.getOperationType(),
                    branch.getAmount(),
                    branch.getBusinessNo(),
                    now
            );
            freezeRecord.markDeducted(now);
            walletAccountRepository.createFreezeRecord(freezeRecord);
        } else if (freezeRecord.getFreezeStatus() != WalletFreezeStatus.DEDUCTED) {
            freezeRecord.markDeducted(now);
            walletAccountRepository.saveFreezeRecord(freezeRecord);
        }

        walletAccountRepository.save(walletAccount);
        walletAccountRepository.saveBranch(branch);
        return new WalletTccBranchDTO(branch.getXid(), branch.getBranchId(), branch.getBranchStatus().name(), "confirm success");
    }

    /**
     * 处理TCC信息。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WalletTccBranchDTO tccCancel(WalletTccCancelCommand command) {
        validateXidAndBranchId(command.xid(), command.branchId());
        WalletTccBranch branch = walletAccountRepository.findBranchForUpdate(command.xid(), command.branchId())
                .orElse(null);
        LocalDateTime now = LocalDateTime.now();

        if (branch == null) {
            if (command.userId() == null || command.userId() <= 0) {
                throw new IllegalArgumentException("userId must be greater than 0 for empty cancel");
            }
            TccOperationType operationType = parseOperationType(command.operationType());
            WalletFreezeType freezeType = parseFreezeType(command.freezeType());
            Money amount = normalizeAmount(command.amount());
            WalletTccBranch cancelFence = WalletTccBranch.newCancelFence(
                    command.xid(),
                    command.branchId(),
                    command.userId(),
                    operationType,
                    freezeType,
                    amount,
                    command.businessNo(),
                    now
            );
            walletAccountRepository.createBranch(cancelFence);
            return new WalletTccBranchDTO(
                    cancelFence.getXid(),
                    cancelFence.getBranchId(),
                    cancelFence.getBranchStatus().name(),
                    "empty cancel fenced"
            );
        }

        if (branch.getBranchStatus() == WalletTccBranchStatus.CANCELED) {
            return new WalletTccBranchDTO(branch.getXid(), branch.getBranchId(), branch.getBranchStatus().name(),
                    "cancel duplicated, idempotent return");
        }
        if (branch.getBranchStatus() == WalletTccBranchStatus.CONFIRMED) {
            return new WalletTccBranchDTO(branch.getXid(), branch.getBranchId(), branch.getBranchStatus().name(),
                    "branch already confirmed, cancel ignored");
        }

        WalletAccount walletAccount = walletAccountRepository.findByUserIdAndCurrencyForUpdate(
                        branch.getUserId(),
                        branch.getAmount().getCurrencyUnit().getCode())
                .orElseThrow(() -> new NoSuchElementException("wallet account not found: " + branch.getUserId()));
        walletAccount.cancel(branch.getOperationType(), branch.getAmount(), now);
        branch.markCanceled(now);
        WalletFreezeRecord freezeRecord = walletAccountRepository.findFreezeRecordForUpdate(branch.getXid(), branch.getBranchId())
                .orElse(null);
        if (freezeRecord == null) {
            freezeRecord = WalletFreezeRecord.newFrozen(
                    branch.getXid(),
                    branch.getBranchId(),
                    branch.getUserId(),
                    branch.getFreezeType(),
                    branch.getOperationType(),
                    branch.getAmount(),
                    branch.getBusinessNo(),
                    now
            );
            freezeRecord.markReleased("tcc cancel", now);
            walletAccountRepository.createFreezeRecord(freezeRecord);
        } else if (freezeRecord.getFreezeStatus() != WalletFreezeStatus.RELEASED) {
            freezeRecord.markReleased("tcc cancel", now);
            walletAccountRepository.saveFreezeRecord(freezeRecord);
        }

        walletAccountRepository.save(walletAccount);
        walletAccountRepository.saveBranch(branch);
        return new WalletTccBranchDTO(branch.getXid(), branch.getBranchId(), branch.getBranchStatus().name(), "cancel success");
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WalletFreezeOperationDTO holdFreeze(WalletFreezeHoldCommand command) {
        validateFreezeOperation(command.userId(), command.freezeNo());
        WalletFreezeType freezeType = parseFreezeType(command.freezeType());
        Money amount = normalizeAmount(command.amount());
        LocalDateTime now = LocalDateTime.now();

        WalletFreezeRecord existing = walletAccountRepository.findManualFreezeRecordForUpdate(command.userId(), command.freezeNo())
                .orElse(null);
        if (existing != null) {
            WalletAccount account = walletAccountRepository.findByUserIdAndCurrency(
                            command.userId(),
                            existing.getAmount().getCurrencyUnit().getCode())
                    .orElseThrow(() -> new NoSuchElementException("wallet account not found: " + command.userId()));
            return toFreezeOperationDTO(account, existing, "manual hold duplicated, idempotent return");
        }

        WalletAccount walletAccount = walletAccountRepository.findByUserIdAndCurrencyForUpdate(
                        command.userId(),
                        amount.getCurrencyUnit().getCode())
                .orElseThrow(() -> new NoSuchElementException("wallet account not found: " + command.userId()));
        ensureCurrencyMatched(walletAccount, amount);
        walletAccount.hold(TccOperationType.DEBIT, amount, now);

        WalletFreezeRecord freezeRecord = new WalletFreezeRecord(
                command.freezeNo(),
                command.freezeNo(),
                command.userId(),
                freezeType,
                TccOperationType.DEBIT,
                amount,
                WalletFreezeStatus.FROZEN,
                command.freezeNo(),
                normalizeReason(command.reason()),
                now,
                now
        );

        walletAccountRepository.save(walletAccount);
        walletAccountRepository.createFreezeRecord(freezeRecord);
        return toFreezeOperationDTO(walletAccount, freezeRecord, "manual hold success");
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WalletFreezeOperationDTO releaseFreeze(WalletFreezeReleaseCommand command) {
        validateFreezeOperation(command.userId(), command.freezeNo());
        LocalDateTime now = LocalDateTime.now();
        WalletFreezeRecord freezeRecord = walletAccountRepository.findManualFreezeRecordForUpdate(command.userId(), command.freezeNo())
                .orElseThrow(() -> new NoSuchElementException("manual freeze not found: " + command.freezeNo()));

        WalletAccount walletAccount = walletAccountRepository.findByUserIdAndCurrencyForUpdate(
                        command.userId(),
                        freezeRecord.getAmount().getCurrencyUnit().getCode())
                .orElseThrow(() -> new NoSuchElementException("wallet account not found: " + command.userId()));
        if (freezeRecord.getFreezeStatus() == WalletFreezeStatus.RELEASED) {
            return toFreezeOperationDTO(walletAccount, freezeRecord, "manual release duplicated, idempotent return");
        }
        if (freezeRecord.getFreezeStatus() == WalletFreezeStatus.DEDUCTED) {
            throw new IllegalStateException("freeze has already been deducted");
        }

        walletAccount.cancel(TccOperationType.DEBIT, freezeRecord.getAmount(), now);
        freezeRecord.markReleased(normalizeReason(command.reason()), now);

        walletAccountRepository.save(walletAccount);
        walletAccountRepository.saveFreezeRecord(freezeRecord);
        return toFreezeOperationDTO(walletAccount, freezeRecord, "manual release success");
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WalletFreezeOperationDTO deductFreeze(WalletFreezeDeductCommand command) {
        validateFreezeOperation(command.userId(), command.freezeNo());
        LocalDateTime now = LocalDateTime.now();
        WalletFreezeRecord freezeRecord = walletAccountRepository.findManualFreezeRecordForUpdate(command.userId(), command.freezeNo())
                .orElseThrow(() -> new NoSuchElementException("manual freeze not found: " + command.freezeNo()));

        WalletAccount walletAccount = walletAccountRepository.findByUserIdAndCurrencyForUpdate(
                        command.userId(),
                        freezeRecord.getAmount().getCurrencyUnit().getCode())
                .orElseThrow(() -> new NoSuchElementException("wallet account not found: " + command.userId()));
        if (freezeRecord.getFreezeStatus() == WalletFreezeStatus.DEDUCTED) {
            return toFreezeOperationDTO(walletAccount, freezeRecord, "manual deduct duplicated, idempotent return");
        }
        if (freezeRecord.getFreezeStatus() == WalletFreezeStatus.RELEASED) {
            throw new IllegalStateException("freeze has already been released");
        }

        walletAccount.confirm(TccOperationType.DEBIT, freezeRecord.getAmount(), now);
        freezeRecord.markDeducted(
                normalizeReason(command.reason()) == null ? "manual deduct" : normalizeReason(command.reason()),
                now
        );

        walletAccountRepository.save(walletAccount);
        walletAccountRepository.saveFreezeRecord(freezeRecord);
        return toFreezeOperationDTO(walletAccount, freezeRecord, "manual deduct success");
    }

    /**
     * 查询记录列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<WalletFreezeRecordDTO> listFreezeRecords(WalletFreezeQueryCommand command) {
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        WalletFreezeType freezeType = parseFreezeTypeNullable(command.freezeType());
        WalletFreezeStatus freezeStatus = parseFreezeStatusNullable(command.freezeStatus());
        int limit = normalizeLimit(command.limit());
        return walletAccountRepository.listFreezeRecords(
                        command.userId(),
                        normalizeOptionalCurrency(command.currencyCode()),
                        freezeType,
                        freezeStatus,
                        limit).stream()
                .map(this::toFreezeRecordDTO)
                .toList();
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional(readOnly = true)
    public WalletFreezeSummaryDTO summarizeFreezes(Long userId) {
        return summarizeFreezes(userId, "CNY");
    }

    /**
     * 处理业务数据。
     */
    @Override
    @Transactional(readOnly = true)
    public WalletFreezeSummaryDTO summarizeFreezes(Long userId, String currencyCode) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        String normalizedCurrency = normalizeCurrency(currencyCode);
        WalletAccount walletAccount = walletAccountRepository.findByUserIdAndCurrency(userId, normalizedCurrency)
                .orElseThrow(() -> new NoSuchElementException("wallet account not found: " + userId + ":" + normalizedCurrency));
        List<WalletFreezeRecord> activeRecords = walletAccountRepository.listFreezeRecords(
                userId,
                normalizedCurrency,
                null,
                WalletFreezeStatus.FROZEN,
                1000
        );
        Map<WalletFreezeType, Money> grouped = activeRecords.stream()
                .collect(Collectors.toMap(
                        WalletFreezeRecord::getFreezeType,
                        WalletFreezeRecord::getAmount,
                        Money::plus
                ));
        List<WalletFreezeSummaryItemDTO> items = grouped.entrySet().stream()
                .map(entry -> new WalletFreezeSummaryItemDTO(entry.getKey().name(), entry.getValue()))
                .sorted(Comparator.comparing(WalletFreezeSummaryItemDTO::freezeType))
                .toList();
        return new WalletFreezeSummaryDTO(userId, walletAccount.getReservedBalance(), items);
    }

    private WalletAccountDTO toWalletAccountDTO(WalletAccount walletAccount) {
        return new WalletAccountDTO(
                walletAccount.getUserId(),
                walletAccount.getCurrencyCode(),
                walletAccount.getAvailableBalance(),
                walletAccount.getReservedBalance(),
                walletAccount.getAccountStatus().name()
        );
    }

    /**
     * 并发兜底：多个请求首次同时访问余额时，只允许一条建户成功，其他请求回查已创建账户。
     */
    private WalletAccount createWalletAccountOrLoadExisting(Long userId, String currencyCode) {
        LocalDateTime now = LocalDateTime.now();
        WalletAccount createdAccount = WalletAccount.open(userId, currencyCode, now);
        try {
            return walletAccountRepository.save(createdAccount);
        } catch (DataIntegrityViolationException ex) {
            return walletAccountRepository.findByUserIdAndCurrency(userId, currencyCode)
                    .orElseThrow(() -> ex);
        }
    }

    private void validateXidAndBranchId(String xid, String branchId) {
        if (xid == null || xid.isBlank()) {
            throw new IllegalArgumentException("xid must not be blank");
        }
        if (branchId == null || branchId.isBlank()) {
            throw new IllegalArgumentException("branchId must not be blank");
        }
    }

    private TccOperationType parseOperationType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            throw new IllegalArgumentException("operationType must not be blank");
        }
        try {
            return TccOperationType.valueOf(rawType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("operationType must be DEBIT or CREDIT");
        }
    }

    private WalletFreezeType parseFreezeType(String rawType) {
        try {
            return WalletFreezeType.from(rawType);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("freezeType is invalid");
        }
    }

    private WalletFreezeType parseFreezeTypeNullable(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return null;
        }
        return parseFreezeType(rawType);
    }

    private WalletFreezeStatus parseFreezeStatusNullable(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return null;
        }
        try {
            return WalletFreezeStatus.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("freezeStatus is invalid");
        }
    }

    private int normalizeLimit(Integer rawLimit) {
        if (rawLimit == null) {
            return 200;
        }
        if (rawLimit <= 0) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }
        return Math.min(rawLimit, 1000);
    }

    private void validateFreezeOperation(Long userId, String freezeNo) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        if (freezeNo == null || freezeNo.isBlank()) {
            throw new IllegalArgumentException("freezeNo must not be blank");
        }
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        String normalized = reason.trim();
        if (normalized.length() > 128) {
            return normalized.substring(0, 128);
        }
        return normalized;
    }

    private WalletFreezeOperationDTO toFreezeOperationDTO(WalletAccount walletAccount,
                                                          WalletFreezeRecord freezeRecord,
                                                          String message) {
        return new WalletFreezeOperationDTO(
                walletAccount.getUserId(),
                freezeRecord.getBusinessNo(),
                freezeRecord.getFreezeType().name(),
                freezeRecord.getFreezeStatus().name(),
                freezeRecord.getAmount(),
                walletAccount.getAvailableBalance(),
                walletAccount.getReservedBalance(),
                message
        );
    }

    private WalletFreezeRecordDTO toFreezeRecordDTO(WalletFreezeRecord record) {
        return new WalletFreezeRecordDTO(
                record.getUserId(),
                record.getBusinessNo(),
                record.getFreezeType().name(),
                record.getOperationType().name(),
                record.getFreezeStatus().name(),
                record.getAmount(),
                record.getFreezeReason(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private String normalizeCurrency(String source) {
        if (source == null || source.isBlank()) {
            return DEFAULT_CURRENCY;
        }
        String normalized = source.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > 8) {
            throw new IllegalArgumentException("currencyCode length must be <= 8");
        }
        return normalized;
    }

    private String normalizeOptionalCurrency(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        return normalizeCurrency(source);
    }

    private Money normalizeAmount(Money source) {
        if (source == null || source.isLessThanOrEqual(Money.zero(source.getCurrencyUnit()))) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        return source.rounded(source.getCurrencyUnit().getDecimalPlaces(), RoundingMode.HALF_UP);
    }

    private void ensureCurrencyMatched(WalletAccount walletAccount, Money amount) {
        String accountCurrency = walletAccount.getCurrencyCode();
        String amountCurrency = amount.getCurrencyUnit().getCode();
        if (!accountCurrency.equalsIgnoreCase(amountCurrency)) {
            throw new IllegalArgumentException("currency mismatch");
        }
    }
}
