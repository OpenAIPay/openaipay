package cn.openaipay.infrastructure.walletaccount;

import cn.openaipay.domain.walletaccount.model.TccOperationType;
import cn.openaipay.domain.walletaccount.model.WalletAccount;
import cn.openaipay.domain.walletaccount.model.WalletAccountStatus;
import cn.openaipay.domain.walletaccount.model.WalletFreezeRecord;
import cn.openaipay.domain.walletaccount.model.WalletFreezeStatus;
import cn.openaipay.domain.walletaccount.model.WalletFreezeType;
import cn.openaipay.domain.walletaccount.model.WalletTccBranch;
import cn.openaipay.domain.walletaccount.model.WalletTccBranchStatus;
import cn.openaipay.domain.walletaccount.repository.WalletAccountRepository;
import cn.openaipay.infrastructure.walletaccount.dataobject.WalletAccountDO;
import cn.openaipay.infrastructure.walletaccount.dataobject.WalletFreezeRecordDO;
import cn.openaipay.infrastructure.walletaccount.dataobject.WalletTccBranchDO;
import cn.openaipay.infrastructure.walletaccount.mapper.WalletAccountMapper;
import cn.openaipay.infrastructure.walletaccount.mapper.WalletFreezeRecordMapper;
import cn.openaipay.infrastructure.walletaccount.mapper.WalletTccBranchMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 钱包账户仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class WalletAccountRepositoryImpl implements WalletAccountRepository {

    /** WalletAccountMapper组件 */
    private final WalletAccountMapper walletAccountMapper;
    /** WalletTcc分支Persistence组件 */
    private final WalletTccBranchMapper walletTccBranchMapper;
    /** Wallet冻结明细Persistence组件 */
    private final WalletFreezeRecordMapper walletFreezeRecordMapper;

    public WalletAccountRepositoryImpl(WalletAccountMapper walletAccountMapper,
                                       WalletTccBranchMapper walletTccBranchMapper,
                                       WalletFreezeRecordMapper walletFreezeRecordMapper) {
        this.walletAccountMapper = walletAccountMapper;
        this.walletTccBranchMapper = walletTccBranchMapper;
        this.walletFreezeRecordMapper = walletFreezeRecordMapper;
    }

    /**
     * 按用户ID查找记录。
     */
    @Override
    public Optional<WalletAccount> findByUserId(Long userId) {
        return walletAccountMapper.findByUserId(userId).map(this::toDomainAccount);
    }

    /**
     * 按用户ID与币种查找记录。
     */
    @Override
    public Optional<WalletAccount> findByUserIdAndCurrency(Long userId, String currencyCode) {
        return walletAccountMapper.findByUserIdAndCurrency(userId, normalizeCurrencyCode(currencyCode))
                .map(this::toDomainAccount);
    }

    /**
     * 按用户ID查找记录并加锁。
     */
    @Override
    public Optional<WalletAccount> findByUserIdForUpdate(Long userId) {
        return walletAccountMapper.findByUserIdForUpdate(userId).map(this::toDomainAccount);
    }

    /**
     * 按用户ID与币种查找记录并加锁。
     */
    @Override
    public Optional<WalletAccount> findByUserIdAndCurrencyForUpdate(Long userId, String currencyCode) {
        return walletAccountMapper.findByUserIdAndCurrencyForUpdate(userId, normalizeCurrencyCode(currencyCode))
                .map(this::toDomainAccount);
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public WalletAccount save(WalletAccount walletAccount) {
        validateAccountBalances(walletAccount);
        WalletAccountDO entity = new WalletAccountDO();
        fillAccountDO(entity, walletAccount);

        UpdateWrapper<WalletAccountDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", walletAccount.getUserId());
        wrapper.eq("currency_code", walletAccount.getCurrencyCode());
        int updatedRows = walletAccountMapper.update(entity, wrapper);
        if (updatedRows <= 0) {
            walletAccountMapper.insert(entity);
        }
        return toDomainAccount(entity);
    }

    /**
     * 查找分支信息。
     */
    @Override
    public Optional<WalletTccBranch> findBranch(String xid, String branchId) {
        return walletTccBranchMapper.findByXidAndBranchId(xid, branchId).map(this::toDomainBranch);
    }

    /**
     * 查找用于更新信息。
     */
    @Override
    public Optional<WalletTccBranch> findBranchForUpdate(String xid, String branchId) {
        return walletTccBranchMapper.findByXidAndBranchIdForUpdate(xid, branchId).map(this::toDomainBranch);
    }

    /**
     * 保存分支信息。
     */
    @Override
    @Transactional
    public WalletTccBranch saveBranch(WalletTccBranch branch) {
        WalletTccBranchDO entity = new WalletTccBranchDO();
        fillBranchDO(entity, branch);

        UpdateWrapper<WalletTccBranchDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("xid", branch.getXid());
        wrapper.eq("branch_id", branch.getBranchId());
        int updatedRows = walletTccBranchMapper.update(entity, wrapper);
        if (updatedRows <= 0) {
            walletTccBranchMapper.insert(entity);
        }
        return toDomainBranch(entity);
    }

    /**
     * 创建分支信息。
     */
    @Override
    @Transactional
    public WalletTccBranch createBranch(WalletTccBranch branch) {
        WalletTccBranchDO entity = new WalletTccBranchDO();
        fillBranchDO(entity, branch);
        walletTccBranchMapper.insert(entity);
        return toDomainBranch(entity);
    }

    /**
     * 查找记录用于更新信息。
     */
    @Override
    public Optional<WalletFreezeRecord> findFreezeRecordForUpdate(String xid, String branchId) {
        return walletFreezeRecordMapper.findByXidAndBranchIdForUpdate(xid, branchId).map(this::toDomainFreezeRecord);
    }

    /**
     * 保存记录。
     */
    @Override
    @Transactional
    public WalletFreezeRecord saveFreezeRecord(WalletFreezeRecord freezeRecord) {
        WalletFreezeRecordDO entity = new WalletFreezeRecordDO();
        fillFreezeRecordDO(entity, freezeRecord);

        UpdateWrapper<WalletFreezeRecordDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("xid", freezeRecord.getXid());
        wrapper.eq("branch_id", freezeRecord.getBranchId());
        int updatedRows = walletFreezeRecordMapper.update(entity, wrapper);
        if (updatedRows <= 0) {
            walletFreezeRecordMapper.insert(entity);
        }
        return toDomainFreezeRecord(entity);
    }

    /**
     * 创建记录。
     */
    @Override
    @Transactional
    public WalletFreezeRecord createFreezeRecord(WalletFreezeRecord freezeRecord) {
        WalletFreezeRecordDO entity = new WalletFreezeRecordDO();
        fillFreezeRecordDO(entity, freezeRecord);
        walletFreezeRecordMapper.insert(entity);
        return toDomainFreezeRecord(entity);
    }

    /**
     * 查找记录用于更新信息。
     */
    @Override
    public Optional<WalletFreezeRecord> findManualFreezeRecordForUpdate(Long userId, String freezeNo) {
        return walletFreezeRecordMapper.findManualByFreezeNoForUpdate(userId, freezeNo)
                .map(this::toDomainFreezeRecord);
    }

    /**
     * 查询记录列表。
     */
    @Override
    public List<WalletFreezeRecord> listFreezeRecords(Long userId,
                                                      String currencyCode,
                                                      WalletFreezeType freezeType,
                                                      WalletFreezeStatus freezeStatus,
                                                      int limit) {
        return walletFreezeRecordMapper.listByUserAndFilters(
                        userId,
                        normalizeOptionalCurrencyCode(currencyCode),
                        freezeType == null ? null : freezeType.name(),
                        freezeStatus == null ? null : freezeStatus.name(),
                        limit)
                .stream()
                .map(this::toDomainFreezeRecord)
                .toList();
    }

    private WalletAccount toDomainAccount(WalletAccountDO entity) {
        return new WalletAccount(
                entity.getUserId(),
                entity.getCurrencyCode(),
                toMoney(entity.getCurrencyCode(), entity.getAvailableBalance()),
                toMoney(entity.getCurrencyCode(), entity.getReservedBalance()),
                WalletAccountStatus.valueOf(entity.getAccountStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private WalletTccBranch toDomainBranch(WalletTccBranchDO entity) {
        return new WalletTccBranch(
                entity.getXid(),
                entity.getBranchId(),
                entity.getUserId(),
                TccOperationType.valueOf(entity.getOperationType()),
                WalletFreezeType.from(entity.getFreezeType()),
                toMoney(entity.getCurrencyCode(), entity.getAmount()),
                WalletTccBranchStatus.valueOf(entity.getBranchStatus()),
                entity.getBusinessNo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private WalletFreezeRecord toDomainFreezeRecord(WalletFreezeRecordDO entity) {
        return new WalletFreezeRecord(
                entity.getXid(),
                entity.getBranchId(),
                entity.getUserId(),
                WalletFreezeType.from(entity.getFreezeType()),
                TccOperationType.valueOf(entity.getOperationType()),
                toMoney(entity.getCurrencyCode(), entity.getAmount()),
                WalletFreezeStatus.valueOf(entity.getFreezeStatus()),
                entity.getBusinessNo(),
                entity.getFreezeReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillAccountDO(WalletAccountDO entity, WalletAccount account) {
        LocalDateTime now = LocalDateTime.now();
        entity.setUserId(account.getUserId());
        entity.setCurrencyCode(account.getCurrencyCode());
        entity.setAvailableBalance(account.getAvailableBalance() == null ? BigDecimal.ZERO : account.getAvailableBalance().getAmount());
        entity.setReservedBalance(account.getReservedBalance() == null ? BigDecimal.ZERO : account.getReservedBalance().getAmount());
        entity.setAccountStatus(account.getAccountStatus().name());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(account.getCreatedAt() == null ? now : account.getCreatedAt());
        }
        entity.setUpdatedAt(account.getUpdatedAt() == null ? now : account.getUpdatedAt());
    }

    private void fillBranchDO(WalletTccBranchDO entity, WalletTccBranch branch) {
        LocalDateTime now = LocalDateTime.now();
        entity.setXid(branch.getXid());
        entity.setBranchId(branch.getBranchId());
        entity.setUserId(branch.getUserId());
        entity.setOperationType(branch.getOperationType().name());
        entity.setFreezeType(branch.getFreezeType().name());
        entity.setBranchStatus(branch.getBranchStatus().name());
        entity.setAmount(branch.getAmount().getAmount());
        entity.setCurrencyCode(branch.getAmount().getCurrencyUnit().getCode());
        entity.setBusinessNo(branch.getBusinessNo());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(branch.getCreatedAt() == null ? now : branch.getCreatedAt());
        }
        entity.setUpdatedAt(branch.getUpdatedAt() == null ? now : branch.getUpdatedAt());
    }

    private void fillFreezeRecordDO(WalletFreezeRecordDO entity, WalletFreezeRecord freezeRecord) {
        LocalDateTime now = LocalDateTime.now();
        entity.setXid(freezeRecord.getXid());
        entity.setBranchId(freezeRecord.getBranchId());
        entity.setUserId(freezeRecord.getUserId());
        entity.setFreezeType(freezeRecord.getFreezeType().name());
        entity.setOperationType(freezeRecord.getOperationType().name());
        entity.setFreezeStatus(freezeRecord.getFreezeStatus().name());
        entity.setAmount(freezeRecord.getAmount().getAmount());
        entity.setCurrencyCode(freezeRecord.getAmount().getCurrencyUnit().getCode());
        entity.setBusinessNo(freezeRecord.getBusinessNo());
        entity.setFreezeReason(freezeRecord.getFreezeReason());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(freezeRecord.getCreatedAt() == null ? now : freezeRecord.getCreatedAt());
        }
        entity.setUpdatedAt(freezeRecord.getUpdatedAt() == null ? now : freezeRecord.getUpdatedAt());
    }

    private Money toMoney(String currencyCode, BigDecimal amount) {
        CurrencyUnit unit = CurrencyUnit.of(normalizeCurrencyCode(currencyCode));
        BigDecimal normalized = amount == null ? BigDecimal.ZERO : amount;
        return Money.of(unit, normalized, RoundingMode.HALF_UP);
    }

    private String normalizeCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return "CNY";
        }
        return currencyCode.trim().toUpperCase();
    }

    private String normalizeOptionalCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return null;
        }
        return currencyCode.trim().toUpperCase();
    }

    private void validateAccountBalances(WalletAccount walletAccount) {
        requireNonNegative(walletAccount.getAvailableBalance(), "availableBalance");
        requireNonNegative(walletAccount.getReservedBalance(), "reservedBalance");
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
