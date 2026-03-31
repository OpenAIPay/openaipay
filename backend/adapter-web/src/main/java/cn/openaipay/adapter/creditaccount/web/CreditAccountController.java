package cn.openaipay.adapter.creditaccount.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.creditaccount.web.request.CreditTccCancelRequest;
import cn.openaipay.adapter.creditaccount.web.request.CreditTccConfirmRequest;
import cn.openaipay.adapter.creditaccount.web.request.CreditTccTryRequest;
import cn.openaipay.adapter.creditaccount.web.request.CreateCreditAccountRequest;
import cn.openaipay.application.creditaccount.command.CreateCreditAccountCommand;
import cn.openaipay.application.creditaccount.dto.CreditAccountDTO;
import cn.openaipay.application.creditaccount.dto.CreditCurrentBillDetailDTO;
import cn.openaipay.application.creditaccount.dto.CreditTccBranchDTO;
import cn.openaipay.application.creditaccount.facade.CreditAccountFacade;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 信用账户控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/credit-accounts")
public class CreditAccountController {

    /** CreditAccountFacade组件 */
    private final CreditAccountFacade creditAccountFacade;

    public CreditAccountController(CreditAccountFacade creditAccountFacade) {
        this.creditAccountFacade = creditAccountFacade;
    }

    /**
     * 创建信用信息。
     */
    @PostMapping
    public ApiResponse<String> createCreditAccount(@Valid @RequestBody CreateCreditAccountRequest request) {
        String accountNo = creditAccountFacade.createCreditAccount(
                new CreateCreditAccountCommand(
                        request.userId(),
                        request.accountNo(),
                        request.totalLimit(),
                        request.repayDayOfMonth()
                )
        );
        return ApiResponse.success(accountNo);
    }

    /**
     * 按用户ID获取信用信息。
     */
    @GetMapping("/users/{userId}")
    public ApiResponse<CreditAccountDTO> getCreditAccountByUserId(@PathVariable("userId") Long userId) {
        return ApiResponse.success(creditAccountFacade.getCreditAccountByUserId(userId));
    }

    /**
     * 查询爱花当前账单明细页数据。
     *
     * 业务场景：移动端从爱花总计账单页点击“明细”进入当前账单列表页时，
     * 通过该接口读取顶部摘要金额和第一页账单列表。
     */
    /**
     * 按用户ID获取当前明细信息。
     */
    @GetMapping("/users/{userId}/current-bill-detail")
    public ApiResponse<CreditCurrentBillDetailDTO> getCurrentBillDetailByUserId(@PathVariable("userId") Long userId) {
        return ApiResponse.success(creditAccountFacade.getCurrentBillDetailByUserId(userId));
    }

    /**
     * 查询爱花下期账单明细页数据。
     *
     * 业务场景：移动端从“3月总计账单”页点击“4月账单累计中(元)”进入下期账单页时，
     * 通过该接口读取 4 月即将出账账单的顶部摘要和 3 月消费列表。
     */
    @GetMapping("/users/{userId}/next-bill-detail")
    public ApiResponse<CreditCurrentBillDetailDTO> getNextBillDetailByUserId(@PathVariable("userId") Long userId) {
        return ApiResponse.success(creditAccountFacade.getNextBillDetailByUserId(userId));
    }

    /**
     * 获取信用信息。
     */
    @GetMapping("/{accountNo}")
    public ApiResponse<CreditAccountDTO> getCreditAccount(@PathVariable("accountNo") String accountNo) {
        return ApiResponse.success(creditAccountFacade.getCreditAccount(accountNo));
    }

    /**
     * 处理TCCTRY信息。
     */
    @PostMapping("/tcc/try")
    public ApiResponse<CreditTccBranchDTO> tccTry(@Valid @RequestBody CreditTccTryRequest request) {
        CreditTccBranchDTO result = creditAccountFacade.tccTry(
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
    public ApiResponse<CreditTccBranchDTO> tccConfirm(@Valid @RequestBody CreditTccConfirmRequest request) {
        CreditTccBranchDTO result = creditAccountFacade.tccConfirm(request.xid(), request.branchId());
        return ApiResponse.success(result);
    }

    /**
     * 处理TCC信息。
     */
    @PostMapping("/tcc/cancel")
    public ApiResponse<CreditTccBranchDTO> tccCancel(@Valid @RequestBody CreditTccCancelRequest request) {
        CreditTccBranchDTO result = creditAccountFacade.tccCancel(
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
