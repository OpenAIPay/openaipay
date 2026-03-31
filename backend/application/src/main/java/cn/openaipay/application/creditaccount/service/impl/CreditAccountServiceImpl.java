package cn.openaipay.application.creditaccount.service.impl;

import cn.openaipay.application.creditaccount.command.CreateCreditAccountCommand;
import cn.openaipay.application.creditaccount.command.CreditTccCancelCommand;
import cn.openaipay.application.creditaccount.command.CreditTccConfirmCommand;
import cn.openaipay.application.creditaccount.command.CreditTccTryCommand;
import cn.openaipay.application.creditaccount.dto.CreditAccountDTO;
import cn.openaipay.application.creditaccount.dto.CreditCurrentBillDetailDTO;
import cn.openaipay.application.creditaccount.dto.CreditCurrentBillDetailItemDTO;
import cn.openaipay.application.creditaccount.dto.CreditTccBranchDTO;
import cn.openaipay.application.creditaccount.service.CreditAccountService;
import cn.openaipay.domain.creditaccount.model.CreditAccount;
import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import cn.openaipay.domain.creditaccount.model.CreditAssetCategory;
import cn.openaipay.domain.creditaccount.model.CreditRepayBillMode;
import cn.openaipay.domain.creditaccount.model.CreditTccBranch;
import cn.openaipay.domain.creditaccount.model.CreditTccBranchStatus;
import cn.openaipay.domain.creditaccount.model.CreditTccOperationType;
import cn.openaipay.domain.creditaccount.repository.CreditAccountRepository;
import cn.openaipay.domain.creditaccount.service.CreditBillCurrentWindow;
import cn.openaipay.domain.creditaccount.service.CreditBillDetailItem;
import cn.openaipay.domain.creditaccount.service.CreditBillDomainService;
import cn.openaipay.domain.creditaccount.service.CreditBillSummary;
import cn.openaipay.domain.creditaccount.service.CreditBillUpcomingWindow;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 信用账户应用服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class CreditAccountServiceImpl implements CreditAccountService {

    /** 爱花默认总额度。 */
    private static final Money DEFAULT_AICREDIT_TOTAL_LIMIT = Money.of(CurrencyUnit.of("CNY"), new BigDecimal("660000.00"), RoundingMode.HALF_UP);
    /** 爱借默认总额度。 */
    private static final Money DEFAULT_AILOAN_TOTAL_LIMIT = Money.of(CurrencyUnit.of("CNY"), new BigDecimal("880000.00"), RoundingMode.HALF_UP);
    /** 默认每月还款日 */
    private static final int DEFAULT_REPAY_DAY_OF_MONTH = 10;
    /** CreditAccountRepository组件 */
    private final CreditAccountRepository creditAccountRepository;
    /** 信用域信息 */
    private final CreditBillDomainService creditBillDomainService;

    public CreditAccountServiceImpl(CreditAccountRepository creditAccountRepository,
                                               CreditBillDomainService creditBillDomainService) {
        this.creditAccountRepository = creditAccountRepository;
        this.creditBillDomainService = creditBillDomainService;
    }

    /**
     * 创建信用信息。
     */
    @Override
    @Transactional
    public String createCreditAccount(CreateCreditAccountCommand command) {
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        String accountNo = normalizeAccountNo(command.accountNo());
        CreditAccountType accountType = CreditAccountType.fromAccountNo(accountNo);
        if (creditAccountRepository.findByAccountNo(accountNo).isPresent()) {
            throw new IllegalArgumentException("credit account already exists: " + accountNo);
        }
        if (creditAccountRepository.findByUserIdAndType(command.userId(), accountType).isPresent()) {
            throw new IllegalArgumentException(
                    "credit account already exists for userId=" + command.userId() + ", type=" + accountType.name()
            );
        }

        LocalDateTime now = LocalDateTime.now();
        CreditAccount creditAccount = CreditAccount.open(
                accountNo,
                command.userId(),
                resolveTotalLimit(command.totalLimit(), accountType),
                normalizeRepayDayOfMonth(command.repayDayOfMonth()),
                now
        );
        creditAccountRepository.save(creditAccount);
        return creditAccount.getAccountNo();
    }

    /**
     * 获取信用信息。
     */
    @Override
    @Transactional(readOnly = true, noRollbackFor = NoSuchElementException.class)
    public CreditAccountDTO getCreditAccount(String accountNo) {
        CreditAccount creditAccount = creditAccountRepository.findByAccountNo(normalizeAccountNo(accountNo))
                .orElseThrow(() -> new NoSuchElementException("credit account not found: " + accountNo));
        return toCreditAccountDTO(creditAccount);
    }

    /**
     * 按用户ID获取信用信息。
     */
    @Override
    @Transactional
    public CreditAccountDTO getCreditAccountByUserId(Long userId) {
        return toCreditAccountDTO(resolveAiCreditAccountByUserIdOrCreate(userId));
    }

    /**
     * 按用户ID获取信用信息。
     */
    @Override
    @Transactional(readOnly = true, noRollbackFor = NoSuchElementException.class)
    public CreditAccountDTO getCreditAccountByUserId(Long userId, CreditAccountType accountType) {
        validateUserId(userId);
        CreditAccountType normalizedAccountType = requireAccountType(accountType);
        CreditAccount creditAccount = creditAccountRepository.findByUserIdAndType(userId, normalizedAccountType)
                .orElseThrow(() -> new NoSuchElementException(
                        "credit account not found for userId=" + userId + ", type=" + normalizedAccountType.name()
                ));
        return toCreditAccountDTO(creditAccount);
    }

    /**
     * 按用户ID获取当前明细信息。
     */
    @Override
    @Transactional
    public CreditCurrentBillDetailDTO getCurrentBillDetailByUserId(Long userId) {
        CreditAccount creditAccount = resolveAiCreditAccountByUserIdOrCreate(userId);

        CreditBillCurrentWindow currentBillWindow = creditBillDomainService.resolveCurrentBillWindow(
                LocalDate.now(),
                creditAccount.getRepayDayOfMonth()
        );
        List<CreditTccBranch> statementConsumptionBranches = creditAccountRepository.findConfirmedPrincipalLendBranches(
                creditAccount.getAccountNo(),
                currentBillWindow.cycleStartInclusive(),
                currentBillWindow.cycleEndExclusive()
        );
        List<CreditTccBranch> unbilledConsumptionBranches = currentBillWindow.unbilledStartInclusive().isBefore(currentBillWindow.unbilledEndExclusive())
                ? creditAccountRepository.findConfirmedPrincipalLendBranches(
                creditAccount.getAccountNo(),
                currentBillWindow.unbilledStartInclusive(),
                currentBillWindow.unbilledEndExclusive()
        )
                : List.of();
        List<CreditTccBranch> repaidBranches = creditAccountRepository.findConfirmedPrincipalRepayBranches(
                creditAccount.getAccountNo(),
                currentBillWindow.statementDate().atStartOfDay(),
                currentBillWindow.unbilledEndExclusive(),
                CreditRepayBillMode.CURRENT
        );
        CreditBillSummary billSummary = creditBillDomainService.summarizeCurrentBill(
                currentBillWindow,
                creditAccount,
                statementConsumptionBranches,
                unbilledConsumptionBranches,
                repaidBranches
        );
        return toCurrentBillDetailDTO(billSummary);
    }

    /**
     * 按用户ID获取明细信息。
     */
    @Override
    @Transactional
    public CreditCurrentBillDetailDTO getNextBillDetailByUserId(Long userId) {
        CreditAccount creditAccount = resolveAiCreditAccountByUserIdOrCreate(userId);

        CreditBillUpcomingWindow nextBillWindow = creditBillDomainService.resolveUpcomingBillWindow(
                LocalDate.now(),
                creditAccount.getRepayDayOfMonth()
        );
        List<CreditTccBranch> statementConsumptionBranches = creditAccountRepository.findConfirmedPrincipalLendBranches(
                creditAccount.getAccountNo(),
                nextBillWindow.cycleStartInclusive(),
                nextBillWindow.cycleEndExclusive()
        );
        LocalDateTime repaymentQueryEndExclusive = LocalDateTime.now().plusSeconds(1);
        List<CreditTccBranch> repaidBranches = creditAccountRepository.findConfirmedPrincipalRepayBranches(
                creditAccount.getAccountNo(),
                nextBillWindow.cycleStartInclusive(),
                repaymentQueryEndExclusive,
                CreditRepayBillMode.NEXT
        );
        CreditBillSummary billSummary = creditBillDomainService.summarizeUpcomingBill(
                nextBillWindow,
                creditAccount,
                statementConsumptionBranches,
                repaidBranches
        );
        return toCurrentBillDetailDTO(billSummary);
    }

    /**
     * 按用户ID获取或信用信息。
     */
    @Override
    @Transactional
    public CreditAccountDTO getOrCreateCreditAccountByUserId(Long userId,
                                                             CreditAccountType accountType,
                                                             Money totalLimit,
                                                             Integer repayDayOfMonth) {
        validateUserId(userId);
        CreditAccountType normalizedAccountType = requireAccountType(accountType);
        Money resolvedTotalLimit = resolveTotalLimit(totalLimit, normalizedAccountType);
        return creditAccountRepository.findByUserIdAndType(userId, normalizedAccountType)
                .map(existingAccount -> ensureAtLeastTotalLimit(existingAccount, resolvedTotalLimit))
                .map(this::toCreditAccountDTO)
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    CreditAccount createdAccount = CreditAccount.open(
                            normalizedAccountType.defaultAccountNo(userId),
                            userId,
                            resolvedTotalLimit,
                            normalizeRepayDayOfMonth(repayDayOfMonth),
                            now
                    );
                    return toCreditAccountDTO(creditAccountRepository.save(createdAccount));
                });
    }

    /**
     * 处理TCCTRY信息。
     */
    @Override
    @Transactional
    public CreditTccBranchDTO tccTry(CreditTccTryCommand command) {
        validateXidAndBranchId(command.xid(), command.branchId());
        String accountNo = normalizeAccountNo(command.accountNo());
        CreditTccOperationType operationType = parseOperationType(command.operationType());
        CreditAssetCategory assetCategory = parseAssetCategory(command.assetCategory());
        Money amount = normalizeAmount(command.amount());
        LocalDateTime now = LocalDateTime.now();

        CreditTccBranch existingBranch = creditAccountRepository.findBranchForUpdate(command.xid(), command.branchId())
                .orElse(null);
        if (existingBranch != null) {
            if (existingBranch.getBranchStatus() == CreditTccBranchStatus.CANCELED) {
                throw new IllegalStateException("branch has been canceled, try is not allowed");
            }
            return new CreditTccBranchDTO(
                    existingBranch.getXid(),
                    existingBranch.getBranchId(),
                    existingBranch.getBranchStatus().name(),
                    "try duplicated, idempotent return"
            );
        }

        CreditAccount creditAccount = creditAccountRepository.findByAccountNoForUpdate(accountNo)
                .orElseThrow(() -> new NoSuchElementException("credit account not found: " + accountNo));
        creditAccount.hold(operationType, assetCategory, amount, now);
        creditAccountRepository.save(creditAccount);

        CreditTccBranch branch = CreditTccBranch.newTry(
                command.xid(),
                command.branchId(),
                accountNo,
                operationType,
                assetCategory,
                amount,
                command.businessNo(),
                now
        );
        creditAccountRepository.saveBranch(branch);
        return new CreditTccBranchDTO(branch.getXid(), branch.getBranchId(), branch.getBranchStatus().name(), "try success");
    }

    /**
     * 处理TCC信息。
     */
    @Override
    @Transactional
    public CreditTccBranchDTO tccConfirm(CreditTccConfirmCommand command) {
        validateXidAndBranchId(command.xid(), command.branchId());
        CreditTccBranch branch = creditAccountRepository.findBranchForUpdate(command.xid(), command.branchId())
                .orElse(null);
        if (branch == null) {
            return new CreditTccBranchDTO(command.xid(), command.branchId(), CreditTccBranchStatus.CONFIRMED.name(),
                    "empty confirm, ignored");
        }
        if (branch.getBranchStatus() == CreditTccBranchStatus.CONFIRMED) {
            return new CreditTccBranchDTO(branch.getXid(), branch.getBranchId(), branch.getBranchStatus().name(),
                    "confirm duplicated, idempotent return");
        }
        if (branch.getBranchStatus() == CreditTccBranchStatus.CANCELED) {
            return new CreditTccBranchDTO(branch.getXid(), branch.getBranchId(), branch.getBranchStatus().name(),
                    "branch already canceled, confirm ignored");
        }

        CreditAccount creditAccount = creditAccountRepository.findByAccountNoForUpdate(branch.getAccountNo())
                .orElseThrow(() -> new NoSuchElementException("credit account not found: " + branch.getAccountNo()));
        LocalDateTime now = LocalDateTime.now();
        creditAccount.confirm(branch.getOperationType(), branch.getAssetCategory(), branch.getAmount(), now);
        branch.markConfirmed(now);

        creditAccountRepository.save(creditAccount);
        creditAccountRepository.saveBranch(branch);
        return new CreditTccBranchDTO(branch.getXid(), branch.getBranchId(), branch.getBranchStatus().name(), "confirm success");
    }

    /**
     * 处理TCC信息。
     */
    @Override
    @Transactional
    public CreditTccBranchDTO tccCancel(CreditTccCancelCommand command) {
        validateXidAndBranchId(command.xid(), command.branchId());
        CreditTccBranch branch = creditAccountRepository.findBranchForUpdate(command.xid(), command.branchId())
                .orElse(null);
        LocalDateTime now = LocalDateTime.now();

        if (branch == null) {
            String accountNo = normalizeAccountNo(command.accountNo());
            CreditTccOperationType operationType = parseOperationType(command.operationType());
            CreditAssetCategory assetCategory = parseAssetCategory(command.assetCategory());
            Money amount = normalizeAmount(command.amount());
            CreditTccBranch cancelFence = CreditTccBranch.newCancelFence(
                    command.xid(),
                    command.branchId(),
                    accountNo,
                    operationType,
                    assetCategory,
                    amount,
                    command.businessNo(),
                    now
            );
            creditAccountRepository.saveBranch(cancelFence);
            return new CreditTccBranchDTO(
                    cancelFence.getXid(),
                    cancelFence.getBranchId(),
                    cancelFence.getBranchStatus().name(),
                    "empty cancel fenced"
            );
        }

        if (branch.getBranchStatus() == CreditTccBranchStatus.CANCELED) {
            return new CreditTccBranchDTO(branch.getXid(), branch.getBranchId(), branch.getBranchStatus().name(),
                    "cancel duplicated, idempotent return");
        }
        if (branch.getBranchStatus() == CreditTccBranchStatus.CONFIRMED) {
            return new CreditTccBranchDTO(branch.getXid(), branch.getBranchId(), branch.getBranchStatus().name(),
                    "branch already confirmed, cancel ignored");
        }

        CreditAccount creditAccount = creditAccountRepository.findByAccountNoForUpdate(branch.getAccountNo())
                .orElseThrow(() -> new NoSuchElementException("credit account not found: " + branch.getAccountNo()));
        creditAccount.cancel(branch.getOperationType(), branch.getAssetCategory(), branch.getAmount(), now);
        branch.markCanceled(now);

        creditAccountRepository.save(creditAccount);
        creditAccountRepository.saveBranch(branch);
        return new CreditTccBranchDTO(branch.getXid(), branch.getBranchId(), branch.getBranchStatus().name(), "cancel success");
    }

    private CreditAccountDTO toCreditAccountDTO(CreditAccount creditAccount) {
        return new CreditAccountDTO(
                creditAccount.getAccountNo(),
                creditAccount.getUserId(),
                creditAccount.getTotalLimit(),
                creditAccount.calculateAvailableLimit(),
                creditAccount.getPrincipalBalance(),
                creditAccount.getOverduePrincipalBalance(),
                creditAccount.getInterestBalance(),
                creditAccount.getFineBalance(),
                resolveNextMonthBillAccumulatedAmount(creditAccount),
                creditAccount.getRepayDayOfMonth(),
                creditAccount.getAccountStatus().name(),
                creditAccount.getPayStatus().name()
        );
    }

    private CreditCurrentBillDetailDTO toCurrentBillDetailDTO(CreditBillSummary summary) {
        return new CreditCurrentBillDetailDTO(
                summary.title(),
                summary.periodText(),
                summary.dueAmount(),
                summary.statementTotalAmount(),
                summary.refundedAmount(),
                summary.repaidAmount(),
                summary.items().stream().map(this::toCurrentBillDetailItemDTO).toList()
        );
    }

    private CreditCurrentBillDetailItemDTO toCurrentBillDetailItemDTO(CreditBillDetailItem item) {
        return new CreditCurrentBillDetailItemDTO(
                item.dateText(),
                item.displayTitle(),
                item.displaySubtitle(),
                item.amount(),
                item.businessNo()
        );
    }

    /**
     * 解析爱花默认信用账户，不存在时为当前登录用户自动补齐一户空账单信用账户。
     *
     * 业务场景：移动端首次打开爱花首页、总计账单页或还款页时，
     * 某些演示用户可能还没有预置爱花账户；这里直接补齐默认账户，避免页面暴露底层英文异常。
     */
    private CreditAccount resolveAiCreditAccountByUserIdOrCreate(Long userId) {
        validateUserId(userId);
        return creditAccountRepository.findByUserIdAndType(userId, CreditAccountType.AICREDIT)
                .orElseGet(() -> createAiCreditAccountOrLoadExisting(userId));
    }

    /**
     * 并发兜底：多个请求首次同时访问爱花时，只允许一条建户成功，其他请求回查已创建账户。
     */
    private CreditAccount createAiCreditAccountOrLoadExisting(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        CreditAccount createdAccount = CreditAccount.open(
                CreditAccountType.AICREDIT.defaultAccountNo(userId),
                userId,
                resolveTotalLimit(null, CreditAccountType.AICREDIT),
                normalizeRepayDayOfMonth(DEFAULT_REPAY_DAY_OF_MONTH),
                now
        );
        try {
            return creditAccountRepository.save(createdAccount);
        } catch (DataIntegrityViolationException ex) {
            return creditAccountRepository.findByUserIdAndType(userId, CreditAccountType.AICREDIT)
                    .orElseThrow(() -> ex);
        }
    }

    private Money resolveNextMonthBillAccumulatedAmount(CreditAccount creditAccount) {
        if (CreditAccountType.fromAccountNo(creditAccount.getAccountNo()) != CreditAccountType.AICREDIT) {
            return zeroMoney();
        }
        LocalDate currentDate = LocalDate.now();
        LocalDateTime currentMonthStart = currentDate.withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = currentDate.withDayOfMonth(1).plusMonths(1).atStartOfDay();
        List<CreditTccBranch> currentMonthConsumptionBranches = creditAccountRepository.findConfirmedPrincipalLendBranches(
                creditAccount.getAccountNo(),
                currentMonthStart,
                nextMonthStart
        );
        List<CreditTccBranch> currentMonthRepaidBranches = creditAccountRepository.findConfirmedPrincipalRepayBranches(
                creditAccount.getAccountNo(),
                currentMonthStart,
                LocalDateTime.now().plusSeconds(1),
                CreditRepayBillMode.NEXT
        );
        return creditBillDomainService.calculateNextMonthAccumulatedAmount(
                creditAccount,
                currentMonthConsumptionBranches,
                currentMonthRepaidBranches
        );
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
    }

    private CreditAccountType requireAccountType(CreditAccountType accountType) {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType must not be null");
        }
        return accountType;
    }

    private void validateXidAndBranchId(String xid, String branchId) {
        if (xid == null || xid.isBlank()) {
            throw new IllegalArgumentException("xid must not be blank");
        }
        if (branchId == null || branchId.isBlank()) {
            throw new IllegalArgumentException("branchId must not be blank");
        }
    }

    private CreditTccOperationType parseOperationType(String rawType) {
        return CreditTccOperationType.from(rawType);
    }

    private CreditAssetCategory parseAssetCategory(String rawCategory) {
        return CreditAssetCategory.from(rawCategory);
    }

    private String normalizeAccountNo(String accountNo) {
        if (accountNo == null || accountNo.isBlank()) {
            throw new IllegalArgumentException("accountNo must not be blank");
        }
        String normalized = accountNo.trim();
        if (normalized.length() > 32) {
            throw new IllegalArgumentException("accountNo length must be <= 32");
        }
        return normalized;
    }

    private Money resolveTotalLimit(Money totalLimit, CreditAccountType accountType) {
        Money resolvedLimit = totalLimit == null ? defaultTotalLimit(accountType) : totalLimit;
        if (resolvedLimit.compareTo(zeroMoney()) <= 0) {
            throw new IllegalArgumentException("totalLimit must be greater than 0");
        }
        return resolvedLimit.rounded(2, RoundingMode.HALF_UP);
    }

    private Money defaultTotalLimit(CreditAccountType accountType) {
        return switch (accountType) {
            case AICREDIT -> DEFAULT_AICREDIT_TOTAL_LIMIT;
            case LOAN_ACCOUNT -> DEFAULT_AILOAN_TOTAL_LIMIT;
        };
    }

    private CreditAccount ensureAtLeastTotalLimit(CreditAccount creditAccount, Money targetLimit) {
        if (creditAccount.getTotalLimit().compareTo(targetLimit) >= 0) {
            return creditAccount;
        }
        creditAccount.updateTotalLimit(targetLimit, LocalDateTime.now());
        return creditAccountRepository.save(creditAccount);
    }

    private Money normalizeAmount(Money source) {
        if (source == null || source.compareTo(zeroMoney()) <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        return source.rounded(2, RoundingMode.HALF_UP);
    }

    private int normalizeRepayDayOfMonth(Integer source) {
        if (source == null) {
            return DEFAULT_REPAY_DAY_OF_MONTH;
        }
        if (source < 1 || source > 28) {
            throw new IllegalArgumentException("repayDayOfMonth must be between 1 and 28");
        }
        return source;
    }

    private Money zeroMoney() {
        return Money.zero(CurrencyUnit.of("CNY"));
    }
}
