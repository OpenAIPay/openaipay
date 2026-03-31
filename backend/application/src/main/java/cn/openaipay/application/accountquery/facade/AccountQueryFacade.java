package cn.openaipay.application.accountquery.facade;

import cn.openaipay.application.creditaccount.dto.CreditAccountDTO;
import cn.openaipay.application.creditaccount.dto.CreditCurrentBillDetailDTO;
import cn.openaipay.application.fundaccount.dto.FundAccountDTO;
import cn.openaipay.application.loanaccount.dto.LoanAccountDTO;
import cn.openaipay.application.walletaccount.dto.WalletAccountDTO;

/**
 * 账户查询门面接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
public interface AccountQueryFacade {

    /**
     * 查询爱花账户。
     */
    CreditAccountDTO getCreditAccountByUserId(Long userId);

    /**
     * 查询爱花当前账单明细。
     */
    CreditCurrentBillDetailDTO getCreditCurrentBillDetailByUserId(Long userId);

    /**
     * 查询爱花下期账单明细。
     */
    CreditCurrentBillDetailDTO getCreditNextBillDetailByUserId(Long userId);

    /**
     * 查询爱借账户。
     */
    LoanAccountDTO getLoanAccountByUserId(Long userId);

    /**
     * 查询爱存账户。
     */
    FundAccountDTO getFundAccountByUserId(Long userId, String fundCode);

    /**
     * 查询余额账户。
     */
    WalletAccountDTO getWalletAccountByUserId(Long userId, String currencyCode);
}
