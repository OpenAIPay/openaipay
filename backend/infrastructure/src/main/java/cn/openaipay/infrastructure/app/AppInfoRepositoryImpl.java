package cn.openaipay.infrastructure.app;

import cn.openaipay.domain.app.model.AppInfo;
import cn.openaipay.domain.app.model.AppStatus;
import cn.openaipay.domain.app.repository.AppInfoRepository;
import cn.openaipay.infrastructure.app.dataobject.AppInfoDO;
import cn.openaipay.infrastructure.app.mapper.AppInfoMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 应用定义仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Repository
public class AppInfoRepositoryImpl implements AppInfoRepository {

    /** 应用信息 */
    private final AppInfoMapper appInfoMapper;

    public AppInfoRepositoryImpl(AppInfoMapper appInfoMapper) {
        this.appInfoMapper = appInfoMapper;
    }

    /**
     * 保存业务数据。
     */
    @Override
    public AppInfo save(AppInfo appInfo) {
        AppInfoDO entity = resolveDO(appInfo);
        entity.setAppCode(appInfo.getAppCode());
        entity.setAppName(appInfo.getAppName());
        entity.setStatus(appInfo.getStatus().name());
        entity.setVersionPromptEnabled(appInfo.isVersionPromptEnabled());
        entity.setDemoAutoLoginEnabled(appInfo.isDemoAutoLoginEnabled());
        entity.setLoginDeviceBindingCheckEnabled(appInfo.isLoginDeviceBindingCheckEnabled());
        entity.setDemoTemplateLoginId(appInfo.getDemoTemplateLoginId());
        entity.setDemoContactLoginId(appInfo.getDemoContactLoginId());
        entity.setDemoLoginPassword(appInfo.getDemoLoginPassword());
        entity.setCreatedAt(appInfo.getCreatedAt());
        entity.setUpdatedAt(appInfo.getUpdatedAt());
        return toDomain(appInfoMapper.save(entity));
    }

    /**
     * 按应用编码查找记录。
     */
    @Override
    public Optional<AppInfo> findByAppCode(String appCode) {
        return appInfoMapper.findByAppCode(appCode).map(this::toDomain);
    }

    /**
     * 查找ALL信息。
     */
    @Override
    public List<AppInfo> findAll() {
        return appInfoMapper.findAll().stream().map(this::toDomain).toList();
    }

    private AppInfoDO resolveDO(AppInfo appInfo) {
        if (appInfo.getId() != null) {
            return appInfoMapper.findById(appInfo.getId()).orElse(new AppInfoDO());
        }
        return appInfoMapper.findByAppCode(appInfo.getAppCode()).orElse(new AppInfoDO());
    }

    private AppInfo toDomain(AppInfoDO entity) {
        return new AppInfo(
                entity.getId(),
                entity.getAppCode(),
                entity.getAppName(),
                AppStatus.fromCode(entity.getStatus()),
                Boolean.TRUE.equals(entity.getVersionPromptEnabled()),
                entity.getDemoAutoLoginEnabled() == null || Boolean.TRUE.equals(entity.getDemoAutoLoginEnabled()),
                entity.getLoginDeviceBindingCheckEnabled() == null || Boolean.TRUE.equals(entity.getLoginDeviceBindingCheckEnabled()),
                entity.getDemoTemplateLoginId(),
                entity.getDemoContactLoginId(),
                entity.getDemoLoginPassword(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
