package cn.openaipay.application.loantrade.service.impl;

import cn.openaipay.application.creditaccount.command.CreditTccCancelCommand;
import cn.openaipay.application.creditaccount.command.CreditTccConfirmCommand;
import cn.openaipay.application.creditaccount.command.CreditTccTryCommand;
import cn.openaipay.application.creditaccount.dto.CreditAccountDTO;
import cn.openaipay.application.creditaccount.dto.CreditTccBranchDTO;
import cn.openaipay.application.creditaccount.service.CreditAccountService;
import cn.openaipay.application.loanaccount.command.LoanTccCancelCommand;
import cn.openaipay.application.loanaccount.command.LoanTccConfirmCommand;
import cn.openaipay.application.loanaccount.command.LoanTccTryCommand;
import cn.openaipay.application.loanaccount.dto.LoanTccBranchDTO;
import cn.openaipay.application.loantrade.service.LoanTradeService;
import cn.openaipay.domain.creditaccount.model.CreditAccount;
import cn.openaipay.domain.creditaccount.repository.CreditAccountRepository;
import cn.openaipay.domain.loanaccount.model.LoanAccountProfile;
import cn.openaipay.domain.loanaccount.repository.LoanAccountProfileRepository;
import cn.openaipay.domain.loantrade.model.LoanTradeOperationType;
import cn.openaipay.domain.loantrade.model.LoanTradeOrder;
import cn.openaipay.domain.loantrade.model.LoanTradeOrderStatus;
import cn.openaipay.domain.loantrade.repository.LoanTradeRepository;
import cn.openaipay.domain.riskpolicy.model.RiskCheckContext;
import cn.openaipay.domain.riskpolicy.model.RiskDecision;
import cn.openaipay.domain.riskpolicy.model.RiskSceneCode;
import cn.openaipay.domain.riskpolicy.service.RiskPolicyDomainService;
import cn.openaipay.domain.trade.model.TradeCreditOrder;
import cn.openaipay.domain.trade.model.TradeOrder;
import cn.openaipay.domain.trade.repository.TradeRepository;
import org.joda.money.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * 爱借交易应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Service
public class LoanTradeServiceImpl implements LoanTradeService {

    /** 业务费率 */
    private static final BigDecimal DEFAULT_ANNUAL_RATE = new BigDecimal("3.24");
    /** 原始费率 */
    private static final BigDecimal DEFAULT_ORIGINAL_ANNUAL_RATE = new BigDecimal("5.04");
    /** PER信息 */
    private static final BigDecimal DAYS_PER_YEAR = new BigDecimal("365");
    /** 默认信息 */
    private static final int DEFAULT_TERM_MONTHS = 24;

    /** 借款交易类型 */
    private static final String LOAN_DRAW_TRADE_TYPE = "LOAN_DRAW";
    /** 操作借款信息 */
    private static final String OPERATION_LEND = "LEND";
    /** 操作还款信息 */
    private static final String OPERATION_REPAY = "REPAY";
    /** 资源信息 */
    private static final String ASSET_CATEGORY_PRINCIPAL = "PRINCIPAL";
    /** 资源信息 */
    private static final String ASSET_CATEGORY_INTEREST = "INTEREST";
    /** 资源信息 */
    private static final String ASSET_CATEGORY_FINE = "FINE";
    /** 分支信息 */
    private static final String BRANCH_SUFFIX_INTEREST = "-I";
    /** 分支信息 */
    private static final String BRANCH_SUFFIX_PRINCIPAL = "-P";
    /** 分支信息 */
    private static final String BRANCH_SUFFIX_FINE = "-F";

    /** 信用信息 */
    private final CreditAccountService creditAccountService;
    /** 信用信息 */
    private final CreditAccountRepository creditAccountRepository;
    /** 借款交易信息 */
    private final LoanTradeRepository loanTradeRepository;
    /** 交易信息 */
    private final TradeRepository tradeRepository;
    /** 爱借档案信息 */
    private final LoanAccountProfileRepository loanAccountProfileRepository;
    /** 风控策略信息 */
    private final RiskPolicyDomainService riskPolicyDomainService;

