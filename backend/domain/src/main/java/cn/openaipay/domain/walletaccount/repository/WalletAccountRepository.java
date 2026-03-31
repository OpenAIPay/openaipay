package cn.openaipay.domain.walletaccount.repository;

import cn.openaipay.domain.walletaccount.model.WalletAccount;
import cn.openaipay.domain.walletaccount.model.WalletFreezeRecord;
import cn.openaipay.domain.walletaccount.model.WalletFreezeStatus;
import cn.openaipay.domain.walletaccount.model.WalletFreezeType;
import cn.openaipay.domain.walletaccount.model.WalletTccBranch;

import java.util.List;
import java.util.Optional;

/**
 * 钱包账户仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface WalletAccountRepository {

    /**
     * 按用户ID查询钱包账户。
     */
    Optional<WalletAccount> findByUserId(Long userId);

    /**
     * 按用户ID和币种查询钱包账户。
     */
    Optional<WalletAccount> findByUserIdAndCurrency(Long userId, String currencyCode);

    /**
     * 按用户ID加锁查询钱包账户。
     */
    Optional<WalletAccount> findByUserIdForUpdate(Long userId);

    /**
     * 按用户ID和币种加锁查询钱包账户。
     */
    Optional<WalletAccount> findByUserIdAndCurrencyForUpdate(Long userId, String currencyCode);

    /**
     * 保存钱包账户。
     */
    WalletAccount save(WalletAccount walletAccount);

    /**
     * 按XID和分支ID查询TCC分支。
     */
    Optional<WalletTccBranch> findBranch(String xid, String branchId);

    /**
     * 按XID和分支ID加锁查询TCC分支。
     */
    Optional<WalletTccBranch> findBranchForUpdate(String xid, String branchId);

    /**
     * 保存TCC分支。
     */
    WalletTccBranch saveBranch(WalletTccBranch branch);

    /**
     * 新建TCC分支。
     */
    WalletTccBranch createBranch(WalletTccBranch branch);

    /**
     * 按XID和分支ID加锁查询冻结明细。
     */
    Optional<WalletFreezeRecord> findFreezeRecordForUpdate(String xid, String branchId);

    /**
     * 保存冻结明细。
     */
    WalletFreezeRecord saveFreezeRecord(WalletFreezeRecord freezeRecord);

    /**
     * 新建冻结明细。
     */
    WalletFreezeRecord createFreezeRecord(WalletFreezeRecord freezeRecord);

    /**
     * 按冻结号加锁查询手工冻结明细。
     */
    Optional<WalletFreezeRecord> findManualFreezeRecordForUpdate(Long userId, String freezeNo);

    /**
     * 按条件查询冻结明细列表。
     */
    List<WalletFreezeRecord> listFreezeRecords(Long userId,
                                               String currencyCode,
                                               WalletFreezeType freezeType,
                                               WalletFreezeStatus freezeStatus,
                                               int limit);
}
