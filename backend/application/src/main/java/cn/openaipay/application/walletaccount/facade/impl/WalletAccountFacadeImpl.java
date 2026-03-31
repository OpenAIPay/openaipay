package cn.openaipay.application.walletaccount.facade.impl;

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
import cn.openaipay.application.walletaccount.dto.WalletTccBranchDTO;
import cn.openaipay.application.walletaccount.facade.WalletAccountFacade;
import cn.openaipay.application.walletaccount.service.WalletAccountService;
import org.joda.money.Money;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 钱包账户门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class WalletAccountFacadeImpl implements WalletAccountFacade {

    /** WalletAccountService组件 */
    private final WalletAccountService walletAccountService;

    public WalletAccountFacadeImpl(WalletAccountService walletAccountService) {
        this.walletAccountService = walletAccountService;
    }

    /**
     * 创建钱包账户信息。
     */
    @Override
    public Long createWalletAccount(CreateWalletAccountCommand command) {
        return walletAccountService.createWalletAccount(command);
    }

    /**
     * 获取钱包账户信息。
     */
    @Override
    public WalletAccountDTO getWalletAccount(Long userId) {
        return walletAccountService.getWalletAccount(userId);
    }

    /**
     * 获取钱包账户信息。
     */
    @Override
    public WalletAccountDTO getWalletAccount(Long userId, String currencyCode) {
        return walletAccountService.getWalletAccount(userId, currencyCode);
    }

    /**
     * 获取钱包信息，不存在时自动补齐默认账户。
     */
    @Override
    public WalletAccountDTO getOrCreateWalletAccount(Long userId, String currencyCode) {
        return walletAccountService.getOrCreateWalletAccount(userId, currencyCode);
    }

    /**
     * 处理TCCTRY信息。
     */
    @Override
    public WalletTccBranchDTO tccTry(String xid,
                                     String branchId,
                                     Long userId,
                                     String operationType,
                                     String freezeType,
                                     Money amount,
                                     String businessNo) {
        return walletAccountService.tccTry(new WalletTccTryCommand(
                xid,
                branchId,
                userId,
                operationType,
                freezeType,
                amount,
                businessNo
        ));
    }

    /**
     * 处理TCC信息。
     */
    @Override
    public WalletTccBranchDTO tccConfirm(String xid, String branchId) {
        return walletAccountService.tccConfirm(new WalletTccConfirmCommand(xid, branchId));
    }

    /**
     * 处理TCC信息。
     */
    @Override
    public WalletTccBranchDTO tccCancel(String xid,
                                        String branchId,
                                        Long userId,
                                        String operationType,
                                        String freezeType,
                                        Money amount,
                                        String businessNo) {
        return walletAccountService.tccCancel(new WalletTccCancelCommand(
                xid,
                branchId,
                userId,
                operationType,
                freezeType,
                amount,
                businessNo
        ));
    }

    /**
     * 处理业务数据。
     */
    @Override
    public WalletFreezeOperationDTO holdFreeze(WalletFreezeHoldCommand command) {
        return walletAccountService.holdFreeze(command);
    }

    /**
     * 处理业务数据。
     */
    @Override
    public WalletFreezeOperationDTO releaseFreeze(WalletFreezeReleaseCommand command) {
        return walletAccountService.releaseFreeze(command);
    }

    /**
     * 处理业务数据。
     */
    @Override
    public WalletFreezeOperationDTO deductFreeze(WalletFreezeDeductCommand command) {
        return walletAccountService.deductFreeze(command);
    }

    /**
     * 查询记录列表。
     */
    @Override
    public List<WalletFreezeRecordDTO> listFreezeRecords(WalletFreezeQueryCommand command) {
        return walletAccountService.listFreezeRecords(command);
    }

    /**
     * 处理业务数据。
     */
    @Override
    public WalletFreezeSummaryDTO summarizeFreezes(Long userId) {
        return walletAccountService.summarizeFreezes(userId);
    }

    /**
     * 处理业务数据。
     */
    @Override
    public WalletFreezeSummaryDTO summarizeFreezes(Long userId, String currencyCode) {
        return walletAccountService.summarizeFreezes(userId, currencyCode);
    }
}