    public LoanTradeServiceImpl(CreditAccountService creditAccountService,
                                CreditAccountRepository creditAccountRepository,
                                LoanTradeRepository loanTradeRepository,
                                TradeRepository tradeRepository,
                                LoanAccountProfileRepository loanAccountProfileRepository,
                                RiskPolicyDomainService riskPolicyDomainService) {
        this.creditAccountService = creditAccountService;
        this.creditAccountRepository = creditAccountRepository;
        this.loanTradeRepository = loanTradeRepository;
        this.tradeRepository = tradeRepository;
        this.loanAccountProfileRepository = loanAccountProfileRepository;
        this.riskPolicyDomainService = riskPolicyDomainService;
    }

    /**
     * 处理TCCTRY信息。
     */
    @Override
    @Transactional
    public LoanTccBranchDTO tccTry(LoanTccTryCommand command) {
        if (isLoanRepayPrincipal(command.operationType(), command.assetCategory())) {
            return handleLoanRepayTry(command);
        }
        if (isLoanDrawPrincipal(command.operationType(), command.assetCategory())) {
            String accountNo = normalizeRequired(command.accountNo(), "accountNo");
            CreditAccountDTO accountDTO = creditAccountService.getCreditAccount(accountNo);
            ensureRiskAllowed(
                    RiskSceneCode.LOAN_DRAW,
                    accountDTO.userId(),
                    accountNo,
                    command.amount() == null ? null : command.amount().getAmount(),
                    Map.of("operationType", OPERATION_LEND)
            );
        }
        return toLoanTccBranchDTO(creditAccountService.tccTry(new CreditTccTryCommand(
                command.xid(),
                command.branchId(),
                command.accountNo(),
                command.operationType(),
                command.assetCategory(),
                command.amount(),
                command.businessNo()
        )));
    }

    /**
     * 处理TCC信息。
     */
    @Override
    @Transactional
    public LoanTccBranchDTO tccConfirm(LoanTccConfirmCommand command) {
        LoanTradeOrder loanTradeOrder = loanTradeRepository.findByXidAndBranchIdForUpdate(command.xid(), command.branchId())
                .orElse(null);
        if (loanTradeOrder == null) {
            return toLoanTccBranchDTO(creditAccountService.tccConfirm(new CreditTccConfirmCommand(
                    command.xid(),
                    command.branchId()
            )));
        }
        if (loanTradeOrder.getStatus() == LoanTradeOrderStatus.CONFIRMED) {
            return new LoanTccBranchDTO(
                    command.xid(),
                    command.branchId(),
                    LoanTradeOrderStatus.CONFIRMED.name(),
                    "confirm duplicated, idempotent return"
            );
        }
        if (loanTradeOrder.getStatus() == LoanTradeOrderStatus.CANCELED) {
            return new LoanTccBranchDTO(
                    command.xid(),
                    command.branchId(),
                    LoanTradeOrderStatus.CANCELED.name(),
                    "branch already canceled, confirm ignored"
            );
        }

        confirmRepayChildIfNeeded(command.xid(), loanTradeOrder.getInterestBranchId());
        confirmRepayChildIfNeeded(command.xid(), loanTradeOrder.getPrincipalBranchId());
        confirmRepayChildIfNeeded(command.xid(), loanTradeOrder.getFineBranchId());

        CreditAccountDTO accountDTO = creditAccountService.getCreditAccount(loanTradeOrder.getAccountNo());
        LoanRateProfile rateProfile = resolveLoanRateProfile(loanTradeOrder.getAccountNo());
        Money monthlyPayment = calculateMonthlyPayment(
                accountDTO.principalBalance(),
                rateProfile.annualRatePercent(),
                rateProfile.remainingTermMonths()
        );
        loanTradeOrder.markConfirmed(
                rateProfile.annualRatePercent(),
                rateProfile.remainingTermMonths(),
                monthlyPayment,
                LocalDateTime.now()
        );
        loanTradeRepository.save(loanTradeOrder);
        return new LoanTccBranchDTO(
                command.xid(),
                command.branchId(),
                LoanTradeOrderStatus.CONFIRMED.name(),
                "confirm success"
        );
    }

