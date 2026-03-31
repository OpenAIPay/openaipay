package cn.openaipay.adapter.walletaccount.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.walletaccount.web.request.CreateWalletAccountRequest;
import cn.openaipay.adapter.walletaccount.web.request.WalletFreezeDeductRequest;
import cn.openaipay.adapter.walletaccount.web.request.WalletFreezeHoldRequest;
import cn.openaipay.adapter.walletaccount.web.request.WalletFreezeReleaseRequest;
import cn.openaipay.adapter.walletaccount.web.request.WalletTccCancelRequest;
import cn.openaipay.adapter.walletaccount.web.request.WalletTccConfirmRequest;
import cn.openaipay.adapter.walletaccount.web.request.WalletTccTryRequest;
import cn.openaipay.application.walletaccount.command.CreateWalletAccountCommand;
import cn.openaipay.application.walletaccount.command.WalletFreezeDeductCommand;
import cn.openaipay.application.walletaccount.command.WalletFreezeHoldCommand;
import cn.openaipay.application.walletaccount.command.WalletFreezeQueryCommand;
import cn.openaipay.application.walletaccount.command.WalletFreezeReleaseCommand;
import cn.openaipay.application.walletaccount.dto.WalletAccountDTO;
import cn.openaipay.application.walletaccount.dto.WalletFreezeOperationDTO;
import cn.openaipay.application.walletaccount.dto.WalletFreezeRecordDTO;
import cn.openaipay.application.walletaccount.dto.WalletFreezeSummaryDTO;
import cn.openaipay.application.walletaccount.dto.WalletTccBranchDTO;
import cn.openaipay.application.walletaccount.facade.WalletAccountFacade;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 钱包账户控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/wallet-accounts")
public class WalletAccountController {

    /** WalletAccountFacade组件 */
    private final WalletAccountFacade walletAccountFacade;

    public WalletAccountController(WalletAccountFacade walletAccountFacade) {
        this.walletAccountFacade = walletAccountFacade;
    }

    /**
     * 创建钱包信息。
     */
    @PostMapping
    public ApiResponse<Long> createWalletAccount(@Valid @RequestBody CreateWalletAccountRequest request) {
        Long userId = walletAccountFacade.createWalletAccount(
                new CreateWalletAccountCommand(request.userId(), request.currencyCode())
        );
        return ApiResponse.success(userId);
    }

    /**
     * 获取钱包信息。
     */
    @GetMapping("/{userId}")
    public ApiResponse<WalletAccountDTO> getWalletAccount(@PathVariable("userId") Long userId,
                                                          @RequestParam(value = "currencyCode", required = false) String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return ApiResponse.success(walletAccountFacade.getWalletAccount(userId));
        }
        return ApiResponse.success(walletAccountFacade.getWalletAccount(userId, currencyCode));
    }

    /**
     * 处理TCCTRY信息。
     */
    @PostMapping("/tcc/try")
    public ApiResponse<WalletTccBranchDTO> tccTry(@Valid @RequestBody WalletTccTryRequest request) {
        WalletTccBranchDTO result = walletAccountFacade.tccTry(
                request.xid(),
                request.branchId(),
                request.userId(),
                request.operationType(),
                request.freezeType(),
                request.amount(),
                request.businessNo()
        );
        return ApiResponse.success(result);
    }

    /**
     * 处理TCC信息。
     */
    @PostMapping("/tcc/confirm")
    public ApiResponse<WalletTccBranchDTO> tccConfirm(@Valid @RequestBody WalletTccConfirmRequest request) {
        WalletTccBranchDTO result = walletAccountFacade.tccConfirm(request.xid(), request.branchId());
        return ApiResponse.success(result);
    }

    /**
     * 处理TCC信息。
     */
    @PostMapping("/tcc/cancel")
    public ApiResponse<WalletTccBranchDTO> tccCancel(@Valid @RequestBody WalletTccCancelRequest request) {
        WalletTccBranchDTO result = walletAccountFacade.tccCancel(
                request.xid(),
                request.branchId(),
                request.userId(),
                request.operationType(),
                request.freezeType(),
                request.amount(),
                request.businessNo()
        );
        return ApiResponse.success(result);
    }

    /**
     * 处理业务数据。
     */
    @PostMapping("/freeze/hold")
    public ApiResponse<WalletFreezeOperationDTO> holdFreeze(@Valid @RequestBody WalletFreezeHoldRequest request) {
        return ApiResponse.success(walletAccountFacade.holdFreeze(new WalletFreezeHoldCommand(
                request.userId(),
                request.freezeNo(),
                request.freezeType(),
                request.amount(),
                request.reason()
        )));
    }

    /**
     * 处理业务数据。
     */
    @PostMapping("/freeze/release")
    public ApiResponse<WalletFreezeOperationDTO> releaseFreeze(@Valid @RequestBody WalletFreezeReleaseRequest request) {
        return ApiResponse.success(walletAccountFacade.releaseFreeze(new WalletFreezeReleaseCommand(
                request.userId(),
                request.freezeNo(),
                request.reason()
        )));
    }

    /**
     * 处理业务数据。
     */
    @PostMapping("/freeze/deduct")
    public ApiResponse<WalletFreezeOperationDTO> deductFreeze(@Valid @RequestBody WalletFreezeDeductRequest request) {
        return ApiResponse.success(walletAccountFacade.deductFreeze(new WalletFreezeDeductCommand(
                request.userId(),
                request.freezeNo(),
                request.reason()
        )));
    }

    /**
     * 查询记录列表。
     */
    @GetMapping("/{userId}/freezes")
    public ApiResponse<List<WalletFreezeRecordDTO>> listFreezeRecords(@PathVariable("userId") Long userId,
                                                                       @RequestParam(value = "currencyCode", required = false) String currencyCode,
                                                                       @RequestParam(value = "freezeType", required = false) String freezeType,
                                                                       @RequestParam(value = "freezeStatus", required = false) String freezeStatus,
                                                                       @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(walletAccountFacade.listFreezeRecords(new WalletFreezeQueryCommand(
                userId,
                currencyCode,
                freezeType,
                freezeStatus,
                limit
        )));
    }

    /**
     * 处理业务数据。
     */
    @GetMapping("/{userId}/freezes/summary")
    public ApiResponse<WalletFreezeSummaryDTO> summarizeFreezes(@PathVariable("userId") Long userId,
                                                                @RequestParam(value = "currencyCode", required = false) String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return ApiResponse.success(walletAccountFacade.summarizeFreezes(userId));
        }
        return ApiResponse.success(walletAccountFacade.summarizeFreezes(userId, currencyCode));
    }
}
