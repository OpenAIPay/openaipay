package cn.openaipay.application.user.facade.impl;

import cn.openaipay.application.user.command.UpdateUserPrivacyCommand;
import cn.openaipay.application.user.command.UpdateUserProfileCommand;
import cn.openaipay.application.user.command.UpdateUserSecurityCommand;
import cn.openaipay.application.user.dto.UserInitDTO;
import cn.openaipay.application.user.dto.UserProfileDTO;
import cn.openaipay.application.user.dto.UserRecentContactDTO;
import cn.openaipay.application.user.dto.UserSecurityDTO;
import cn.openaipay.application.user.dto.UserSettingsDTO;
import cn.openaipay.application.user.facade.UserFacade;
import cn.openaipay.application.user.service.UserService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 用户门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Service
public class UserFacadeImpl implements UserFacade {

    /** 用户信息 */
    private final UserService userService;

    public UserFacadeImpl(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取资料信息。
     */
    @Override
    public UserProfileDTO getProfile(Long userId) {
        return userService.getProfile(userId);
    }

    /**
     * 按登录账号ID获取资料信息。
     */
    @Override
    public UserProfileDTO getProfileByLoginId(String loginId) {
        return userService.getProfileByLoginId(loginId);
    }

    /**
     * 查询资料信息列表。
     */
    @Override
    public List<UserProfileDTO> listProfiles(List<Long> userIds) {
        return userService.listProfiles(userIds);
    }

    /**
     * 获取安全信息。
     */
    @Override
    public UserSecurityDTO getSecurity(Long userId) {
        return userService.getSecurity(userId);
    }

    /**
     * 获取设置信息。
     */
    @Override
    public UserSettingsDTO getSettings(Long userId) {
        return userService.getSettings(userId);
    }

    /**
     * 获取初始化信息。
     */
    @Override
    public UserInitDTO getInit(Long userId) {
        return userService.getInit(userId);
    }

    /**
     * 查询联系人信息列表。
     */
    @Override
    public List<UserRecentContactDTO> listRecentContacts(Long userId, Integer limit) {
        return userService.listRecentContacts(userId, limit);
    }

    /**
     * 更新资料信息。
     */
    @Override
    public void updateProfile(UpdateUserProfileCommand command) {
        userService.updateProfile(command);
    }

    /**
     * 更新安全信息。
     */
    @Override
    public void updateSecurity(UpdateUserSecurityCommand command) {
        userService.updateSecurity(command);
    }

    /**
     * 更新业务数据。
     */
    @Override
    public void updatePrivacy(UpdateUserPrivacyCommand command) {
        userService.updatePrivacy(command);
    }
}
