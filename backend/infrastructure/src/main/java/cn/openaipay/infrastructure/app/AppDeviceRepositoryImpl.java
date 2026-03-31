package cn.openaipay.infrastructure.app;

import cn.openaipay.domain.app.model.AppDevice;
import cn.openaipay.domain.app.model.AppDeviceStatus;
import cn.openaipay.domain.app.repository.AppDeviceRepository;
import cn.openaipay.infrastructure.app.dataobject.AppDeviceDO;
import cn.openaipay.infrastructure.app.mapper.AppDeviceMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

/**
 * App 设备仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Repository
public class AppDeviceRepositoryImpl implements AppDeviceRepository {

    /** 应用设备信息 */
    private final AppDeviceMapper appDeviceMapper;

    public AppDeviceRepositoryImpl(AppDeviceMapper appDeviceMapper) {
        this.appDeviceMapper = appDeviceMapper;
    }

    /**
     * 保存业务数据。
     */
    @Override
    public AppDevice save(AppDevice device) {
        AppDeviceDO entity = resolveDO(device);
        entity.setDeviceId(device.getDeviceId());
        entity.setAppCode(device.getAppCode());
        entity.setClientIdListText(writeTextList(device.getClientIds()));
        entity.setStatus(device.getStatus().name());
        entity.setInstalledAt(device.getInstalledAt());
        entity.setStartedAt(device.getStartedAt());
        entity.setLastOpenedAt(device.getLastOpenedAt());
        entity.setCurrentAppVersionId(device.getCurrentAppVersionId());
        entity.setCurrentIosPackageId(device.getCurrentIosPackageId());
        entity.setAppUpdatedAt(device.getAppUpdatedAt());
        entity.setDeviceBrand(device.getDeviceBrand());
        entity.setOsVersion(device.getOsVersion());
        entity.setUserId(device.getUserId());
        entity.setAipayUid(device.getAipayUid());
        entity.setLoginId(device.getLoginId());
        entity.setAccountStatus(device.getAccountStatus());
        entity.setKycLevel(device.getKycLevel());
        entity.setNickname(device.getNickname());
        entity.setAvatarUrl(device.getAvatarUrl());
        entity.setMobile(device.getMobile());
        entity.setMaskedRealName(device.getMaskedRealName());
        entity.setIdCardNoMasked(device.getIdCardNoMasked());
        entity.setCountryCode(device.getCountryCode());
        entity.setGender(device.getGender());
        entity.setRegion(device.getRegion());
        entity.setLastLoginAt(device.getLastLoginAt());
        entity.setCreatedAt(device.getCreatedAt());
        entity.setUpdatedAt(device.getUpdatedAt());
        try {
            return toDomain(appDeviceMapper.save(entity));
        } catch (DuplicateKeyException duplicateKeyException) {
            AppDeviceDO existing = appDeviceMapper.findByDeviceId(device.getDeviceId())
                    .orElseThrow(() -> duplicateKeyException);
            entity.setId(existing.getId());
            if (existing.getCreatedAt() != null) {
                entity.setCreatedAt(existing.getCreatedAt());
            }
            if (existing.getInstalledAt() != null) {
                entity.setInstalledAt(existing.getInstalledAt());
            }
            return toDomain(appDeviceMapper.save(entity));
        }
    }

    /**
     * 按设备ID查找记录。
     */
    @Override
    public Optional<AppDevice> findByDeviceId(String deviceId) {
        return appDeviceMapper.findByDeviceId(deviceId).map(this::toDomain);
    }

    /**
     * 按用户ID查询最近登录设备。
     */
    @Override
    public Optional<AppDevice> findLatestByUserId(Long userId) {
        return appDeviceMapper.findLatestByUserId(userId).map(this::toDomain);
    }

    /**
     * 按登录账号查询最近登录设备。
     */
    @Override
    public Optional<AppDevice> findLatestByLoginId(String loginId) {
        return appDeviceMapper.findLatestByLoginId(loginId).map(this::toDomain);
    }

    /**
     * 按应用编码查询记录列表。
     */
    @Override
    public List<AppDevice> listByAppCode(String appCode) {
        return appDeviceMapper.listByAppCode(appCode).stream().map(this::toDomain).toList();
    }

    private AppDeviceDO resolveDO(AppDevice device) {
        if (device.getId() != null) {
            return appDeviceMapper.findById(device.getId()).orElse(new AppDeviceDO());
        }
        return appDeviceMapper.findByDeviceId(device.getDeviceId()).orElse(new AppDeviceDO());
    }

    private AppDevice toDomain(AppDeviceDO entity) {
        return new AppDevice(
                entity.getId(),
                entity.getDeviceId(),
                entity.getAppCode(),
                readTextList(entity.getClientIdListText()),
                AppDeviceStatus.fromCode(entity.getStatus()),
                entity.getInstalledAt(),
                entity.getStartedAt(),
                entity.getLastOpenedAt(),
                entity.getCurrentAppVersionId(),
                entity.getCurrentIosPackageId(),
                entity.getAppUpdatedAt(),
                entity.getDeviceBrand(),
                entity.getOsVersion(),
                entity.getUserId(),
                entity.getAipayUid(),
                entity.getLoginId(),
                entity.getAccountStatus(),
                entity.getKycLevel(),
                entity.getNickname(),
                entity.getAvatarUrl(),
                entity.getMobile(),
                entity.getMaskedRealName(),
                entity.getIdCardNoMasked(),
                entity.getCountryCode(),
                entity.getGender(),
                entity.getRegion(),
                entity.getLastLoginAt(),
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
