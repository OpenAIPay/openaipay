package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.admin.web.request.ChangeUserAccountStatusRequest;
import cn.openaipay.adapter.admin.web.response.AdminFundAccountSnapshotResponse;
import cn.openaipay.adapter.admin.web.response.AdminUserCenterSummaryResponse;
import cn.openaipay.adapter.admin.web.response.AdminUserDetailResponse;
import cn.openaipay.adapter.admin.web.response.AdminUserSummaryResponse;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.adminuser.dto.AdminFundAccountSnapshotDTO;
import cn.openaipay.application.adminuser.dto.AdminUserCenterSummaryDTO;
import cn.openaipay.application.adminuser.dto.AdminUserDetailDTO;
import cn.openaipay.application.adminuser.dto.AdminUserSummaryDTO;
import cn.openaipay.application.adminuser.facade.AdminUserManageFacade;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台管理用户中心控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    /** 用户管理门面 */
    private final AdminUserManageFacade adminUserManageFacade;

    public AdminUserController(AdminUserManageFacade adminUserManageFacade) {
        this.adminUserManageFacade = adminUserManageFacade;
    }

    /**
     * 处理汇总信息。
     */
    @GetMapping("/summary")
    @RequireAdminPermission("user.account.list")
    public ApiResponse<AdminUserCenterSummaryResponse> summary() {
        return ApiResponse.success(toSummaryResponse(adminUserManageFacade.summary()));
    }

    /**
     * 查询用户信息列表。
     */
    @GetMapping
    @RequireAdminPermission("user.account.list")
    public ApiResponse<List<AdminUserSummaryResponse>> listUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "accountStatus", required = false) String accountStatus,
            @RequestParam(value = "kycLevel", required = false) String kycLevel,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "limit", required = false) Integer limit) {
        Integer resolvedPageSize = pageSize == null ? limit : pageSize;
        return ApiResponse.success(adminUserManageFacade.listUsers(keyword, accountStatus, kycLevel, pageNo, resolvedPageSize).stream()
                .map(this::toSummaryResponse)
                .toList());
    }

    /**
     * 获取用户明细信息。
     */
    @GetMapping("/{userId}")
    @RequireAdminPermission("user.account.view")
    public ApiResponse<AdminUserDetailResponse> getUserDetail(@PathVariable("userId") Long userId) {
        return ApiResponse.success(toDetailResponse(adminUserManageFacade.getUserDetail(userId)));
    }

    /**
     * 处理用户状态。
     */
    @PutMapping("/{userId}/status")
    @RequireAdminPermission("user.account.status")
    public ApiResponse<AdminUserSummaryResponse> changeUserStatus(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody ChangeUserAccountStatusRequest request) {
        return ApiResponse.success(toSummaryResponse(adminUserManageFacade.changeUserStatus(userId, request.status())));
    }

    private AdminUserCenterSummaryResponse toSummaryResponse(AdminUserCenterSummaryDTO dto) {
        return new AdminUserCenterSummaryResponse(
                dto.totalUserCount(),
                dto.activeUserCount(),
                dto.frozenUserCount(),
                dto.closedUserCount(),
                dto.walletActiveUserCount(),
                dto.creditNormalUserCount()
        );
    }

    private AdminUserSummaryResponse toSummaryResponse(AdminUserSummaryDTO dto) {
        return new AdminUserSummaryResponse(
                dto.userId(),
                dto.realName(),
                dto.aipayUid(),
                dto.loginId(),
                dto.nickname(),
                dto.mobile(),
                dto.accountStatus(),
                dto.kycLevel(),
                dto.loginPasswordSet(),
                dto.payPasswordSet(),
                dto.walletAccountStatus(),
                dto.walletAvailableBalance(),
                dto.walletReservedBalance(),
                dto.creditAccountStatus(),
                dto.creditTotalLimit(),
                dto.creditUsedAmount(),
                dto.creditPrincipalBalance(),
                dto.fundAccountCount(),
                dto.createdAt(),
                dto.updatedAt()
        );
    }

    private AdminFundAccountSnapshotResponse toFundSnapshotResponse(AdminFundAccountSnapshotDTO dto) {
        return new AdminFundAccountSnapshotResponse(
                dto.fundCode(),
                dto.currencyCode(),
                dto.availableShare(),
                dto.frozenShare(),
                dto.pendingSubscribeAmount(),
                dto.pendingRedeemShare(),
                dto.accumulatedIncome(),
                dto.yesterdayIncome(),
                dto.latestNav(),
                dto.accountStatus(),
                dto.updatedAt()
        );
    }

    private AdminUserDetailResponse toDetailResponse(AdminUserDetailDTO dto) {
        return new AdminUserDetailResponse(
                toSummaryResponse(dto.account()),
                dto.avatarUrl(),
                dto.countryCode(),
                dto.maskedRealName(),
                dto.idCardNo(),
                dto.gender(),
                dto.region(),
                dto.birthday(),
                dto.biometricEnabled(),
                dto.twoFactorMode(),
                dto.riskLevel(),
                dto.deviceLockEnabled(),
                dto.privacyModeEnabled(),
                dto.allowSearchByMobile(),
                dto.allowSearchByAipayUid(),
                dto.hideRealName(),
                dto.personalizedRecommendationEnabled(),
                dto.creditInterestBalance(),
                dto.creditFineBalance(),
                dto.fundTotalAvailableShare(),
                dto.fundTotalAccumulatedIncome(),
                dto.fundAccounts().stream().map(this::toFundSnapshotResponse).toList(),
                dto.profileUpdatedAt()
        );
    }
}
