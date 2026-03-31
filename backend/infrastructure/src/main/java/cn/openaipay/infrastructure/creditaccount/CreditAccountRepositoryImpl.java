package cn.openaipay.infrastructure.creditaccount;

import cn.openaipay.domain.creditaccount.model.CreditAccount;
import cn.openaipay.domain.creditaccount.model.CreditAccountPayStatus;
import cn.openaipay.domain.creditaccount.model.CreditAccountType;
import cn.openaipay.domain.creditaccount.model.CreditAccountStatus;
import cn.openaipay.domain.creditaccount.model.CreditAssetCategory;
import cn.openaipay.domain.creditaccount.model.CreditRepayBillMode;
import cn.openaipay.domain.creditaccount.model.CreditTccBranch;
import cn.openaipay.domain.creditaccount.model.CreditTccBranchStatus;
import cn.openaipay.domain.creditaccount.model.CreditTccOperationType;
import cn.openaipay.domain.creditaccount.repository.CreditAccountRepository;
import cn.openaipay.infrastructure.creditaccount.dataobject.CreditAccountDO;
import cn.openaipay.infrastructure.creditaccount.dataobject.CreditTccBranchDO;
import cn.openaipay.infrastructure.creditaccount.mapper.CreditAccountMapper;
import cn.openaipay.infrastructure.creditaccount.mapper.CreditTccBranchMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.joda.money.Money;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 信用账户仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class CreditAccountRepositoryImpl implements CreditAccountRepository {

    /** CreditAccountMapper组件 */
    private final CreditAccountMapper creditAccountMapper;
    /** CreditTcc分支Persistence组件 */
    private final CreditTccBranchMapper creditTccBranchMapper;

    public CreditAccountRepositoryImpl(CreditAccountMapper creditAccountMapper,
                                       CreditTccBranchMapper creditTccBranchMapper) {
        this.creditAccountMapper = creditAccountMapper;
        this.creditTccBranchMapper = creditTccBranchMapper;
    }

    /**
     * 按单号查找记录。
     */
    @Override
    public Optional<CreditAccount> findByAccountNo(String accountNo) {
        return creditAccountMapper.findByAccountNo(accountNo).map(this::toDomainAccount);
    }

    /**
     * 按单号查找记录并加锁。
     */
    @Override
    public Optional<CreditAccount> findByAccountNoForUpdate(String accountNo) {
        return creditAccountMapper.findByAccountNoForUpdate(accountNo).map(this::toDomainAccount);
    }

    /**
     * 按用户ID查找记录。
     */
    @Override
    public Optional<CreditAccount> findByUserId(Long userId) {
        return findByUserIdAndType(userId, CreditAccountType.AICREDIT);
    }

    /**
     * 按用户ID查找记录。
     */
    @Override
    public Optional<CreditAccount> findByUserIdAndType(Long userId, CreditAccountType accountType) {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType must not be null");
        }
        return creditAccountMapper
                .findByUserIdAndAccountNoPrefix(userId, accountType.accountNoPrefix())
                .map(this::toDomainAccount);
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public CreditAccount save(CreditAccount creditAccount) {
        validateAccountBalances(creditAccount);
        CreditAccountDO entity = creditAccountMapper.findByAccountNo(creditAccount.getAccountNo())
                .orElse(new CreditAccountDO());
        fillAccountDO(entity, creditAccount);
        CreditAccountDO savedDO = creditAccountMapper.save(entity);
        return toDomainAccount(savedDO);
    }

    /**
     * 查找分支信息。
     */
    @Override
    public Optional<CreditTccBranch> findBranch(String xid, String branchId) {
        return creditTccBranchMapper.findByXidAndBranchId(xid, branchId).map(this::toDomainBranch);
    }

    /**
     * 查找用于更新信息。
     */
    @Override
    public Optional<CreditTccBranch> findBranchForUpdate(String xid, String branchId) {
        return creditTccBranchMapper.findByXidAndBranchIdForUpdate(xid, branchId).map(this::toDomainBranch);
    }

    /**
     * 保存分支信息。
     */
    @Override
    @Transactional
    public CreditTccBranch saveBranch(CreditTccBranch branch) {
        CreditTccBranchDO entity = creditTccBranchMapper.findByXidAndBranchId(branch.getXid(), branch.getBranchId())
                .orElse(new CreditTccBranchDO());
        fillBranchDO(entity, branch);
        CreditTccBranchDO savedDO = creditTccBranchMapper.save(entity);
        return toDomainBranch(savedDO);
    }

    /**
     * 查找业务数据。
     */
    @Override
    public List<CreditTccBranch> findConfirmedPrincipalLendBranches(
            String accountNo,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    ) {
        return creditTccBranchMapper.findConfirmedPrincipalLendBranches(accountNo, startInclusive, endExclusive)
                .stream()
                .map(this::toDomainBranch)
                .toList();
    }

    /**
     * 查找业务数据。
     */
    @Override
    public List<CreditTccBranch> findConfirmedPrincipalRepayBranches(
            String accountNo,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive,
            CreditRepayBillMode repayBillMode
    ) {
        List<CreditTccBranchDO> branchDOs = repayBillMode == CreditRepayBillMode.NEXT
                ? creditTccBranchMapper.findConfirmedPrincipalRepayBranchesForNextBill(accountNo, startInclusive, endExclusive)
                : creditTccBranchMapper.findConfirmedPrincipalRepayBranchesForCurrentBill(accountNo, startInclusive, endExclusive);
        return branchDOs.stream()
                .map(this::toDomainBranch)
                .toList();
    }

    private CreditAccount toDomainAccount(CreditAccountDO entity) {
        return new CreditAccount(
                entity.getAccountNo(),
                entity.getUserId(),
                entity.getTotalLimit(),
                entity.getPrincipalBalance(),
                entity.getPrincipalUnreachAmount(),
                entity.getOverduePrincipalBalance(),
                entity.getOverduePrincipalUnreachAmount(),
                entity.getInterestBalance(),
                entity.getFineBalance(),
                CreditAccountStatus.valueOf(entity.getAccountStatus()),
                CreditAccountPayStatus.valueOf(entity.getPayStatus()),
                entity.getRepayDayOfMonth() == null ? 10 : entity.getRepayDayOfMonth(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private CreditTccBranch toDomainBranch(CreditTccBranchDO entity) {
        return new CreditTccBranch(
                entity.getXid(),
                entity.getBranchId(),
                entity.getAccountNo(),
                CreditTccOperationType.valueOf(entity.getOperationType()),
                CreditAssetCategory.valueOf(entity.getAssetCategory()),
                entity.getAmount(),
                CreditTccBranchStatus.valueOf(entity.getBranchStatus()),
                entity.getBusinessNo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillAccountDO(CreditAccountDO entity, CreditAccount account) {
        LocalDateTime now = LocalDateTime.now();
        entity.setAccountNo(account.getAccountNo());
        entity.setUserId(account.getUserId());
        entity.setTotalLimit(account.getTotalLimit());
        entity.setPrincipalBalance(account.getPrincipalBalance());
        entity.setPrincipalUnreachAmount(account.getPrincipalUnreachAmount());
        entity.setOverduePrincipalBalance(account.getOverduePrincipalBalance());
        entity.setOverduePrincipalUnreachAmount(account.getOverduePrincipalUnreachAmount());
        entity.setInterestBalance(account.getInterestBalance());
        entity.setFineBalance(account.getFineBalance());
        entity.setAccountStatus(account.getAccountStatus().name());
        entity.setPayStatus(account.getPayStatus().name());
        entity.setRepayDayOfMonth(account.getRepayDayOfMonth());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(account.getCreatedAt() == null ? now : account.getCreatedAt());
        }
        entity.setUpdatedAt(account.getUpdatedAt() == null ? now : account.getUpdatedAt());
    }

    private void fillBranchDO(CreditTccBranchDO entity, CreditTccBranch branch) {
        LocalDateTime now = LocalDateTime.now();
        entity.setXid(branch.getXid());
        entity.setBranchId(branch.getBranchId());
        entity.setAccountNo(branch.getAccountNo());
        entity.setOperationType(branch.getOperationType().name());
        entity.setAssetCategory(branch.getAssetCategory().name());
        entity.setBranchStatus(branch.getBranchStatus().name());
        entity.setAmount(branch.getAmount());
        entity.setBusinessNo(branch.getBusinessNo());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(branch.getCreatedAt() == null ? now : branch.getCreatedAt());
        }
        entity.setUpdatedAt(branch.getUpdatedAt() == null ? now : branch.getUpdatedAt());
    }

    private void validateAccountBalances(CreditAccount account) {
        requireNonNegative(account.getTotalLimit(), "totalLimit");
        requireNonNegative(account.getPrincipalBalance(), "principalBalance");
        requireNonNegative(account.getPrincipalUnreachAmount(), "principalUnreachAmount");
        requireNonNegative(account.getOverduePrincipalBalance(), "overduePrincipalBalance");
        requireNonNegative(account.getOverduePrincipalUnreachAmount(), "overduePrincipalUnreachAmount");
        requireNonNegative(account.getInterestBalance(), "interestBalance");
        requireNonNegative(account.getFineBalance(), "fineBalance");
    }

    private void requireNonNegative(Money amount, String fieldName) {
        if (amount == null) {
            return;
        }
        if (amount.compareTo(Money.zero(amount.getCurrencyUnit())) < 0) {
            throw new IllegalStateException(fieldName + " must not be less than 0");
        }
    }
}
