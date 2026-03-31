package cn.openaipay.domain.app.model;

import java.time.LocalDateTime;

/**
 * 应用定义模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public class AppInfo {

    /** 主键ID。 */
    private final Long id;
    /** 应用编码。 */
    private final String appCode;
    /** 应用名称。 */
    private String appName;
    /** 应用状态。 */
    private AppStatus status;
    /** 版本提示开关。 */
    private boolean versionPromptEnabled;
    /** 演示账号自动登录开关。 */
    private boolean demoAutoLoginEnabled;
    /** 登录本机注册校验开关。 */
    private boolean loginDeviceBindingCheckEnabled;
    /** 演示模板登录号。 */
    private String demoTemplateLoginId;
    /** 演示联系人登录号。 */
    private String demoContactLoginId;
    /** 演示注册默认密码。 */
    private String demoLoginPassword;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public AppInfo(Long id,
                   String appCode,
                   String appName,
                   AppStatus status,
                   boolean versionPromptEnabled,
                   boolean demoAutoLoginEnabled,
                   boolean loginDeviceBindingCheckEnabled,
                   String demoTemplateLoginId,
                   String demoContactLoginId,
                   String demoLoginPassword,
                   LocalDateTime createdAt,
                   LocalDateTime updatedAt) {
        this.id = id;
        this.appCode = AppDomainSupport.normalizeRequired(appCode, "appCode");
        this.appName = AppDomainSupport.normalizeRequired(appName, "appName");
        this.status = status == null ? AppStatus.ENABLED : status;
        this.versionPromptEnabled = versionPromptEnabled;
        this.demoAutoLoginEnabled = demoAutoLoginEnabled;
        this.loginDeviceBindingCheckEnabled = loginDeviceBindingCheckEnabled;
        this.demoTemplateLoginId = AppDomainSupport.normalizeOptional(demoTemplateLoginId);
        this.demoContactLoginId = AppDomainSupport.normalizeOptional(demoContactLoginId);
        this.demoLoginPassword = AppDomainSupport.normalizeOptional(demoLoginPassword);
        this.createdAt = AppDomainSupport.defaultNow(createdAt);
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 创建业务数据。
     */
    public static AppInfo create(String appCode, String appName, LocalDateTime now) {
        LocalDateTime createdAt = AppDomainSupport.defaultNow(now);
        return new AppInfo(null, appCode, appName, AppStatus.ENABLED, false, true, true, null, null, null, createdAt, createdAt);
    }

    /**
     * 处理业务数据。
     */
    public void rename(String appName, LocalDateTime now) {
        this.appName = AppDomainSupport.normalizeRequired(appName, "appName");
        this.updatedAt = AppDomainSupport.defaultNow(now);
    }

    /**
     * 处理状态。
     */
    public void changeStatus(AppStatus targetStatus, LocalDateTime now) {
        this.status = targetStatus == null ? AppStatus.ENABLED : targetStatus;
        this.updatedAt = AppDomainSupport.defaultNow(now);
    }

    /**
     * 处理版本信息。
     */
    public void changeVersionPromptEnabled(boolean targetEnabled, LocalDateTime now) {
        this.versionPromptEnabled = targetEnabled;
        this.updatedAt = AppDomainSupport.defaultNow(now);
    }

    /**
     * 处理演示自动登录开关。
     */
    public void changeDemoAutoLoginEnabled(boolean targetEnabled, LocalDateTime now) {
        this.demoAutoLoginEnabled = targetEnabled;
        this.updatedAt = AppDomainSupport.defaultNow(now);
    }

    /**
     * 处理登录本机注册校验开关。
     */
    public void changeLoginDeviceBindingCheckEnabled(boolean targetEnabled, LocalDateTime now) {
        this.loginDeviceBindingCheckEnabled = targetEnabled;
        this.updatedAt = AppDomainSupport.defaultNow(now);
    }

    /**
     * 处理演示账号配置。
     */
    public void changeDemoProvisioningConfig(String demoTemplateLoginId,
                                             String demoContactLoginId,
                                             String demoLoginPassword,
                                             LocalDateTime now) {
        this.demoTemplateLoginId = AppDomainSupport.normalizeOptional(demoTemplateLoginId);
        this.demoContactLoginId = AppDomainSupport.normalizeOptional(demoContactLoginId);
        this.demoLoginPassword = AppDomainSupport.normalizeOptional(demoLoginPassword);
        this.updatedAt = AppDomainSupport.defaultNow(now);
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取应用编码。
     */
    public String getAppCode() {
        return appCode;
    }

    /**
     * 获取应用信息。
     */
    public String getAppName() {
        return appName;
    }

    /**
     * 获取状态。
     */
    public AppStatus getStatus() {
        return status;
    }

    /**
     * 判断是否版本信息。
     */
    public boolean isVersionPromptEnabled() {
        return versionPromptEnabled;
    }

    /**
     * 判断是否启用演示自动登录。
     */
    public boolean isDemoAutoLoginEnabled() {
        return demoAutoLoginEnabled;
    }

    /**
     * 判断是否启用登录本机注册校验。
     */
    public boolean isLoginDeviceBindingCheckEnabled() {
        return loginDeviceBindingCheckEnabled;
    }

    /**
     * 获取演示模板登录号。
     */
    public String getDemoTemplateLoginId() {
        return demoTemplateLoginId;
    }

    /**
     * 获取演示联系人登录号。
     */
    public String getDemoContactLoginId() {
        return demoContactLoginId;
    }

    /**
     * 获取演示默认密码。
     */
    public String getDemoLoginPassword() {
        return demoLoginPassword;
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
}
