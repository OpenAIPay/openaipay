package cn.openaipay.infrastructure.app;

import cn.openaipay.domain.app.model.AppLoginDeviceAccountWhitelist;
import cn.openaipay.domain.app.repository.AppLoginDeviceAccountWhitelistRepository;
import cn.openaipay.infrastructure.app.dataobject.AppLoginDeviceAccountWhitelistDO;
import cn.openaipay.infrastructure.app.mapper.AppLoginDeviceAccountWhitelistMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * 登录设备白名单账号仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/21
 */
@Repository
public class AppLoginDeviceAccountWhitelistRepositoryImpl implements AppLoginDeviceAccountWhitelistRepository {

    /** 登录设备白名单账号映射。 */
    private final AppLoginDeviceAccountWhitelistMapper whitelistMapper;

    public AppLoginDeviceAccountWhitelistRepositoryImpl(AppLoginDeviceAccountWhitelistMapper whitelistMapper) {
        this.whitelistMapper = whitelistMapper;
    }

    /**
     * 按应用和设备查询启用账号列表。
     */
    @Override
    public List<AppLoginDeviceAccountWhitelist> listEnabledByAppCodeAndDeviceId(String appCode, String deviceId) {
        return whitelistMapper.listEnabledByAppCodeAndDeviceId(appCode, deviceId).stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * 判断应用、设备、账号是否在白名单内。
     */
    @Override
    public boolean existsEnabledByAppCodeAndDeviceIdAndLoginId(String appCode, String deviceId, String loginId) {
        return whitelistMapper.existsEnabledByAppCodeAndDeviceIdAndLoginId(appCode, deviceId, loginId);
    }

    /**
     * 判断应用下账号是否配置白名单。
     */
    @Override
    public boolean existsEnabledByAppCodeAndLoginId(String appCode, String loginId) {
        return whitelistMapper.existsEnabledByAppCodeAndLoginId(appCode, loginId);
    }

    /**
     * 按应用与设备统计已启用账号数。
     */
    @Override
    public long countEnabledByAppCodeAndDeviceId(String appCode, String deviceId) {
        return whitelistMapper.countEnabledByAppCodeAndDeviceId(appCode, deviceId);
    }

    /**
     * 保存白名单账号绑定。
     */
    @Override
    public AppLoginDeviceAccountWhitelist save(AppLoginDeviceAccountWhitelist account) {
        AppLoginDeviceAccountWhitelistDO entity = whitelistMapper
                .findByAppCodeAndDeviceIdAndLoginId(account.getAppCode(), account.getDeviceId(), account.getLoginId())
                .orElse(new AppLoginDeviceAccountWhitelistDO());
        entity.setAppCode(account.getAppCode());
        entity.setDeviceId(account.getDeviceId());
        entity.setLoginId(account.getLoginId());
        entity.setNickname(account.getNickname());
        entity.setEnabled(account.isEnabled());
        entity.setCreatedAt(entity.getCreatedAt() == null ? account.getCreatedAt() : entity.getCreatedAt());
        entity.setUpdatedAt(account.getUpdatedAt());
        return toDomain(whitelistMapper.save(entity));
    }

    private AppLoginDeviceAccountWhitelist toDomain(AppLoginDeviceAccountWhitelistDO entity) {
        return new AppLoginDeviceAccountWhitelist(
                entity.getId(),
                entity.getAppCode(),
                entity.getDeviceId(),
                entity.getLoginId(),
                entity.getNickname(),
                Boolean.TRUE.equals(entity.getEnabled()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
