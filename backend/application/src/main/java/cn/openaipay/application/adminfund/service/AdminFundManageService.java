package cn.openaipay.application.adminfund.service;

import cn.openaipay.application.adminfund.dto.AdminBankCardRowDTO;
import cn.openaipay.application.adminfund.dto.AdminCreditAccountRowDTO;
import cn.openaipay.application.adminfund.dto.AdminFundAccountRowDTO;
import cn.openaipay.application.adminfund.dto.AdminFundOverviewDTO;
import cn.openaipay.application.adminfund.dto.AdminWalletAccountRowDTO;
import java.util.List;

/**
 * 资金中心服务
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public interface AdminFundManageService {

    /**
     * 查询概览。
     */
    AdminFundOverviewDTO overview();

    /**
     * 查询钱包账户列表。
     */
    List<AdminWalletAccountRowDTO> listWalletAccounts(Long userId, String accountStatus, Integer pageNo, Integer pageSize);

    /**
     * 查询基金账户列表。
     */
    List<AdminFundAccountRowDTO> listFundAccounts(Long userId, String fundCode, String accountStatus, Integer pageNo, Integer pageSize);

    /**
     * 查询爱花账户列表。
     */
    List<AdminCreditAccountRowDTO> listCreditAccounts(Long userId, String accountStatus, String payStatus, Integer pageNo, Integer pageSize);

    /**
     * 查询爱借账户列表。
     */
    List<AdminCreditAccountRowDTO> listLoanAccounts(Long userId, String accountStatus, String payStatus, Integer pageNo, Integer pageSize);

    /**
     * 查询银行卡列表。
     */
    List<AdminBankCardRowDTO> listBankCards(Long userId, String cardStatus, String bankCode, Integer pageNo, Integer pageSize);
}
