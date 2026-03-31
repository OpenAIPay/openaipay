package cn.openaipay.infrastructure.app;

import cn.openaipay.domain.app.model.AppIosPackage;
import cn.openaipay.domain.app.model.AppReleaseStatus;
import cn.openaipay.domain.app.repository.AppIosPackageRepository;
import cn.openaipay.infrastructure.app.dataobject.AppIosPackageDO;
import cn.openaipay.infrastructure.app.mapper.AppIosPackageMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * iOS 包仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Repository
public class AppIosPackageRepositoryImpl implements AppIosPackageRepository {

    /** 应用IOS信息 */
    private final AppIosPackageMapper appIosPackageMapper;

    public AppIosPackageRepositoryImpl(AppIosPackageMapper appIosPackageMapper) {
        this.appIosPackageMapper = appIosPackageMapper;
    }

    /**
     * 保存业务数据。
     */
    @Override
    public AppIosPackage save(AppIosPackage appIosPackage) {
        AppIosPackageDO entity = resolveDO(appIosPackage);
        entity.setIosCode(appIosPackage.getIosCode());
        entity.setAppCode(appIosPackage.getAppCode());
        entity.setVersionCode(appIosPackage.getVersionCode());
        entity.setAppStoreUrl(appIosPackage.getAppStoreUrl());
        entity.setPackageSizeBytes(appIosPackage.getPackageSizeBytes());
        entity.setMd5(appIosPackage.getMd5());
        entity.setReviewSubmittedAt(appIosPackage.getReviewSubmittedAt());
        entity.setPublishedAt(appIosPackage.getPublishedAt());
        entity.setReleaseStatus(appIosPackage.getReleaseStatus().name());
        entity.setReviewSubmittedBy(appIosPackage.getReviewSubmittedBy());
        entity.setCreatedAt(appIosPackage.getCreatedAt());
        entity.setUpdatedAt(appIosPackage.getUpdatedAt());
        return toDomain(appIosPackageMapper.save(entity));
    }

    /**
     * 按IOS编码查找记录。
     */
    @Override
    public Optional<AppIosPackage> findByIosCode(String iosCode) {
        return appIosPackageMapper.findByIosCode(iosCode).map(this::toDomain);
    }

    /**
     * 按版本编码查找记录。
     */
    @Override
    public Optional<AppIosPackage> findByVersionCode(String versionCode) {
        return appIosPackageMapper.findByVersionCode(versionCode).map(this::toDomain);
    }

    private AppIosPackageDO resolveDO(AppIosPackage appIosPackage) {
        if (appIosPackage.getId() != null) {
            return appIosPackageMapper.findById(appIosPackage.getId()).orElse(new AppIosPackageDO());
        }
        return appIosPackageMapper.findByVersionCode(appIosPackage.getVersionCode()).orElse(new AppIosPackageDO());
    }

    private AppIosPackage toDomain(AppIosPackageDO entity) {
        return new AppIosPackage(
                entity.getId(),
                entity.getIosCode(),
                entity.getAppCode(),
                entity.getVersionCode(),
                entity.getAppStoreUrl(),
                entity.getPackageSizeBytes(),
                entity.getMd5(),
                entity.getReviewSubmittedAt(),
                entity.getPublishedAt(),
                AppReleaseStatus.fromCode(entity.getReleaseStatus()),
                entity.getReviewSubmittedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
