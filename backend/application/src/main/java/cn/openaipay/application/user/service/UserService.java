package cn.openaipay.application.user.service;

import cn.openaipay.application.user.command.CreateUserCommand;
import cn.openaipay.application.user.command.UpdateUserPrivacyCommand;
import cn.openaipay.application.user.command.UpdateUserProfileCommand;
import cn.openaipay.application.user.command.UpdateUserSecurityCommand;
import cn.openaipay.application.user.dto.UserInitDTO;
import cn.openaipay.application.user.dto.UserProfileDTO;
import cn.openaipay.application.user.dto.UserRecentContactDTO;
import cn.openaipay.application.user.dto.UserSecurityDTO;
import cn.openaipay.application.user.dto.UserSettingsDTO;

import java.util.List;

/**
 * 用户应用服务接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface UserService {

    /**
     * 创建用户主数据。
     */
    Long createCoreUser(CreateUserCommand command);

    /**
     * 获取资料信息。
     */
    UserProfileDTO getProfile(Long userId);

    /**
     * 按登录账号ID获取资料信息。
     */
    UserProfileDTO getProfileByLoginId(String loginId);

    /**
     * 查询资料信息列表。
     */
    List<UserProfileDTO> listProfiles(List<Long> userIds);

    /**
     * 获取安全信息。
     */
    UserSecurityDTO getSecurity(Long userId);

    /**
     * 获取设置信息。
     */
    UserSettingsDTO getSettings(Long userId);

    /**
     * 获取初始化信息。
     */
    UserInitDTO getInit(Long userId);

    /**
     * 查询联系人信息列表。
     */
    List<UserRecentContactDTO> listRecentContacts(Long userId, Integer limit);

    /**
     * 更新资料信息。
     */
    void updateProfile(UpdateUserProfileCommand command);

    /**
     * 更新安全信息。
     */
    void updateSecurity(UpdateUserSecurityCommand command);

    /**
     * 更新业务数据。
     */
    void updatePrivacy(UpdateUserPrivacyCommand command);
}