    /**
     * 处理TCC信息。
     */
    @Override
    @Transactional
    public LoanTccBranchDTO tccCancel(LoanTccCancelCommand command) {
        LoanTradeOrder loanTradeOrder = loanTradeRepository.findByXidAndBranchIdForUpdate(command.xid(), command.branchId())
                .orElse(null);
        if (loanTradeOrder == null) {
            return toLoanTccBranchDTO(creditAccountService.tccCancel(new CreditTccCancelCommand(
                    command.xid(),
                    command.branchId(),
                    command.accountNo(),
                    command.operationType(),
                    command.assetCategory(),
                    command.amount(),
                    command.businessNo()
            )));
        }
        if (loanTradeOrder.getStatus() == LoanTradeOrderStatus.CANCELED) {
            return new LoanTccBranchDTO(
                    command.xid(),
                    command.branchId(),
                    LoanTradeOrderStatus.CANCELED.name(),
                    "cancel duplicated, idempotent return"
            );
        }
        if (loanTradeOrder.getStatus() == LoanTradeOrderStatus.CONFIRMED) {
            return new LoanTccBranchDTO(
                    command.xid(),
                    command.branchId(),
                    LoanTradeOrderStatus.CONFIRMED.name(),
                    "branch already confirmed, cancel ignored"
            );
        }

        cancelRepayChildIfNeeded(command.xid(), loanTradeOrder, ASSET_CATEGORY_FINE, loanTradeOrder.getFineBranchId(), loanTradeOrder.getFineAmount());
        cancelRepayChildIfNeeded(command.xid(), loanTradeOrder, ASSET_CATEGORY_PRINCIPAL, loanTradeOrder.getPrincipalBranchId(), loanTradeOrder.getPrincipalAmount());
        cancelRepayChildIfNeeded(command.xid(), loanTradeOrder, ASSET_CATEGORY_INTEREST, loanTradeOrder.getInterestBranchId(), loanTradeOrder.getInterestAmount());

        loanTradeOrder.markCanceled(LocalDateTime.now());
        loanTradeRepository.save(loanTradeOrder);
        return new LoanTccBranchDTO(
                command.xid(),
                command.branchId(),
                LoanTradeOrderStatus.CANCELED.name(),
                "cancel success"
        );
    }

    private LoanTccBranchDTO handleLoanRepayTry(LoanTccTryCommand command) {
        LoanTradeOrder existingLoanTradeOrder = loanTradeRepository.findByXidAndBranchIdForUpdate(command.xid(), command.branchId())
                .orElse(null);
        if (existingLoanTradeOrder != null) {
            return toIdempotentTryResult(existingLoanTradeOrder);
        }

        String accountNo = normalizeRequired(command.accountNo(), "accountNo");
        Money requestAmount = requirePositiveAmount(command.amount(), "amount");
        LoanRateProfile rateProfile = resolveLoanRateProfile(accountNo);
        ensureLoanInterestAccrued(accountNo, rateProfile);

        CreditAccountDTO accountDTO = creditAccountService.getCreditAccount(accountNo);
        ensureRiskAllowed(
                RiskSceneCode.LOAN_REPAY,
                accountDTO.userId(),
                accountNo,
                requestAmount.getAmount(),
                Map.of("operationType", OPERATION_REPAY)
        );
        LoanRepaySplit repaySplit = splitRepayAmount(
                requestAmount,
                accountDTO.interestBalance(),
                accountDTO.principalBalance(),
                accountDTO.fineBalance()
        );
        if (repaySplit.unallocatedAmount().isGreaterThan(zeroOf(repaySplit.unallocatedAmount()))) {
            throw new IllegalArgumentException("repay amount exceeds outstanding balance");
        }

        List<RepayChildBranch> preparedChildBranches = new ArrayList<>();
        String interestBranchId = buildChildBranchId(command.branchId(), BRANCH_SUFFIX_INTEREST, repaySplit.interestAmount());
        String principalBranchId = buildChildBranchId(command.branchId(), BRANCH_SUFFIX_PRINCIPAL, repaySplit.principalAmount());
        String fineBranchId = buildChildBranchId(command.branchId(), BRANCH_SUFFIX_FINE, repaySplit.fineAmount());
        try {
            tryRepayChildIfNeeded(command, accountNo, interestBranchId, ASSET_CATEGORY_INTEREST, repaySplit.interestAmount(), preparedChildBranches);
            tryRepayChildIfNeeded(command, accountNo, principalBranchId, ASSET_CATEGORY_PRINCIPAL, repaySplit.principalAmount(), preparedChildBranches);
            tryRepayChildIfNeeded(command, accountNo, fineBranchId, ASSET_CATEGORY_FINE, repaySplit.fineAmount(), preparedChildBranches);
        } catch (RuntimeException ex) {
            rollbackPreparedRepayChildren(command, accountNo, preparedChildBranches);
            throw ex;
        }

        Money principalAfterRepay = maxZero(accountDTO.principalBalance().minus(repaySplit.principalAmount()));
        Money monthlyPayment = calculateMonthlyPayment(
                principalAfterRepay,
                rateProfile.annualRatePercent(),
                rateProfile.remainingTermMonths()
        );
        LoanTradeOrder loanTradeOrder = LoanTradeOrder.newTried(
                command.xid(),
                command.branchId(),
                normalizeOptional(command.businessNo()),
                accountNo,
                LoanTradeOperationType.REPAY,
                requestAmount,
                repaySplit.interestAmount(),
                repaySplit.principalAmount(),
                repaySplit.fineAmount(),
                interestBranchId,
                principalBranchId,
                fineBranchId,
                rateProfile.annualRatePercent(),
                rateProfile.remainingTermMonths(),
                monthlyPayment,
                LocalDateTime.now()
        );
        loanTradeRepository.save(loanTradeOrder);
        return new LoanTccBranchDTO(
                command.xid(),
                command.branchId(),
                LoanTradeOrderStatus.TRIED.name(),
                "try success"
        );
    }

