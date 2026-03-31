package cn.openaipay.application.adminfund.facade.impl;

import cn.openaipay.application.adminfund.dto.AdminBankCardRowDTO;
import cn.openaipay.application.adminfund.dto.AdminCreditAccountRowDTO;
import cn.openaipay.application.adminfund.dto.AdminFundAccountRowDTO;
import cn.openaipay.application.adminfund.dto.AdminFundOverviewDTO;
import cn.openaipay.application.adminfund.dto.AdminWalletAccountRowDTO;
import cn.openaipay.application.adminfund.facade.AdminFundManageFacade;
import cn.openaipay.application.adminfund.service.AdminFundManageService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 资金中心门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Service
public class AdminFundManageFacadeImpl implements AdminFundManageFacade {

    private final AdminFundManageService adminFundManageService;

    public AdminFundManageFacadeImpl(AdminFundManageService adminFundManageService) {
        this.adminFundManageService = adminFundManageService;
    }

    @Override
    public AdminFundOverviewDTO overview() {
        return adminFundManageService.overview();
    }

    @Override
    public List<AdminWalletAccountRowDTO> listWalletAccounts(Long userId, String accountStatus, Integer pageNo, Integer pageSize) {
        return adminFundManageService.listWalletAccounts(userId, accountStatus, pageNo, pageSize);
    }

    @Override
    public List<AdminFundAccountRowDTO> listFundAccounts(Long userId, String fundCode, String accountStatus, Integer pageNo, Integer pageSize) {
        return adminFundManageService.listFundAccounts(userId, fundCode, accountStatus, pageNo, pageSize);
    }

    @Override
    public List<AdminCreditAccountRowDTO> listCreditAccounts(Long userId, String accountStatus, String payStatus, Integer pageNo, Integer pageSize) {
        return adminFundManageService.listCreditAccounts(userId, accountStatus, payStatus, pageNo, pageSize);
    }

    @Override
    public List<AdminCreditAccountRowDTO> listLoanAccounts(Long userId, String accountStatus, String payStatus, Integer pageNo, Integer pageSize) {
        return adminFundManageService.listLoanAccounts(userId, accountStatus, payStatus, pageNo, pageSize);
    }

    @Override
    public List<AdminBankCardRowDTO> listBankCards(Long userId, String cardStatus, String bankCode, Integer pageNo, Integer pageSize) {
        return adminFundManageService.listBankCards(userId, cardStatus, bankCode, pageNo, pageSize);
    }
}
