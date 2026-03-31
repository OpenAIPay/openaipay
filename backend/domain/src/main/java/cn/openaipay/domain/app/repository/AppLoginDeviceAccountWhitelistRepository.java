package cn.openaipay.domain.app.repository;

import cn.openaipay.domain.app.model.AppLoginDeviceAccountWhitelist;
import java.util.List;

/**
 * 登录设备白名单账号仓储。
 *
 * @author: tenggk.ai
 * @date: 2026/03/21
 */
public interface AppLoginDeviceAccountWhitelistRepository {

    /**
     * 按应用和设备查询启用账号列表。
     */
    List<AppLoginDeviceAccountWhitelist> listEnabledByAppCodeAndDeviceId(String appCode, String deviceId);

    /**
     * 判断应用、设备、账号是否在白名单内。
     */
    boolean existsEnabledByAppCodeAndDeviceIdAndLoginId(String appCode, String deviceId, String loginId);

    /**
     * 判断应用下账号是否配置白名单。
     */
    boolean existsEnabledByAppCodeAndLoginId(String appCode, String loginId);

    /**
     * 按应用与设备统计已启用账号数。
     */
    long countEnabledByAppCodeAndDeviceId(String appCode, String deviceId);

    /**
     * 保存白名单账号绑定。
     */
    AppLoginDeviceAccountWhitelist save(AppLoginDeviceAccountWhitelist account);
}