    private LoanTccBranchDTO toIdempotentTryResult(LoanTradeOrder loanTradeOrder) {
        String message = switch (loanTradeOrder.getStatus()) {
            case TRIED -> "try duplicated, idempotent return";
            case CONFIRMED -> "branch already confirmed, try ignored";
            case CANCELED -> "branch has been canceled, try is not allowed";
        };
        return new LoanTccBranchDTO(
                loanTradeOrder.getXid(),
                loanTradeOrder.getBranchId(),
                loanTradeOrder.getStatus().name(),
                message
        );
    }

    private void tryRepayChildIfNeeded(LoanTccTryCommand command,
                                       String accountNo,
                                       String childBranchId,
                                       String assetCategory,
                                       Money amount,
                                       List<RepayChildBranch> preparedChildBranches) {
        if (childBranchId == null || amount == null || !amount.isGreaterThan(zeroOf(amount))) {
            return;
        }
        creditAccountService.tccTry(new CreditTccTryCommand(
                command.xid(),
                childBranchId,
                accountNo,
                OPERATION_REPAY,
                assetCategory,
                amount,
                command.businessNo()
        ));
        preparedChildBranches.add(new RepayChildBranch(childBranchId, assetCategory, amount));
    }

    private void rollbackPreparedRepayChildren(LoanTccTryCommand command,
                                               String accountNo,
                                               List<RepayChildBranch> preparedChildBranches) {
        for (int index = preparedChildBranches.size() - 1; index >= 0; index--) {
            RepayChildBranch preparedChild = preparedChildBranches.get(index);
            try {
                creditAccountService.tccCancel(new CreditTccCancelCommand(
                        command.xid(),
                        preparedChild.branchId(),
                        accountNo,
                        OPERATION_REPAY,
                        preparedChild.assetCategory(),
                        preparedChild.amount(),
                        command.businessNo()
                ));
            } catch (RuntimeException ignore) {
            }
        }
    }

    private void confirmRepayChildIfNeeded(String xid, String childBranchId) {
        if (normalizeOptional(childBranchId) == null) {
            return;
        }
        creditAccountService.tccConfirm(new CreditTccConfirmCommand(xid, childBranchId));
    }

    private void cancelRepayChildIfNeeded(String xid,
                                          LoanTradeOrder loanTradeOrder,
                                          String assetCategory,
                                          String childBranchId,
                                          Money amount) {
        if (normalizeOptional(childBranchId) == null || amount == null || !amount.isGreaterThan(zeroOf(amount))) {
            return;
        }
        creditAccountService.tccCancel(new CreditTccCancelCommand(
                xid,
                childBranchId,
                loanTradeOrder.getAccountNo(),
                OPERATION_REPAY,
                assetCategory,
                amount,
                loanTradeOrder.getBusinessNo()
        ));
    }

    private String buildChildBranchId(String parentBranchId, String suffix, Money amount) {
        if (amount == null || !amount.isGreaterThan(zeroOf(amount))) {
            return null;
        }
        return normalizeRequired(parentBranchId, "branchId") + suffix;
    }

