package cn.openaipay.domain.app.repository;

import cn.openaipay.domain.app.model.AppIosPackage;
import java.util.Optional;

/**
 * iOS 包仓储。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public interface AppIosPackageRepository {

    /**
     * 保存业务数据。
     */
    AppIosPackage save(AppIosPackage appIosPackage);

    /**
     * 按IOS编码查找记录。
     */
    Optional<AppIosPackage> findByIosCode(String iosCode);

    /**
     * 按版本编码查找记录。
     */
    Optional<AppIosPackage> findByVersionCode(String versionCode);
}
