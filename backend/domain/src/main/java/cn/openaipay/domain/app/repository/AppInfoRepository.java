package cn.openaipay.domain.app.repository;

import cn.openaipay.domain.app.model.AppInfo;
import java.util.List;
import java.util.Optional;

/**
 * 应用定义仓储。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public interface AppInfoRepository {

    /**
     * 保存业务数据。
     */
    AppInfo save(AppInfo appInfo);

    /**
     * 按应用编码查找记录。
     */
    Optional<AppInfo> findByAppCode(String appCode);

    /**
     * 查找ALL信息。
     */
    List<AppInfo> findAll();
}
