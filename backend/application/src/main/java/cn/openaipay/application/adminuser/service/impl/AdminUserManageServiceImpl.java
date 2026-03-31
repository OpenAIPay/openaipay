package cn.openaipay.application.adminuser.service.impl;

import cn.openaipay.application.adminuser.dto.AdminUserCenterSummaryDTO;
import cn.openaipay.application.adminuser.dto.AdminUserDetailDTO;
import cn.openaipay.application.adminuser.dto.AdminUserSummaryDTO;
import cn.openaipay.application.adminuser.port.AdminUserManagePort;
import cn.openaipay.application.adminuser.service.AdminUserManageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 用户管理服务实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Service
public class AdminUserManageServiceImpl implements AdminUserManageService {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 200;
    private static final Set<String> ALLOWED_USER_STATUS = Set.of("ACTIVE", "FROZEN", "CLOSED");

    private final AdminUserManagePort adminUserManagePort;

    public AdminUserManageServiceImpl(AdminUserManagePort adminUserManagePort) {
        this.adminUserManagePort = adminUserManagePort;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserCenterSummaryDTO summary() {
        return adminUserManagePort.summary();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserSummaryDTO> listUsers(String keyword, String accountStatus, String kycLevel, Integer pageNo, Integer pageSize) {
        return adminUserManagePort.listUsers(
                keyword,
                normalizeFilter(accountStatus),
                normalizeFilter(kycLevel),
                normalizePageNo(pageNo),
                normalizePageSize(pageSize)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailDTO getUserDetail(Long userId) {
        return adminUserManagePort.getUserDetail(requirePositive(userId, "userId"));
    }

    @Override
    @Transactional
    public AdminUserSummaryDTO changeUserStatus(Long userId, String status) {
        return adminUserManagePort.changeUserStatus(
                requirePositive(userId, "userId"),
                normalizeUserStatus(status)
        );
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

    private String normalizeFilter(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        return rawValue.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeUserStatus(String rawStatus) {
        String normalized = normalizeFilter(rawStatus);
        if (normalized == null || !ALLOWED_USER_STATUS.contains(normalized)) {
            throw new IllegalArgumentException("unsupported user status: " + rawStatus);
        }
        return normalized;
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}
