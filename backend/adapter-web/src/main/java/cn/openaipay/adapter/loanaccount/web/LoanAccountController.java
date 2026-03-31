package cn.openaipay.adapter.loanaccount.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.loanaccount.web.request.CreateLoanAccountRequest;
import cn.openaipay.adapter.loanaccount.web.request.LoanTccCancelRequest;
import cn.openaipay.adapter.loanaccount.web.request.LoanTccConfirmRequest;
import cn.openaipay.adapter.loanaccount.web.request.LoanTccTryRequest;
import cn.openaipay.application.loanaccount.command.CreateLoanAccountCommand;
import cn.openaipay.application.loanaccount.dto.LoanAccountDTO;
import cn.openaipay.application.loanaccount.dto.LoanTccBranchDTO;
import cn.openaipay.application.loanaccount.facade.LoanAccountFacade;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * LoanAccountController 控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/loan-accounts")
public class LoanAccountController {

    /** 借贷账户门面。 */
    private final LoanAccountFacade loanAccountFacade;

    public LoanAccountController(LoanAccountFacade loanAccountFacade) {
        this.loanAccountFacade = loanAccountFacade;
    }

    /**
     * 创建借款信息。
     */
    @PostMapping
    public ApiResponse<String> createLoanAccount(@Valid @RequestBody CreateLoanAccountRequest request) {
        String accountNo = loanAccountFacade.createLoanAccount(
                new CreateLoanAccountCommand(
                        request.userId(),
                        request.accountNo(),
                        request.totalLimit(),
                        request.repayDayOfMonth()
                )
        );
        return ApiResponse.success(accountNo);
    }

    /**
     * 按用户ID获取借款信息。
     */
    @GetMapping("/users/{userId}")
    public ApiResponse<LoanAccountDTO> getLoanAccountByUserId(@PathVariable("userId") Long userId) {
        return ApiResponse.success(loanAccountFacade.getLoanAccountByUserId(userId));
    }

    /**
     * 获取借款信息。
     */
    @GetMapping("/{accountNo}")
    public ApiResponse<LoanAccountDTO> getLoanAccount(@PathVariable("accountNo") String accountNo) {
        return ApiResponse.success(loanAccountFacade.getLoanAccount(accountNo));
    }

    /**
     * 处理TCCTRY信息。
     */
    @PostMapping("/tcc/try")
    public ApiResponse<LoanTccBranchDTO> tccTry(@Valid @RequestBody LoanTccTryRequest request) {
        LoanTccBranchDTO result = loanAccountFacade.tccTry(
                request.xid(),
                request.branchId(),
                request.accountNo(),
                request.operationType(),
                request.assetCategory(),
                request.amount(),
                request.businessNo()
        );
        return ApiResponse.success(result);
    }

    /**
     * 处理TCC信息。
     */
    @PostMapping("/tcc/confirm")
    public ApiResponse<LoanTccBranchDTO> tccConfirm(@Valid @RequestBody LoanTccConfirmRequest request) {
        LoanTccBranchDTO result = loanAccountFacade.tccConfirm(request.xid(), request.branchId());
        return ApiResponse.success(result);
    }

    /**
     * 处理TCC信息。
     */
    @PostMapping("/tcc/cancel")
    public ApiResponse<LoanTccBranchDTO> tccCancel(@Valid @RequestBody LoanTccCancelRequest request) {
        LoanTccBranchDTO result = loanAccountFacade.tccCancel(
                request.xid(),
                request.branchId(),
                request.accountNo(),
                request.operationType(),
                request.assetCategory(),
                request.amount(),
                request.businessNo()
        );
        return ApiResponse.success(result);
    }
}
