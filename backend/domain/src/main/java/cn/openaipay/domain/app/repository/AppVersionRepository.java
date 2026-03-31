package cn.openaipay.domain.app.repository;

import cn.openaipay.domain.app.model.AppClientType;
import cn.openaipay.domain.app.model.AppVersion;
import java.util.List;
import java.util.Optional;

/**
 * 应用版本仓储。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public interface AppVersionRepository {

    /**
     * 保存业务数据。
     */
    AppVersion save(AppVersion appVersion);

    /**
     * 按版本编码查找记录。
     */
    Optional<AppVersion> findByVersionCode(String versionCode);

    /**
     * 按应用编码查询记录列表。
     */
    List<AppVersion> listByAppCode(String appCode, AppClientType clientType);

    /**
     * 查找版本信息。
     */
    Optional<AppVersion> findLatestPublishedVersion(String appCode, AppClientType clientType);
}
