package cn.openaipay.application.adminfund.service.impl;

import cn.openaipay.application.adminfund.dto.AdminBankCardRowDTO;
import cn.openaipay.application.adminfund.dto.AdminCreditAccountRowDTO;
import cn.openaipay.application.adminfund.dto.AdminFundAccountRowDTO;
import cn.openaipay.application.adminfund.dto.AdminFundOverviewDTO;
import cn.openaipay.application.adminfund.dto.AdminWalletAccountRowDTO;
import cn.openaipay.application.adminfund.port.AdminFundManagePort;
import cn.openaipay.application.adminfund.service.AdminFundManageService;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 资金中心服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Service
public class AdminFundManageServiceImpl implements AdminFundManageService {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 200;

    private final AdminFundManagePort adminFundManagePort;

    public AdminFundManageServiceImpl(AdminFundManagePort adminFundManagePort) {
        this.adminFundManagePort = adminFundManagePort;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminFundOverviewDTO overview() {
        return adminFundManagePort.overview();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminWalletAccountRowDTO> listWalletAccounts(Long userId, String accountStatus, Integer pageNo, Integer pageSize) {
        return adminFundManagePort.listWalletAccounts(
                normalizeOptionalPositive(userId, "userId"),
                normalizeCode(accountStatus),
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminFundAccountRowDTO> listFundAccounts(Long userId, String fundCode, String accountStatus, Integer pageNo, Integer pageSize) {
        return adminFundManagePort.listFundAccounts(
                normalizeOptionalPositive(userId, "userId"),
                normalizeFundCode(fundCode),
                normalizeCode(accountStatus),
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminCreditAccountRowDTO> listCreditAccounts(Long userId, String accountStatus, String payStatus, Integer pageNo, Integer pageSize) {
        return adminFundManagePort.listCreditAccounts(
                normalizeOptionalPositive(userId, "userId"),
                normalizeCode(accountStatus),
                normalizeCode(payStatus),
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminCreditAccountRowDTO> listLoanAccounts(Long userId, String accountStatus, String payStatus, Integer pageNo, Integer pageSize) {
        return adminFundManagePort.listLoanAccounts(
                normalizeOptionalPositive(userId, "userId"),
                normalizeCode(accountStatus),
                normalizeCode(payStatus),
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminBankCardRowDTO> listBankCards(Long userId, String cardStatus, String bankCode, Integer pageNo, Integer pageSize) {
        return adminFundManagePort.listBankCards(
                normalizeOptionalPositive(userId, "userId"),
                normalizeCode(cardStatus),
                normalizeCode(bankCode),
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    private Long normalizeOptionalPositive(Long value, String label) {
        if (value == null) {
            return null;
        }
        if (value <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0");
        }
        return value;
    }

    private String normalizeFundCode(String raw) {
        String normalized = normalizeKeyword(raw);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeCode(String raw) {
        String normalized = normalizeKeyword(raw);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeKeyword(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim();
    }

    private int normalizePageNo(Integer pageNo) {
        if (pageNo == null || pageNo <= 0) {
            return DEFAULT_PAGE_NO;
        }
        return pageNo;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
