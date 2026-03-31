package cn.openaipay.adapter.user.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.user.web.request.UpdateUserPrivacyRequest;
import cn.openaipay.adapter.user.web.request.UpdateUserProfileRequest;
import cn.openaipay.adapter.user.web.request.UpdateUserSecurityRequest;
import cn.openaipay.adapter.user.web.response.UserProfileLookupResponse;
import cn.openaipay.application.user.command.UpdateUserPrivacyCommand;
import cn.openaipay.application.user.command.UpdateUserProfileCommand;
import cn.openaipay.application.user.command.UpdateUserSecurityCommand;
import cn.openaipay.application.user.dto.UserInitDTO;
import cn.openaipay.application.user.dto.UserProfileDTO;
import cn.openaipay.application.user.dto.UserRecentContactDTO;
import cn.openaipay.application.user.dto.UserSecurityDTO;
import cn.openaipay.application.user.dto.UserSettingsDTO;
import cn.openaipay.application.user.facade.UserFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户控制器
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /** UserFacade组件 */
    private final UserFacade userFacade;

    public UserController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    /**
     * 获取资料信息。
     */
    @GetMapping("/{userId}/profile")
    public ApiResponse<UserProfileDTO> getProfile(@PathVariable("userId") Long userId) {
        return ApiResponse.success(userFacade.getProfile(userId));
    }

    /**
     * 按登录号查询资料信息。
     */
    @GetMapping("/profile-by-login")
    public ApiResponse<UserProfileLookupResponse> getProfileByLoginId(@RequestParam("loginId") String loginId) {
        return ApiResponse.success(UserProfileLookupResponse.from(
                userFacade.getProfileByLoginId(normalizeMainlandPhone(loginId))
        ));
    }

    /**
     * 查询资料信息列表。
     */
    @GetMapping("/profiles")
    public ApiResponse<List<UserProfileDTO>> listProfiles(@RequestParam("userIds") List<Long> userIds) {
        return ApiResponse.success(userFacade.listProfiles(userIds));
    }

    /**
     * 获取安全信息。
     */
    @GetMapping("/{userId}/security")
    public ApiResponse<UserSecurityDTO> getSecurity(@PathVariable("userId") Long userId) {
        return ApiResponse.success(userFacade.getSecurity(userId));
    }

    /**
     * 获取设置信息。
     */
    @GetMapping("/{userId}/settings")
    public ApiResponse<UserSettingsDTO> getSettings(@PathVariable("userId") Long userId) {
        return ApiResponse.success(userFacade.getSettings(userId));
    }

    /**
     * 获取初始化信息。
     */
    @GetMapping("/{userId}/init")
    public ApiResponse<UserInitDTO> getInit(@PathVariable("userId") Long userId) {
        return ApiResponse.success(userFacade.getInit(userId));
    }

    /**
     * 查询联系人信息列表。
     */
    @GetMapping("/{userId}/recent-contacts")
    public ApiResponse<List<UserRecentContactDTO>> listRecentContacts(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(userFacade.listRecentContacts(userId, limit));
    }

    /**
     * 更新资料信息。
     */
    @PutMapping("/{userId}/profile")
    public ApiResponse<Void> updateProfile(@PathVariable("userId") Long userId,
                                           @RequestBody UpdateUserProfileRequest request) {
        userFacade.updateProfile(new UpdateUserProfileCommand(
                userId,
                request.nickname(),
                request.avatarUrl(),
                request.mobile(),
                request.gender(),
                request.region(),
                request.birthday()
        ));
        return ApiResponse.success(null);
    }

    /**
     * 更新安全信息。
     */
    @PutMapping("/{userId}/security")
    public ApiResponse<Void> updateSecurity(@PathVariable("userId") Long userId,
                                            @RequestBody UpdateUserSecurityRequest request) {
        userFacade.updateSecurity(new UpdateUserSecurityCommand(
                userId,
                request.loginPasswordSet(),
                request.payPasswordSet(),
                request.biometricEnabled(),
                request.twoFactorMode(),
                request.riskLevel(),
                request.deviceLockEnabled(),
                request.privacyModeEnabled()
        ));
        return ApiResponse.success(null);
    }

    /**
     * 更新业务数据。
     */
    @PutMapping("/{userId}/privacy")
    public ApiResponse<Void> updatePrivacy(@PathVariable("userId") Long userId,
                                           @RequestBody UpdateUserPrivacyRequest request) {
        userFacade.updatePrivacy(new UpdateUserPrivacyCommand(
                userId,
                request.allowSearchByMobile(),
                request.allowSearchByAipayUid(),
                request.hideRealName(),
                request.personalizedRecommendationEnabled()
        ));
        return ApiResponse.success(null);
    }

    private String normalizeMainlandPhone(String rawLoginId) {
        if (rawLoginId == null) {
            throw new IllegalArgumentException("loginId不能为空");
        }
        String normalized = extractAsciiDigits(rawLoginId.trim());
        if (normalized.length() == 13 && normalized.startsWith("86")) {
            normalized = normalized.substring(2);
        } else if (normalized.length() == 15 && normalized.startsWith("0086")) {
            normalized = normalized.substring(4);
        }
        if (!normalized.matches("^1[3-9][0-9]{9}$")) {
            throw new IllegalArgumentException("loginId格式不正确");
        }
        return normalized;
    }

    private String extractAsciiDigits(String raw) {
        StringBuilder digits = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch >= '0' && ch <= '9') {
                digits.append(ch);
            } else if (ch >= '０' && ch <= '９') {
                digits.append((char) ('0' + (ch - '０')));
            }
        }
        return digits.toString();
    }
}
