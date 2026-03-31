package cn.openaipay.infrastructure.app.mapper;

import cn.openaipay.infrastructure.app.dataobject.AppLoginDeviceAccountWhitelistDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录设备白名单账号持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/21
 */
@Mapper
public interface AppLoginDeviceAccountWhitelistMapper extends BaseMapper<AppLoginDeviceAccountWhitelistDO> {

    /**
     * 按应用和设备查询启用账号列表。
     */
    default List<AppLoginDeviceAccountWhitelistDO> listEnabledByAppCodeAndDeviceId(String appCode, String deviceId) {
        QueryWrapper<AppLoginDeviceAccountWhitelistDO> wrapper = new QueryWrapper<>();
        wrapper.eq("app_code", appCode)
                .eq("device_id", deviceId)
                .eq("enabled", 1)
                .orderByAsc("id");
        return selectList(wrapper);
    }

    /**
     * 判断应用、设备、账号是否在白名单内。
     */
    default boolean existsEnabledByAppCodeAndDeviceIdAndLoginId(String appCode, String deviceId, String loginId) {
        QueryWrapper<AppLoginDeviceAccountWhitelistDO> wrapper = new QueryWrapper<>();
        wrapper.eq("app_code", appCode)
                .eq("device_id", deviceId)
                .eq("login_id", loginId)
                .eq("enabled", 1)
                .last("LIMIT 1");
        return selectCount(wrapper) > 0;
    }

    /**
     * 判断应用下账号是否配置白名单。
     */
    default boolean existsEnabledByAppCodeAndLoginId(String appCode, String loginId) {
        QueryWrapper<AppLoginDeviceAccountWhitelistDO> wrapper = new QueryWrapper<>();
        wrapper.eq("app_code", appCode)
                .eq("login_id", loginId)
                .eq("enabled", 1)
                .last("LIMIT 1");
        return selectCount(wrapper) > 0;
    }

    /**
     * 按应用、设备、账号查询白名单记录。
     */
    default Optional<AppLoginDeviceAccountWhitelistDO> findByAppCodeAndDeviceIdAndLoginId(String appCode,
                                                                                           String deviceId,
                                                                                           String loginId) {
        QueryWrapper<AppLoginDeviceAccountWhitelistDO> wrapper = new QueryWrapper<>();
        wrapper.eq("app_code", appCode)
                .eq("device_id", deviceId)
                .eq("login_id", loginId)
                .last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按应用和设备统计已启用账号数。
     */
    default long countEnabledByAppCodeAndDeviceId(String appCode, String deviceId) {
        QueryWrapper<AppLoginDeviceAccountWhitelistDO> wrapper = new QueryWrapper<>();
        wrapper.eq("app_code", appCode)
                .eq("device_id", deviceId)
                .eq("enabled", 1);
        Long total = selectCount(wrapper);
        return total == null ? 0L : total;
    }
}
