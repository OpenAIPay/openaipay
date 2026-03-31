package cn.openaipay.application.admin.pageinit.service.impl;

import cn.openaipay.application.admin.dto.AdminMenuDTO;
import cn.openaipay.application.admin.dto.AdminPageInitDTO;
import cn.openaipay.application.admin.dto.AdminProfileDTO;
import cn.openaipay.application.admin.pageinit.service.AdminPageInitService;
import cn.openaipay.application.coupon.dto.CouponOpsSummaryDTO;
import cn.openaipay.application.coupon.facade.CouponFacade;
import cn.openaipay.domain.admin.model.AdminAccount;
import cn.openaipay.domain.admin.repository.AdminRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台页面初始化应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Service
public class AdminPageInitServiceImpl implements AdminPageInitService {

    /** 后台信息 */
    private final AdminRepository adminRepository;
    /** 优惠券信息 */
    private final CouponFacade couponFacade;

    public AdminPageInitServiceImpl(AdminRepository adminRepository,
                                               CouponFacade couponFacade) {
        this.adminRepository = adminRepository;
        this.couponFacade = couponFacade;
    }

    /**
     * 处理页面初始化信息。
     */
    @Override
    @Transactional(readOnly = true)
    public AdminPageInitDTO pageInit(Long adminId) {
        AdminAccount account = requireAdminAccount(adminId);
        List<AdminMenuDTO> menus = listVisibleMenus(account.getAdminId());
        CouponOpsSummaryDTO summary = couponFacade.queryOpsSummary();
        List<String> roleCodes = adminRepository.findRoleCodesByAdminId(account.getAdminId());
        List<String> permissionCodes = adminRepository.findPermissionCodesByAdminId(account.getAdminId());

        return new AdminPageInitDTO(
                new AdminProfileDTO(
                        String.valueOf(account.getAdminId()),
                        account.getUsername(),
                        account.getDisplayName(),
                        account.getAccountStatus(),
                        account.getLastLoginAt()
                ),
                menus,
                summary,
                roleCodes,
                permissionCodes
        );
    }

    /**
     * 查询菜单信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdminMenuDTO> listVisibleMenus(Long adminId) {
        Long normalizedAdminId = requirePositive(adminId, "adminId");
        return adminRepository.findVisibleMenusByAdminId(normalizedAdminId)
                .stream()
                .map(menu -> new AdminMenuDTO(
                        menu.getMenuCode(),
                        menu.getParentCode(),
                        menu.getMenuName(),
                        menu.getPath(),
                        menu.getIcon(),
                        menu.getSortNo()
                ))
                .toList();
    }

    private AdminAccount requireAdminAccount(Long adminId) {
        Long normalizedAdminId = requirePositive(adminId, "adminId");
        return adminRepository.findByAdminId(normalizedAdminId)
                .orElseThrow(() -> new NoSuchElementException("admin account not found: " + adminId));
    }

    private Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}