    private LoanRepaySplit splitRepayAmount(Money requestAmount,
                                            Money interestBalance,
                                            Money principalBalance,
                                            Money fineBalance) {
        Money remaining = requestAmount;
        Money principalPart = minMoney(remaining, defaultZero(principalBalance));
        remaining = remaining.minus(principalPart).rounded(2, RoundingMode.HALF_UP);

        Money interestPart = minMoney(remaining, defaultZero(interestBalance));
        remaining = remaining.minus(interestPart).rounded(2, RoundingMode.HALF_UP);

        Money finePart = minMoney(remaining, defaultZero(fineBalance));
        remaining = remaining.minus(finePart).rounded(2, RoundingMode.HALF_UP);
        return new LoanRepaySplit(interestPart, principalPart, finePart, remaining);
    }

    private void ensureLoanInterestAccrued(String accountNo, LoanRateProfile rateProfile) {
        CreditAccount creditAccount = creditAccountRepository.findByAccountNoForUpdate(accountNo)
                .orElseThrow(() -> new NoSuchElementException("credit account not found: " + accountNo));
        if (!creditAccount.getPrincipalBalance().isGreaterThan(zeroOf(creditAccount.getPrincipalBalance()))) {
            return;
        }

        BigDecimal annualRateDecimal = rateProfile.annualRatePercent()
                .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        long daysUsed = Math.max(1, ChronoUnit.DAYS.between(rateProfile.referenceDate(), LocalDate.now()));
        BigDecimal accruedInterestAmount = creditAccount.getPrincipalBalance().getAmount()
                .multiply(annualRateDecimal)
                .multiply(BigDecimal.valueOf(daysUsed))
                .divide(DAYS_PER_YEAR, 2, RoundingMode.HALF_UP);
        Money accruedInterest = Money.of(creditAccount.getPrincipalBalance().getCurrencyUnit(), accruedInterestAmount);
        Money currentInterest = creditAccount.getInterestBalance();
        if (accruedInterest.compareTo(currentInterest) <= 0) {
            return;
        }
        Money delta = accruedInterest.minus(currentInterest).rounded(2, RoundingMode.HALF_UP);
        if (delta.isGreaterThan(zeroOf(delta))) {
            creditAccount.accrueInterest(delta, LocalDateTime.now());
            creditAccountRepository.save(creditAccount);
        }
    }

    private LoanRateProfile resolveLoanRateProfile(String accountNo) {
        Optional<LoanAccountProfile> profileOptional = loanAccountProfileRepository.findByAccountNo(accountNo);
        if (profileOptional.isPresent()) {
            return toRateProfile(profileOptional.get());
        }

        Optional<TradeCreditOrder> latestDrawTradeOptional =
                tradeRepository.findLatestTradeCreditOrderByAccountAndType(accountNo, LOAN_DRAW_TRADE_TYPE);
        if (latestDrawTradeOptional.isPresent()) {
            LoanRateSeed rateSeed = buildRateSeedFromTrade(latestDrawTradeOptional.get());
            Long userId = resolveUserIdByAccountNo(accountNo);
            if (userId != null && userId > 0) {
                LoanAccountProfile profile = LoanAccountProfile.createDefault(
                        accountNo,
                        userId,
                        rateSeed.annualRatePercent(),
                        DEFAULT_ORIGINAL_ANNUAL_RATE,
                        rateSeed.totalTermMonths(),
                        rateSeed.drawDate(),
                        LocalDateTime.now()
                );
                LoanAccountProfile saved = loanAccountProfileRepository.save(profile);
                return toRateProfile(saved);
            }
            return toRateProfile(rateSeed.annualRatePercent(), rateSeed.totalTermMonths(), rateSeed.drawDate());
        }

        return new LoanRateProfile(DEFAULT_ANNUAL_RATE, DEFAULT_TERM_MONTHS, LocalDate.now());
    }

    private LoanRateSeed buildRateSeedFromTrade(TradeCreditOrder tradeCreditOrder) {
        int totalTermMonths = DEFAULT_TERM_MONTHS;
        BigDecimal annualRatePercent = DEFAULT_ANNUAL_RATE;
        LocalDate drawDate = tradeCreditOrder.getOccurredAt() == null
                ? LocalDate.now()
                : tradeCreditOrder.getOccurredAt().toLocalDate();
        Optional<TradeOrder> tradeOrderOptional = tradeRepository.findTradeOrderByTradeOrderNo(tradeCreditOrder.getTradeOrderNo());
        if (tradeOrderOptional.isPresent()) {
            String metadata = tradeOrderOptional.get().getMetadata();
            Map<String, String> metadataMap = parseMetadata(metadata);
            annualRatePercent = parseRatePercent(metadataMap.get("annualRate"));
            totalTermMonths = parseInstallmentMonths(metadataMap.get("installment"));
        }
        return new LoanRateSeed(annualRatePercent, totalTermMonths, drawDate);
    }

