package cn.openaipay.domain.app.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * App 设备安装与活跃模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public class AppDevice {

    /** 主键ID。 */
    private final Long id;
    /** 设备唯一标识。 */
    private final String deviceId;
    /** 应用编码。 */
    private final String appCode;
    /** 设备归属的客户端ID列表。 */
    private List<String> clientIds;
    /** 设备状态。 */
    private AppDeviceStatus status;
    /** 安装时间。 */
    private final LocalDateTime installedAt;
    /** 最近启动时间。 */
    private LocalDateTime startedAt;
    /** 最近打开时间。 */
    private LocalDateTime lastOpenedAt;
    /** 当前绑定的应用版本ID。 */
    private Long currentAppVersionId;
    /** 当前绑定的 iOS 安装包ID。 */
    private Long currentIosPackageId;
    /** 应用版本最近更新时间。 */
    private LocalDateTime appUpdatedAt;
    /** 设备品牌。 */
    private String deviceBrand;
    /** 系统版本号。 */
    private String osVersion;
    /** 当前登录用户ID。 */
    private Long userId;
    /** 当前登录爱付号。 */
    private String aipayUid;
    /** 当前登录账号。 */
    private String loginId;
    /** 账户状态。 */
    private String accountStatus;
    /** 实名等级。 */
    private String kycLevel;
    /** 昵称。 */
    private String nickname;
    /** 头像地址。 */
    private String avatarUrl;
    /** 手机号。 */
    private String mobile;
    /** 脱敏姓名。 */
    private String maskedRealName;
    /** 脱敏证件号。 */
    private String idCardNoMasked;
    /** 国家编码。 */
    private String countryCode;
    /** 性别。 */
    private String gender;
    /** 地区。 */
    private String region;
    /** 最近登录时间。 */
    private LocalDateTime lastLoginAt;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public AppDevice(Long id,
                     String deviceId,
                     String appCode,
                     List<String> clientIds,
                     AppDeviceStatus status,
                     LocalDateTime installedAt,
                     LocalDateTime startedAt,
                     LocalDateTime lastOpenedAt,
                     Long currentAppVersionId,
                     Long currentIosPackageId,
                     LocalDateTime appUpdatedAt,
                     String deviceBrand,
                     String osVersion,
                     Long userId,
                     String aipayUid,
                     String loginId,
                     String accountStatus,
                     String kycLevel,
                     String nickname,
                     String avatarUrl,
                     String mobile,
                     String maskedRealName,
                     String idCardNoMasked,
                     String countryCode,
                     String gender,
                     String region,
                     LocalDateTime lastLoginAt,
                     LocalDateTime createdAt,
                     LocalDateTime updatedAt) {
        this.id = id;
        this.deviceId = AppDomainSupport.normalizeRequired(deviceId, "deviceId");
        this.appCode = AppDomainSupport.normalizeRequired(appCode, "appCode");
        this.clientIds = AppDomainSupport.normalizeTextList(clientIds);
        this.status = status == null ? AppDeviceStatus.INSTALLED : status;
        this.installedAt = AppDomainSupport.defaultNow(installedAt);
        this.startedAt = startedAt;
        this.lastOpenedAt = lastOpenedAt;
        this.currentAppVersionId = currentAppVersionId;
        this.currentIosPackageId = currentIosPackageId;
        this.appUpdatedAt = appUpdatedAt;
        this.deviceBrand = AppDomainSupport.defaultText(deviceBrand, "APPLE");
        this.osVersion = AppDomainSupport.normalizeOptional(osVersion);
        this.userId = normalizeUserId(userId);
        this.aipayUid = AppDomainSupport.normalizeOptional(aipayUid);
        this.loginId = AppDomainSupport.normalizeOptional(loginId);
        this.accountStatus = AppDomainSupport.normalizeOptional(accountStatus);
        this.kycLevel = AppDomainSupport.normalizeOptional(kycLevel);
        this.nickname = AppDomainSupport.normalizeOptional(nickname);
        this.avatarUrl = AppDomainSupport.normalizeOptional(avatarUrl);
        this.mobile = AppDomainSupport.normalizeOptional(mobile);
        this.maskedRealName = AppDomainSupport.normalizeOptional(maskedRealName);
        this.idCardNoMasked = AppDomainSupport.normalizeOptional(idCardNoMasked);
        this.countryCode = AppDomainSupport.normalizeOptional(countryCode);
        this.gender = AppDomainSupport.normalizeOptional(gender);
        this.region = AppDomainSupport.normalizeOptional(region);
        this.lastLoginAt = lastLoginAt;
        this.createdAt = AppDomainSupport.defaultNow(createdAt);
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 处理业务数据。
     */
    public static AppDevice register(String deviceId,
                                     String appCode,
                                     List<String> clientIds,
                                     String deviceBrand,
                                     String osVersion,
                                     LocalDateTime now) {
        LocalDateTime createdAt = AppDomainSupport.defaultNow(now);
        return new AppDevice(
                null,
                deviceId,
                appCode,
                clientIds,
                AppDeviceStatus.INSTALLED,
                createdAt,
                null,
                null,
                null,
                null,
                null,
                deviceBrand,
                osVersion,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                createdAt,
                createdAt
        );
    }

    /**
     * 刷新业务数据。
     */
    public void touchStarted(LocalDateTime now) {
        LocalDateTime startedTime = AppDomainSupport.defaultNow(now);
        this.startedAt = startedTime;
        this.lastOpenedAt = startedTime;
        this.status = AppDeviceStatus.ACTIVE;
        this.updatedAt = startedTime;
    }

    /**
     * 刷新业务数据。
     */
    public void touchOpened(LocalDateTime now) {
        LocalDateTime openedAt = AppDomainSupport.defaultNow(now);
        this.lastOpenedAt = openedAt;
        this.status = AppDeviceStatus.ACTIVE;
        this.updatedAt = openedAt;
    }

    /**
     * 刷新设备资料信息。
     */
    public void refreshDeviceProfile(List<String> clientIds, String deviceBrand, String osVersion, LocalDateTime now) {
        LocalDateTime operateTime = AppDomainSupport.defaultNow(now);
        this.clientIds = AppDomainSupport.normalizeTextList(clientIds);
        this.deviceBrand = AppDomainSupport.defaultText(deviceBrand, "APPLE");
        this.osVersion = AppDomainSupport.normalizeOptional(osVersion);
        this.updatedAt = operateTime;
    }

    /**
     * 处理当前版本信息。
     */
    public void bindCurrentVersion(Long currentAppVersionId, Long currentIosPackageId, LocalDateTime now) {
        if (currentAppVersionId != null && currentAppVersionId <= 0) {
            throw new IllegalArgumentException("currentAppVersionId must be positive");
        }
        if (currentIosPackageId != null && currentIosPackageId <= 0) {
            throw new IllegalArgumentException("currentIosPackageId must be positive");
        }
        LocalDateTime operateTime = AppDomainSupport.defaultNow(now);
        this.currentAppVersionId = currentAppVersionId;
        this.currentIosPackageId = currentIosPackageId;
        this.appUpdatedAt = operateTime;
        this.updatedAt = operateTime;
    }

    /**
     * 处理登录用户信息。
     */
    public void bindLoginUser(Long userId,
                              String aipayUid,
                              String loginId,
                              String accountStatus,
                              String kycLevel,
                              String nickname,
                              String avatarUrl,
                              String mobile,
                              String maskedRealName,
                              String idCardNoMasked,
                              String countryCode,
                              String gender,
                              String region,
                              LocalDateTime now) {
        LocalDateTime loginAt = AppDomainSupport.defaultNow(now);
        this.userId = normalizeUserId(userId);
        this.aipayUid = AppDomainSupport.normalizeOptional(aipayUid);
        this.loginId = AppDomainSupport.normalizeOptional(loginId);
        this.accountStatus = AppDomainSupport.normalizeOptional(accountStatus);
        this.kycLevel = AppDomainSupport.normalizeOptional(kycLevel);
        this.nickname = AppDomainSupport.normalizeOptional(nickname);
        this.avatarUrl = AppDomainSupport.normalizeOptional(avatarUrl);
        this.mobile = AppDomainSupport.normalizeOptional(mobile);
        this.maskedRealName = AppDomainSupport.normalizeOptional(maskedRealName);
        this.idCardNoMasked = AppDomainSupport.normalizeOptional(idCardNoMasked);
        this.countryCode = AppDomainSupport.normalizeOptional(countryCode);
        this.gender = AppDomainSupport.normalizeOptional(gender);
        this.region = AppDomainSupport.normalizeOptional(region);
        this.lastLoginAt = loginAt;
        this.updatedAt = loginAt;
    }

    /**
     * 处理状态。
     */
    public void changeStatus(AppDeviceStatus status, LocalDateTime now) {
        this.status = status == null ? AppDeviceStatus.INSTALLED : status;
        this.updatedAt = AppDomainSupport.defaultNow(now);
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取设备ID。
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 获取应用编码。
     */
    public String getAppCode() {
        return appCode;
    }

    /**
     * 获取客户端IDS信息。
     */
    public List<String> getClientIds() {
        return clientIds;
    }

    /**
     * 获取状态。
     */
    public AppDeviceStatus getStatus() {
        return status;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getInstalledAt() {
        return installedAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getLastOpenedAt() {
        return lastOpenedAt;
    }

    /**
     * 获取当前应用版本ID。
     */
    public Long getCurrentAppVersionId() {
        return currentAppVersionId;
    }

    /**
     * 获取当前IOSID。
     */
    public Long getCurrentIosPackageId() {
        return currentIosPackageId;
    }

    /**
     * 获取应用AT信息。
     */
    public LocalDateTime getAppUpdatedAt() {
        return appUpdatedAt;
    }

    /**
     * 获取设备信息。
     */
    public String getDeviceBrand() {
        return deviceBrand;
    }

    /**
     * 获取OS版本信息。
     */
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取UID。
     */
    public String getAipayUid() {
        return aipayUid;
    }

    /**
     * 获取登录ID。
     */
    public String getLoginId() {
        return loginId;
    }

    /**
     * 获取账户状态。
     */
    public String getAccountStatus() {
        return accountStatus;
    }

    /**
     * 获取KYC信息。
     */
    public String getKycLevel() {
        return kycLevel;
    }

    /**
     * 获取业务数据。
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 获取URL信息。
     */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * 获取手机号信息。
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * 获取业务数据。
     */
    public String getMaskedRealName() {
        return maskedRealName;
    }

    /**
     * 获取IDNO信息。
     */
    public String getIdCardNoMasked() {
        return idCardNoMasked;
    }

    /**
     * 获取编码。
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * 获取业务数据。
     */
    public String getGender() {
        return gender;
    }

    /**
     * 获取业务数据。
     */
    public String getRegion() {
        return region;
    }

    /**
     * 获取登录AT信息。
     */
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private Long normalizeUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return userId;
    }
}
