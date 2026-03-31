package cn.openaipay.adapter.accountquery.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.accountquery.facade.AccountQueryFacade;
import cn.openaipay.application.creditaccount.dto.CreditAccountDTO;
import cn.openaipay.application.creditaccount.dto.CreditCurrentBillDetailDTO;
import cn.openaipay.application.fundaccount.dto.FundAccountDTO;
import cn.openaipay.application.loanaccount.dto.LoanAccountDTO;
import cn.openaipay.application.walletaccount.dto.WalletAccountDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账户查询控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
@RestController
@RequestMapping("/api/account-query")
public class AccountQueryController {

    /** 账户查询门面。 */
    private final AccountQueryFacade accountQueryFacade;

    public AccountQueryController(AccountQueryFacade accountQueryFacade) {
        this.accountQueryFacade = accountQueryFacade;
    }

    /**
     * 查询爱花账户。
     */
    @GetMapping("/credit/users/{userId}")
    public ApiResponse<CreditAccountDTO> getCreditAccount(@PathVariable("userId") Long userId) {
        return ApiResponse.success(accountQueryFacade.getCreditAccountByUserId(userId));
    }

    /**
     * 查询爱花当前账单明细。
     */
    @GetMapping("/credit/users/{userId}/current-bill-detail")
    public ApiResponse<CreditCurrentBillDetailDTO> getCreditCurrentBillDetail(@PathVariable("userId") Long userId) {
        return ApiResponse.success(accountQueryFacade.getCreditCurrentBillDetailByUserId(userId));
    }

    /**
     * 查询爱花下期账单明细。
     */
    @GetMapping("/credit/users/{userId}/next-bill-detail")
    public ApiResponse<CreditCurrentBillDetailDTO> getCreditNextBillDetail(@PathVariable("userId") Long userId) {
        return ApiResponse.success(accountQueryFacade.getCreditNextBillDetailByUserId(userId));
    }

    /**
     * 查询爱借账户。
     */
    @GetMapping("/loan/users/{userId}")
    public ApiResponse<LoanAccountDTO> getLoanAccount(@PathVariable("userId") Long userId) {
        return ApiResponse.success(accountQueryFacade.getLoanAccountByUserId(userId));
    }

    /**
     * 查询爱存账户。
     */
    @GetMapping("/fund/users/{userId}")
    public ApiResponse<FundAccountDTO> getFundAccount(@PathVariable("userId") Long userId,
                                                      @RequestParam(value = "fundCode", required = false) String fundCode) {
        return ApiResponse.success(accountQueryFacade.getFundAccountByUserId(userId, fundCode));
    }

    /**
     * 查询余额账户。
     */
    @GetMapping("/wallet/users/{userId}")
    public ApiResponse<WalletAccountDTO> getWalletAccount(@PathVariable("userId") Long userId,
                                                          @RequestParam(value = "currencyCode", required = false) String currencyCode) {
        return ApiResponse.success(accountQueryFacade.getWalletAccountByUserId(userId, currencyCode));
    }
}
