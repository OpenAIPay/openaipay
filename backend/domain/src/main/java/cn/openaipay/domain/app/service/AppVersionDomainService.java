package cn.openaipay.domain.app.service;

import cn.openaipay.domain.app.model.AppInfo;
import cn.openaipay.domain.app.model.AppVersion;
import java.time.LocalDateTime;
import java.util.List;

/**
 * App 版本领域服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface AppVersionDomainService {

    /**
     * 处理计划版本信息。
     */
    AppVersionCheckPlan planVersionCheck(AppInfo appInfo, AppVersion latestVersion, String currentVersionNo);

    /**
     * 标记版本信息。
     */
    void markLatestPublishedVersions(List<AppVersion> versions, String latestVersionCode, LocalDateTime now);
}
