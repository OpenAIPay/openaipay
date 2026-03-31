package cn.openaipay.domain.app.model;

import java.time.LocalDateTime;

/**
 * iOS 安装包发布模型。
 *
 * 当前版本管理只覆盖 iPhone，因此一个应用版本只对应一个 iOS 包。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public class AppIosPackage {

    /** 主键ID。 */
    private final Long id;
    /** iOS 安装包编码。 */
    private final String iosCode;
    /** 应用编码。 */
    private final String appCode;
    /** 关联版本编码。 */
    private final String versionCode;
    /** App Store 下载地址。 */
    private String appStoreUrl;
    /** 安装包大小（字节）。 */
    private Long packageSizeBytes;
    /** 安装包 MD5 校验值。 */
    private String md5;
    /** 提审时间。 */
    private LocalDateTime reviewSubmittedAt;
    /** 上架时间。 */
    private LocalDateTime publishedAt;
    /** 发布状态。 */
    private AppReleaseStatus releaseStatus;
    /** 提审人。 */
    private String reviewSubmittedBy;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public AppIosPackage(Long id,
                         String iosCode,
                         String appCode,
                         String versionCode,
                         String appStoreUrl,
                         Long packageSizeBytes,
                         String md5,
                         LocalDateTime reviewSubmittedAt,
                         LocalDateTime publishedAt,
                         AppReleaseStatus releaseStatus,
                         String reviewSubmittedBy,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.id = id;
        this.iosCode = AppDomainSupport.normalizeRequired(iosCode, "iosCode");
        this.appCode = AppDomainSupport.normalizeRequired(appCode, "appCode");
        this.versionCode = AppDomainSupport.normalizeRequired(versionCode, "versionCode");
        this.appStoreUrl = AppDomainSupport.normalizeOptional(appStoreUrl);
        this.packageSizeBytes = AppDomainSupport.normalizeNonNegative(packageSizeBytes, "packageSizeBytes");
        this.md5 = AppDomainSupport.normalizeOptional(md5);
        this.reviewSubmittedAt = reviewSubmittedAt;
        this.publishedAt = publishedAt;
        this.releaseStatus = releaseStatus == null ? AppReleaseStatus.DRAFT : releaseStatus;
        this.reviewSubmittedBy = AppDomainSupport.normalizeOptional(reviewSubmittedBy);
        this.createdAt = AppDomainSupport.defaultNow(createdAt);
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 创建业务数据。
     */
    public static AppIosPackage create(String iosCode,
                                       String appCode,
                                       String versionCode,
                                       String appStoreUrl,
                                       Long packageSizeBytes,
                                       String md5,
                                       LocalDateTime now) {
        LocalDateTime createdAt = AppDomainSupport.defaultNow(now);
        return new AppIosPackage(
                null,
                iosCode,
                appCode,
                versionCode,
                appStoreUrl,
                packageSizeBytes,
                md5,
                null,
                null,
                AppReleaseStatus.DRAFT,
                null,
                createdAt,
                createdAt
        );
    }

    /**
     * 刷新业务数据。
     */
    public void refreshDraft(String appStoreUrl, Long packageSizeBytes, String md5, LocalDateTime now) {
        this.appStoreUrl = AppDomainSupport.normalizeOptional(appStoreUrl);
        this.packageSizeBytes = AppDomainSupport.normalizeNonNegative(packageSizeBytes, "packageSizeBytes");
        this.md5 = AppDomainSupport.normalizeOptional(md5);
        this.updatedAt = AppDomainSupport.defaultNow(now);
    }

    /**
     * 提交业务数据。
     */
    public void submitReview(String submittedBy, LocalDateTime now) {
        LocalDateTime submitTime = AppDomainSupport.defaultNow(now);
        this.reviewSubmittedBy = AppDomainSupport.normalizeRequired(submittedBy, "submittedBy");
        this.reviewSubmittedAt = submitTime;
        this.releaseStatus = AppReleaseStatus.REVIEWING;
        this.updatedAt = submitTime;
    }

    /**
     * 发布业务数据。
     */
    public void publish(LocalDateTime now) {
        if (AppDomainSupport.normalizeOptional(appStoreUrl) == null) {
            throw new IllegalStateException("appStoreUrl must not be blank before publish");
        }
        if (packageSizeBytes == null || packageSizeBytes <= 0) {
            throw new IllegalStateException("packageSizeBytes must be positive before publish");
        }
        if (AppDomainSupport.normalizeOptional(md5) == null) {
            throw new IllegalStateException("md5 must not be blank before publish");
        }
        LocalDateTime publishTime = AppDomainSupport.defaultNow(now);
        this.publishedAt = publishTime;
        this.releaseStatus = AppReleaseStatus.PUBLISHED;
        this.updatedAt = publishTime;
    }

    /**
     * 处理业务数据。
     */
    public void reject(LocalDateTime now) {
        this.releaseStatus = AppReleaseStatus.REJECTED;
        this.updatedAt = AppDomainSupport.defaultNow(now);
    }

    /**
     * 处理业务数据。
     */
    public void offline(LocalDateTime now) {
        this.releaseStatus = AppReleaseStatus.OFFLINE;
        this.updatedAt = AppDomainSupport.defaultNow(now);
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取IOS编码。
     */
    public String getIosCode() {
        return iosCode;
    }

    /**
     * 获取应用编码。
     */
    public String getAppCode() {
        return appCode;
    }

    /**
     * 获取版本编码。
     */
    public String getVersionCode() {
        return versionCode;
    }

    /**
     * 获取应用URL信息。
     */
    public String getAppStoreUrl() {
        return appStoreUrl;
    }

    /**
     * 获取业务数据。
     */
    public Long getPackageSizeBytes() {
        return packageSizeBytes;
    }

    /**
     * 获取MD5信息。
     */
    public String getMd5() {
        return md5;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getReviewSubmittedAt() {
        return reviewSubmittedAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    /**
     * 获取状态。
     */
    public AppReleaseStatus getReleaseStatus() {
        return releaseStatus;
    }

    /**
     * 按条件获取记录。
     */
    public String getReviewSubmittedBy() {
        return reviewSubmittedBy;
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
