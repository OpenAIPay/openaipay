package cn.openaipay.domain.app.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用版本模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public class AppVersion {

    /** 主键ID。 */
    private final Long id;
    /** 版本编码。 */
    private final String versionCode;
    /** 应用编码。 */
    private final String appCode;
    /** 客户端类型。 */
    private final AppClientType clientType;
    /** 应用版本号。 */
    private final String appVersionNo;
    /** 更新类型。 */
    private AppUpdateType updateType;
    /** 更新提示频率。 */
    private AppUpdatePromptFrequency updatePromptFrequency;
    /** 用户提示信息（客户端弹窗文案）。 */
    private String versionDescription;
    /** 发布者备注，仅后台可见。 */
    private String publisherRemark;
    /** 正式发布区域列表。 */
    private final List<String> releaseRegions;
    /** 灰度定向区域列表。 */
    private final List<String> targetedRegions;
    /** 最低支持版本号。 */
    private String minSupportedVersionNo;
    /** 是否当前最新已发布版本。 */
    private boolean latestPublishedVersion;
    /** 版本状态。 */
    private AppVersionStatus status;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public AppVersion(Long id,
                      String versionCode,
                      String appCode,
                      AppClientType clientType,
                      String appVersionNo,
                      AppUpdateType updateType,
                      AppUpdatePromptFrequency updatePromptFrequency,
                      String versionDescription,
                      String publisherRemark,
                      List<String> releaseRegions,
                      List<String> targetedRegions,
                      String minSupportedVersionNo,
                      boolean latestPublishedVersion,
                      AppVersionStatus status,
                      LocalDateTime createdAt,
                      LocalDateTime updatedAt) {
        this.id = id;
        this.versionCode = AppDomainSupport.normalizeRequired(versionCode, "versionCode");
        this.appCode = AppDomainSupport.normalizeRequired(appCode, "appCode");
        this.clientType = clientType == null ? AppClientType.IOS_IPHONE : clientType;
        this.appVersionNo = AppDomainSupport.normalizeRequired(appVersionNo, "appVersionNo");
        this.updateType = updateType == null ? AppUpdateType.OPTIONAL : updateType;
        this.updatePromptFrequency = updatePromptFrequency == null ? AppUpdatePromptFrequency.ONCE_PER_VERSION : updatePromptFrequency;
        this.versionDescription = AppDomainSupport.normalizeOptional(versionDescription);
        this.publisherRemark = AppDomainSupport.normalizeOptional(publisherRemark);
        this.releaseRegions = AppDomainSupport.normalizeTextList(releaseRegions);
        this.targetedRegions = AppDomainSupport.normalizeTextList(targetedRegions);
        this.minSupportedVersionNo = AppDomainSupport.normalizeOptional(minSupportedVersionNo);
        this.latestPublishedVersion = latestPublishedVersion;
        this.status = status == null ? AppVersionStatus.DRAFT : status;
        this.createdAt = AppDomainSupport.defaultNow(createdAt);
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 创建业务数据。
     */
    public static AppVersion create(String versionCode,
                                    String appCode,
                                    String appVersionNo,
                                    AppUpdateType updateType,
                                    AppUpdatePromptFrequency updatePromptFrequency,
                                    String versionDescription,
                                    String publisherRemark,
                                    List<String> releaseRegions,
                                    List<String> targetedRegions,
                                    String minSupportedVersionNo,
                                    LocalDateTime now) {
        LocalDateTime createdAt = AppDomainSupport.defaultNow(now);
        return new AppVersion(
                null,
                versionCode,
                appCode,
                AppClientType.IOS_IPHONE,
                appVersionNo,
                updateType,
                updatePromptFrequency,
                versionDescription,
                publisherRemark,
                releaseRegions,
                targetedRegions,
                minSupportedVersionNo,
                false,
                AppVersionStatus.DRAFT,
                createdAt,
                createdAt
        );
    }

    /**
     * 处理状态。
     */
    public void changeStatus(AppVersionStatus targetStatus, LocalDateTime now) {
        this.status = targetStatus == null ? AppVersionStatus.DRAFT : targetStatus;
        this.updatedAt = AppDomainSupport.defaultNow(now);
    }

    /**
     * 刷新业务数据。
     */
    public void refreshUpdatePolicy(AppUpdateType updateType,
                                    AppUpdatePromptFrequency promptFrequency,
                                    String minSupportedVersionNo,
                                    String versionDescription,
                                    String publisherRemark,
                                    LocalDateTime now) {
        this.updateType = updateType == null ? AppUpdateType.OPTIONAL : updateType;
        this.updatePromptFrequency = promptFrequency == null ? AppUpdatePromptFrequency.ONCE_PER_VERSION : promptFrequency;
        this.minSupportedVersionNo = AppDomainSupport.normalizeOptional(minSupportedVersionNo);
        this.versionDescription = AppDomainSupport.normalizeOptional(versionDescription);
        this.publisherRemark = AppDomainSupport.normalizeOptional(publisherRemark);
        this.updatedAt = AppDomainSupport.defaultNow(now);
    }

    /**
     * 标记版本信息。
     */
    public void markLatestPublishedVersion(boolean latestPublishedVersion, LocalDateTime now) {
        this.latestPublishedVersion = latestPublishedVersion;
        this.updatedAt = AppDomainSupport.defaultNow(now);
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取版本编码。
     */
    public String getVersionCode() {
        return versionCode;
    }

    /**
     * 获取应用编码。
     */
    public String getAppCode() {
        return appCode;
    }

    /**
     * 获取客户端类型信息。
     */
    public AppClientType getClientType() {
        return clientType;
    }

    /**
     * 获取应用版本NO信息。
     */
    public String getAppVersionNo() {
        return appVersionNo;
    }

    /**
     * 获取业务数据。
     */
    public AppUpdateType getUpdateType() {
        return updateType;
    }

    /**
     * 获取业务数据。
     */
    public AppUpdatePromptFrequency getUpdatePromptFrequency() {
        return updatePromptFrequency;
    }

    /**
     * 获取版本信息。
     */
    public String getVersionDescription() {
        return versionDescription;
    }

    /**
     * 获取发布者备注信息。
     */
    public String getPublisherRemark() {
        return publisherRemark;
    }

    /**
     * 获取业务数据。
     */
    public List<String> getReleaseRegions() {
        return releaseRegions;
    }

    /**
     * 获取业务数据。
     */
    public List<String> getTargetedRegions() {
        return targetedRegions;
    }

    /**
     * 获取MIN版本NO信息。
     */
    public String getMinSupportedVersionNo() {
        return minSupportedVersionNo;
    }

    /**
     * 判断是否版本信息。
     */
    public boolean isLatestPublishedVersion() {
        return latestPublishedVersion;
    }

    /**
     * 获取状态。
     */
    public AppVersionStatus getStatus() {
        return status;
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
