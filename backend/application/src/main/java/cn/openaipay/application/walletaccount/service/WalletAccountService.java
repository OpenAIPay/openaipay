package cn.openaipay.application.walletaccount.service;

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

import java.util.List;

/**
 * 钱包账户应用服务接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface WalletAccountService {

    /**
     * 创建钱包信息。
     */
    Long createWalletAccount(CreateWalletAccountCommand command);

    /**
     * 获取钱包信息。
     */
    WalletAccountDTO getWalletAccount(Long userId);

    /**
     * 获取钱包信息。
     */
    WalletAccountDTO getWalletAccount(Long userId, String currencyCode);

    /**
     * 获取钱包信息，不存在时自动补齐默认账户。
     */
    WalletAccountDTO getOrCreateWalletAccount(Long userId, String currencyCode);

    /**
     * 处理TCCTRY信息。
     */
    WalletTccBranchDTO tccTry(WalletTccTryCommand command);

    /**
     * 处理TCC信息。
     */
    WalletTccBranchDTO tccConfirm(WalletTccConfirmCommand command);

    /**
     * 处理TCC信息。
     */
    WalletTccBranchDTO tccCancel(WalletTccCancelCommand command);

    /**
     * 处理业务数据。
     */
    WalletFreezeOperationDTO holdFreeze(WalletFreezeHoldCommand command);

    /**
     * 处理业务数据。
     */
    WalletFreezeOperationDTO releaseFreeze(WalletFreezeReleaseCommand command);

    /**
     * 处理业务数据。
     */
    WalletFreezeOperationDTO deductFreeze(WalletFreezeDeductCommand command);

    /**
     * 查询记录列表。
     */
    List<WalletFreezeRecordDTO> listFreezeRecords(WalletFreezeQueryCommand command);

    /**
     * 处理业务数据。
     */
    WalletFreezeSummaryDTO summarizeFreezes(Long userId);

    /**
     * 处理业务数据。
     */
    WalletFreezeSummaryDTO summarizeFreezes(Long userId, String currencyCode);
}
