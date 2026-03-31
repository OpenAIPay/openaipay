package cn.openaipay.infrastructure.app;

import cn.openaipay.domain.app.model.AppClientType;
import cn.openaipay.domain.app.model.AppUpdatePromptFrequency;
import cn.openaipay.domain.app.model.AppUpdateType;
import cn.openaipay.domain.app.model.AppVersion;
import cn.openaipay.domain.app.model.AppVersionStatus;
import cn.openaipay.domain.app.repository.AppVersionRepository;
import cn.openaipay.infrastructure.app.dataobject.AppVersionDO;
import cn.openaipay.infrastructure.app.mapper.AppVersionMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 应用版本仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Repository
public class AppVersionRepositoryImpl implements AppVersionRepository {

    /** 应用版本信息 */
    private final AppVersionMapper appVersionMapper;

    public AppVersionRepositoryImpl(AppVersionMapper appVersionMapper) {
        this.appVersionMapper = appVersionMapper;
    }

    /**
     * 保存业务数据。
     */
    @Override
    public AppVersion save(AppVersion appVersion) {
        AppVersionDO entity = resolveDO(appVersion);
        entity.setVersionCode(appVersion.getVersionCode());
        entity.setAppCode(appVersion.getAppCode());
        entity.setClientType(appVersion.getClientType().name());
        entity.setAppVersionNo(appVersion.getAppVersionNo());
        entity.setUpdateType(appVersion.getUpdateType().name());
        entity.setUpdatePromptFrequency(appVersion.getUpdatePromptFrequency().name());
        entity.setVersionDescription(appVersion.getVersionDescription());
        entity.setPublisherRemark(appVersion.getPublisherRemark());
        entity.setReleaseRegionListText(writeTextList(appVersion.getReleaseRegions()));
        entity.setTargetRegionListText(writeTextList(appVersion.getTargetedRegions()));
        entity.setMinSupportedVersionNo(appVersion.getMinSupportedVersionNo());
        entity.setLatestPublishedVersion(appVersion.isLatestPublishedVersion());
        entity.setStatus(appVersion.getStatus().name());
        entity.setCreatedAt(appVersion.getCreatedAt());
        entity.setUpdatedAt(appVersion.getUpdatedAt());
        return toDomain(appVersionMapper.save(entity));
    }

    /**
     * 按版本编码查找记录。
     */
    @Override
    public Optional<AppVersion> findByVersionCode(String versionCode) {
        return appVersionMapper.findByVersionCode(versionCode).map(this::toDomain);
    }

    /**
     * 按应用编码查询记录列表。
     */
    @Override
    public List<AppVersion> listByAppCode(String appCode, AppClientType clientType) {
        return appVersionMapper.findByAppCode(appCode, clientType == null ? null : clientType.name())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * 查找版本信息。
     */
    @Override
    public Optional<AppVersion> findLatestPublishedVersion(String appCode, AppClientType clientType) {
        return appVersionMapper.findLatestPublishedVersion(appCode, clientType == null ? null : clientType.name())
                .map(this::toDomain);
    }

    private AppVersionDO resolveDO(AppVersion appVersion) {
        if (appVersion.getId() != null) {
            return appVersionMapper.findById(appVersion.getId()).orElse(new AppVersionDO());
        }
        return appVersionMapper.findByVersionCode(appVersion.getVersionCode()).orElse(new AppVersionDO());
    }

    private AppVersion toDomain(AppVersionDO entity) {
        return new AppVersion(
                entity.getId(),
                entity.getVersionCode(),
                entity.getAppCode(),
                AppClientType.fromCode(entity.getClientType()),
                entity.getAppVersionNo(),
                AppUpdateType.fromCode(entity.getUpdateType()),
                AppUpdatePromptFrequency.fromCode(entity.getUpdatePromptFrequency()),
                entity.getVersionDescription(),
                entity.getPublisherRemark(),
                readTextList(entity.getReleaseRegionListText()),
                readTextList(entity.getTargetRegionListText()),
                entity.getMinSupportedVersionNo(),
                Boolean.TRUE.equals(entity.getLatestPublishedVersion()),
                AppVersionStatus.fromCode(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String writeTextList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return String.join("\n", values);
    }

    private List<String> readTextList(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split("\\n"))
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .toList();
    }
}
