package cn.openaipay.application.accountquery.facade.impl;

import cn.openaipay.application.accountquery.facade.AccountQueryFacade;
import cn.openaipay.application.creditaccount.dto.CreditAccountDTO;
import cn.openaipay.application.creditaccount.dto.CreditCurrentBillDetailDTO;
import cn.openaipay.application.creditaccount.facade.CreditAccountFacade;
import cn.openaipay.application.fundaccount.dto.FundAccountDTO;
import cn.openaipay.application.fundaccount.facade.FundAccountFacade;
import cn.openaipay.application.loanaccount.dto.LoanAccountDTO;
import cn.openaipay.application.loanaccount.facade.LoanAccountFacade;
import cn.openaipay.application.walletaccount.dto.WalletAccountDTO;
import cn.openaipay.application.walletaccount.facade.WalletAccountFacade;
import org.springframework.stereotype.Service;

/**
 * 账户查询门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
@Service
public class AccountQueryFacadeImpl implements AccountQueryFacade {

    /** 爱花账户门面。 */
    private final CreditAccountFacade creditAccountFacade;
    /** 爱借账户门面。 */
    private final LoanAccountFacade loanAccountFacade;
    /** 爱存账户门面。 */
    private final FundAccountFacade fundAccountFacade;
    /** 余额账户门面。 */
    private final WalletAccountFacade walletAccountFacade;

    public AccountQueryFacadeImpl(CreditAccountFacade creditAccountFacade,
                                  LoanAccountFacade loanAccountFacade,
                                  FundAccountFacade fundAccountFacade,
                                  WalletAccountFacade walletAccountFacade) {
        this.creditAccountFacade = creditAccountFacade;
        this.loanAccountFacade = loanAccountFacade;
        this.fundAccountFacade = fundAccountFacade;
        this.walletAccountFacade = walletAccountFacade;
    }

    /**
     * 查询爱花账户。
     */
    @Override
    public CreditAccountDTO getCreditAccountByUserId(Long userId) {
        return creditAccountFacade.getCreditAccountByUserId(userId);
    }

    /**
     * 查询爱花当前账单明细。
     */
    @Override
    public CreditCurrentBillDetailDTO getCreditCurrentBillDetailByUserId(Long userId) {
        return creditAccountFacade.getCurrentBillDetailByUserId(userId);
    }

    /**
     * 查询爱花下期账单明细。
     */
    @Override
    public CreditCurrentBillDetailDTO getCreditNextBillDetailByUserId(Long userId) {
        return creditAccountFacade.getNextBillDetailByUserId(userId);
    }

    /**
     * 查询爱借账户。
     */
    @Override
    public LoanAccountDTO getLoanAccountByUserId(Long userId) {
        return loanAccountFacade.getLoanAccountByUserId(userId);
    }

    /**
     * 查询爱存账户。
     */
    @Override
    public FundAccountDTO getFundAccountByUserId(Long userId, String fundCode) {
        if (fundCode == null || fundCode.isBlank()) {
            return fundAccountFacade.getFundAccount(userId);
        }
        return fundAccountFacade.getFundAccount(userId, fundCode);
    }

    /**
     * 查询余额账户。
     */
    @Override
    public WalletAccountDTO getWalletAccountByUserId(Long userId, String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return walletAccountFacade.getWalletAccount(userId);
        }
        return walletAccountFacade.getWalletAccount(userId, currencyCode);
    }
}
