package cn.openaipay.application.loanaccount.service.impl;

import cn.openaipay.application.loanaccount.command.CreateLoanAccountCommand;
import cn.openaipay.application.loanaccount.command.LoanTccCancelCommand;
import cn.openaipay.application.loanaccount.command.LoanTccConfirmCommand;
import cn.openaipay.application.loanaccount.command.LoanTccTryCommand;
import cn.openaipay.application.loanaccount.dto.LoanAccountLedgerBranchDTO;
import cn.openaipay.application.loanaccount.dto.LoanAccountLedgerDTO;
import cn.openaipay.application.loanaccount.dto.LoanAccountDTO;
import cn.openaipay.application.loanaccount.dto.LoanTccBranchDTO;
import cn.openaipay.application.loanaccount.service.LoanAccountLedgerService;
import cn.openaipay.application.loanaccount.service.LoanAccountService;
import cn.openaipay.domain.loanaccount.model.LoanAccountProfile;
import cn.openaipay.domain.loanaccount.repository.LoanAccountProfileRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import org.joda.money.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 借贷账户应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Service
public class LoanAccountServiceImpl implements LoanAccountService {

    /** 业务费率 */
    private static final BigDecimal DEFAULT_ANNUAL_RATE = new BigDecimal("3.24");
    /** 原始费率 */
    private static final BigDecimal DEFAULT_ORIGINAL_ANNUAL_RATE = new BigDecimal("5.04");
    /** 默认期限 */
    private static final int DEFAULT_TERM_MONTHS = 24;

    /** 爱借账本端口 */
    private final LoanAccountLedgerService loanAccountLedgerService;
    /** 爱借档案 */
    private final LoanAccountProfileRepository loanAccountProfileRepository;

    public LoanAccountServiceImpl(LoanAccountLedgerService loanAccountLedgerService,
                                  LoanAccountProfileRepository loanAccountProfileRepository) {
        this.loanAccountLedgerService = loanAccountLedgerService;
        this.loanAccountProfileRepository = loanAccountProfileRepository;
    }

    /**
     * 创建借款账户信息。
     */
    @Override
    @Transactional
    public String createLoanAccount(CreateLoanAccountCommand command) {
        LoanAccountLedgerDTO accountLedger = loanAccountLedgerService.createLoanAccount(
                command.userId(),
                command.accountNo(),
                command.totalLimit(),
                command.repayDayOfMonth()
        );
        ensureLoanProfile(accountLedger.accountNo(), command.userId(), LocalDate.now());
        return accountLedger.accountNo();
    }

    /**
     * 获取借款账户信息。
     */
    @Override
    @Transactional(readOnly = true, noRollbackFor = NoSuchElementException.class)
    public LoanAccountDTO getLoanAccount(String accountNo) {
        return toLoanAccountDTO(loanAccountLedgerService.getLoanAccount(accountNo));
    }

    /**
     * 按用户ID获取借款信息。
     */
    @Override
    @Transactional(readOnly = true, noRollbackFor = NoSuchElementException.class)
    public LoanAccountDTO getLoanAccountByUserId(Long userId) {
        return toLoanAccountDTO(loanAccountLedgerService.getLoanAccountByUserId(userId));
    }

    /**
     * 按用户ID获取或借款信息。
     */
    @Override
    @Transactional
    public LoanAccountDTO getOrCreateLoanAccountByUserId(Long userId, Money totalLimit, Integer repayDayOfMonth) {
        LoanAccountLedgerDTO loanAccountLedger = loanAccountLedgerService.getOrCreateLoanAccountByUserId(
                userId,
                totalLimit,
                repayDayOfMonth
        );
        ensureLoanProfile(loanAccountLedger.accountNo(), loanAccountLedger.userId(), LocalDate.now());
        return toLoanAccountDTO(loanAccountLedger);
    }

    /**
     * 处理TCCTRY信息。
     */
    @Override
    @Transactional
    public LoanTccBranchDTO tccTry(LoanTccTryCommand command) {
        LoanAccountLedgerBranchDTO branch = loanAccountLedgerService.tccTry(command);
        return toLoanTccBranchDTO(branch);
    }

    /**
     * 处理TCC信息。
     */
    @Override
    @Transactional
    public LoanTccBranchDTO tccConfirm(LoanTccConfirmCommand command) {
        LoanAccountLedgerBranchDTO branch = loanAccountLedgerService.tccConfirm(command);
        return toLoanTccBranchDTO(branch);
    }

    /**
     * 处理TCC信息。
     */
    @Override
    @Transactional
    public LoanTccBranchDTO tccCancel(LoanTccCancelCommand command) {
        LoanAccountLedgerBranchDTO branch = loanAccountLedgerService.tccCancel(command);
        return toLoanTccBranchDTO(branch);
    }

    private LoanAccountDTO toLoanAccountDTO(LoanAccountLedgerDTO loanAccountLedger) {
        LoanAccountProfile profile = loanAccountProfileRepository.findByAccountNo(loanAccountLedger.accountNo()).orElse(null);
        BigDecimal annualRate = profile == null ? DEFAULT_ANNUAL_RATE : profile.getAnnualRatePercent();
        BigDecimal originalAnnualRate = profile == null
                ? DEFAULT_ORIGINAL_ANNUAL_RATE
                : profile.getOriginalAnnualRatePercent();
        return new LoanAccountDTO(
                loanAccountLedger.accountNo(),
                loanAccountLedger.userId(),
                loanAccountLedger.totalLimit(),
                loanAccountLedger.availableLimit(),
                annualRate,
                originalAnnualRate,
                loanAccountLedger.repayDayOfMonth(),
                loanAccountLedger.accountStatus(),
                loanAccountLedger.payStatus()
        );
    }

    private void ensureLoanProfile(String accountNo, Long userId, LocalDate drawDate) {
        if (normalizeOptional(accountNo) == null || userId == null || userId <= 0) {
            return;
        }
        if (loanAccountProfileRepository.findByAccountNo(accountNo).isPresent()) {
            return;
        }
        loanAccountProfileRepository.save(
                LoanAccountProfile.createDefault(
                        accountNo,
                        userId,
                        DEFAULT_ANNUAL_RATE,
                        DEFAULT_ORIGINAL_ANNUAL_RATE,
                        DEFAULT_TERM_MONTHS,
                        drawDate == null ? LocalDate.now() : drawDate,
                        LocalDateTime.now()
                )
        );
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LoanTccBranchDTO toLoanTccBranchDTO(LoanAccountLedgerBranchDTO branch) {
        return new LoanTccBranchDTO(
                branch.xid(),
                branch.branchId(),
                branch.branchStatus(),
                branch.message()
        );
    }
}
