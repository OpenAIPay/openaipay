package cn.openaipay.domain.app.repository;

import cn.openaipay.domain.app.model.AppDevice;
import java.util.List;
import java.util.Optional;

/**
 * App 设备仓储。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public interface AppDeviceRepository {

    /**
     * 保存业务数据。
     */
    AppDevice save(AppDevice device);

    /**
     * 按设备ID查找记录。
     */
    Optional<AppDevice> findByDeviceId(String deviceId);

    /**
     * 按用户ID查询最近登录设备。
     */
    Optional<AppDevice> findLatestByUserId(Long userId);

    /**
     * 按登录账号查询最近登录设备。
     */
    Optional<AppDevice> findLatestByLoginId(String loginId);

    /**
     * 按应用编码查询记录列表。
     */
    List<AppDevice> listByAppCode(String appCode);
}