    private Map<String, String> parseMetadata(String metadataRaw) {
        String normalizedMetadata = normalizeOptional(metadataRaw);
        if (normalizedMetadata == null) {
            return Map.of();
        }
        Map<String, String> payload = new LinkedHashMap<>();
        String[] segments = normalizedMetadata.split(";");
        for (String segment : segments) {
            String[] kv = segment.split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            String key = normalizeOptional(kv[0]);
            String value = normalizeOptional(kv[1]);
            if (key != null && value != null) {
                payload.put(key, value);
            }
        }
        return payload;
    }

    private BigDecimal parseRatePercent(String rawRate) {
        String normalized = normalizeOptional(rawRate);
        if (normalized == null) {
            return DEFAULT_ANNUAL_RATE;
        }
        String numericText = normalized.replace("%", "").trim();
        try {
            BigDecimal parsed = new BigDecimal(numericText);
            if (parsed.compareTo(BigDecimal.ZERO) <= 0) {
                return DEFAULT_ANNUAL_RATE;
            }
            return parsed.setScale(4, RoundingMode.HALF_UP);
        } catch (RuntimeException ex) {
            return DEFAULT_ANNUAL_RATE;
        }
    }

    private int parseInstallmentMonths(String rawInstallment) {
        String normalized = normalizeOptional(rawInstallment);
        if (normalized == null) {
            return DEFAULT_TERM_MONTHS;
        }
        String digits = normalized.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return DEFAULT_TERM_MONTHS;
        }
        try {
            int parsed = Integer.parseInt(digits);
            return parsed > 0 ? parsed : DEFAULT_TERM_MONTHS;
        } catch (RuntimeException ex) {
            return DEFAULT_TERM_MONTHS;
        }
    }

    private Money calculateMonthlyPayment(Money principal, BigDecimal annualRatePercent, int remainingTermMonths) {
        Money normalizedPrincipal = defaultZero(principal);
        if (!normalizedPrincipal.isGreaterThan(zeroOf(normalizedPrincipal)) || remainingTermMonths <= 0) {
            return zeroOf(normalizedPrincipal);
        }
        BigDecimal p = normalizedPrincipal.getAmount();
        BigDecimal annualRateDecimal = annualRatePercent
                .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        BigDecimal monthlyRate = annualRateDecimal.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        BigDecimal monthlyPaymentAmount;
        if (monthlyRate.compareTo(BigDecimal.ZERO) <= 0) {
            monthlyPaymentAmount = p.divide(BigDecimal.valueOf(remainingTermMonths), 2, RoundingMode.HALF_UP);
        } else {
            BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
            BigDecimal compounded = onePlusRate.pow(remainingTermMonths);
            BigDecimal numerator = p.multiply(monthlyRate).multiply(compounded);
            BigDecimal denominator = compounded.subtract(BigDecimal.ONE);
            if (denominator.compareTo(BigDecimal.ZERO) <= 0) {
                monthlyPaymentAmount = p.divide(BigDecimal.valueOf(remainingTermMonths), 2, RoundingMode.HALF_UP);
            } else {
                monthlyPaymentAmount = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
            }
        }
        return Money.of(normalizedPrincipal.getCurrencyUnit(), monthlyPaymentAmount);
    }

    private boolean isLoanRepayPrincipal(String operationType, String assetCategory) {
        String normalizedOperationType = normalizeOptional(operationType);
        String normalizedAssetCategory = normalizeOptional(assetCategory);
        if (normalizedOperationType == null || normalizedAssetCategory == null) {
            return false;
        }
        return OPERATION_REPAY.equalsIgnoreCase(normalizedOperationType)
                && ASSET_CATEGORY_PRINCIPAL.equalsIgnoreCase(normalizedAssetCategory);
    }

    private boolean isLoanDrawPrincipal(String operationType, String assetCategory) {
        String normalizedOperationType = normalizeOptional(operationType);
        String normalizedAssetCategory = normalizeOptional(assetCategory);
        if (normalizedOperationType == null || normalizedAssetCategory == null) {
            return false;
        }
        return OPERATION_LEND.equalsIgnoreCase(normalizedOperationType)
                && ASSET_CATEGORY_PRINCIPAL.equalsIgnoreCase(normalizedAssetCategory);
    }

    private Money requirePositiveAmount(Money amount, String fieldName) {
        Money normalized = defaultZero(amount);
        if (!normalized.isGreaterThan(zeroOf(normalized))) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return normalized;
    }

    private Money defaultZero(Money amount) {
        if (amount == null) {
            return Money.zero(org.joda.money.CurrencyUnit.of("CNY"));
        }
        return amount.rounded(2, RoundingMode.HALF_UP);
    }

    private Money zeroOf(Money amount) {
        return Money.zero(defaultZero(amount).getCurrencyUnit());
    }

    private Money minMoney(Money left, Money right) {
        Money normalizedLeft = defaultZero(left);
        Money normalizedRight = defaultZero(right);
        if (!normalizedLeft.getCurrencyUnit().equals(normalizedRight.getCurrencyUnit())) {
            throw new IllegalArgumentException("money currency mismatch");
        }
        return normalizedLeft.compareTo(normalizedRight) <= 0 ? normalizedLeft : normalizedRight;
    }

    private Money maxZero(Money amount) {
        Money normalized = defaultZero(amount);
        return normalized.isLessThan(zeroOf(normalized)) ? zeroOf(normalized) : normalized;
    }

    private String normalizeRequired(String raw, String fieldName) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LoanRateProfile toRateProfile(LoanAccountProfile profile) {
        return toRateProfile(profile.getAnnualRatePercent(), profile.getTotalTermMonths(), profile.getDrawDate());
    }

    private LoanRateProfile toRateProfile(BigDecimal annualRatePercent, int totalTermMonths, LocalDate drawDate) {
        LocalDate baseDrawMonth = drawDate.withDayOfMonth(1);
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        long elapsedMonths = Math.max(0, ChronoUnit.MONTHS.between(baseDrawMonth, currentMonth));
        int remainingTerm = (int) Math.max(1, totalTermMonths - elapsedMonths);
        return new LoanRateProfile(annualRatePercent, remainingTerm, drawDate);
    }

    private Long resolveUserIdByAccountNo(String accountNo) {
        return creditAccountRepository.findByAccountNo(accountNo).map(CreditAccount::getUserId).orElse(null);
    }

    private void ensureRiskAllowed(RiskSceneCode sceneCode,
                                   Long userId,
                                   String accountNo,
                                   BigDecimal amount,
                                   Map<String, String> metadata) {
        RiskDecision decision = riskPolicyDomainService.evaluate(new RiskCheckContext(
                sceneCode,
                userId,
                normalizeOptional(accountNo),
                amount,
                metadata == null ? Map.of() : metadata
        ));
        if (!decision.passed()) {
            throw new IllegalArgumentException(decision.message());
        }
    }

    private LoanTccBranchDTO toLoanTccBranchDTO(CreditTccBranchDTO creditTccBranchDTO) {
        return new LoanTccBranchDTO(
                creditTccBranchDTO.xid(),
                creditTccBranchDTO.branchId(),
                creditTccBranchDTO.branchStatus(),
                creditTccBranchDTO.message()
        );
    }

    private record LoanRepaySplit(
            /** 利息金额 */
            Money interestAmount,
            /** 本金金额 */
            Money principalAmount,
            /** fine金额 */
            Money fineAmount,
            /** unallocated金额 */
            Money unallocatedAmount
    ) {
    }

    private record RepayChildBranch(
        /** 分支ID */
        String branchId,
        /** 资源category信息 */
        String assetCategory,
        /** 金额 */
        Money amount
    ) {
    }

    private record LoanRateProfile(
        /** annualratepercent信息 */
        BigDecimal annualRatePercent,
        /** remainingtermmonths信息 */
        int remainingTermMonths,
        /** reference日期 */
        LocalDate referenceDate
    ) {
    }

    private record LoanRateSeed(
            /** annualratepercent信息 */
            BigDecimal annualRatePercent,
            /** totalTermMonths信息 */
            int totalTermMonths,
            /** drawDate信息 */
            LocalDate drawDate
    ) {
    }
}
