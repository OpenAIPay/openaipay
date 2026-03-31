package cn.openaipay.domain.app.service.impl;

import cn.openaipay.domain.app.model.AppInfo;
import cn.openaipay.domain.app.model.AppUpdateType;
import cn.openaipay.domain.app.model.AppVersion;
import cn.openaipay.domain.app.model.AppVersionStatus;
import cn.openaipay.domain.app.service.AppVersionCheckPlan;
import cn.openaipay.domain.app.service.AppVersionDomainService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * App 版本领域服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class AppVersionDomainServiceImpl implements AppVersionDomainService {

    /** 用户提示信息默认文案。 */
    private static final String DEFAULT_VERSION_PROMPT_MESSAGE = "为保障使用体验，请更新到最新版本后继续使用。";

    /**
     * 处理计划版本信息。
     */
    @Override
    public AppVersionCheckPlan planVersionCheck(AppInfo appInfo, AppVersion latestVersion, String currentVersionNo) {
        boolean versionPromptEnabled = appInfo != null && appInfo.isVersionPromptEnabled();
        if (latestVersion == null) {
            return AppVersionCheckPlan.empty(versionPromptEnabled);
        }
        String normalizedCurrentVersionNo = normalizeOptional(currentVersionNo);
        boolean updateAvailable = normalizedCurrentVersionNo == null
                || compareVersions(latestVersion.getAppVersionNo(), normalizedCurrentVersionNo) > 0;
        boolean forceUpdate = false;
        if (normalizedCurrentVersionNo != null && latestVersion.getMinSupportedVersionNo() != null) {
            forceUpdate = compareVersions(normalizedCurrentVersionNo, latestVersion.getMinSupportedVersionNo()) < 0;
        }
        if (latestVersion.getUpdateType() == AppUpdateType.FORCE && updateAvailable) {
            forceUpdate = true;
        }
        return new AppVersionCheckPlan(
                versionPromptEnabled,
                latestVersion.getVersionCode(),
                latestVersion.getAppVersionNo(),
                updateAvailable,
                forceUpdate,
                latestVersion.getUpdateType().name(),
                latestVersion.getUpdatePromptFrequency().name(),
                resolveVersionDescription(latestVersion.getVersionDescription()),
                latestVersion.getMinSupportedVersionNo()
        );
    }

    /**
     * 标记版本信息。
     */
    @Override
    public void markLatestPublishedVersions(List<AppVersion> versions, String latestVersionCode, LocalDateTime now) {
        if (versions == null || versions.isEmpty()) {
            return;
        }
        String normalizedLatestVersionCode = normalizeRequired(latestVersionCode, "latestVersionCode");
        for (AppVersion version : versions) {
            boolean latest = normalizedLatestVersionCode.equals(version.getVersionCode());
            version.markLatestPublishedVersion(latest, now);
            if (latest) {
                version.changeStatus(AppVersionStatus.ENABLED, now);
            }
        }
    }

    private int compareVersions(String left, String right) {
        List<Integer> leftParts = parseVersion(left);
        List<Integer> rightParts = parseVersion(right);
        int maxLength = Math.max(leftParts.size(), rightParts.size());
        for (int index = 0; index < maxLength; index++) {
            int leftValue = index < leftParts.size() ? leftParts.get(index) : 0;
            int rightValue = index < rightParts.size() ? rightParts.get(index) : 0;
            if (leftValue != rightValue) {
                return Integer.compare(leftValue, rightValue);
            }
        }
        return 0;
    }

    private List<Integer> parseVersion(String raw) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            return List.of();
        }
        return Arrays.stream(normalized.split("\\."))
                .map(part -> part.replaceAll("[^0-9]", ""))
                .map(part -> part.isEmpty() ? 0 : Integer.parseInt(part))
                .toList();
    }

    private String normalizeRequired(String raw, String fieldName) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String resolveVersionDescription(String rawDescription) {
        String normalizedDescription = normalizeOptional(rawDescription);
        return normalizedDescription == null ? DEFAULT_VERSION_PROMPT_MESSAGE : normalizedDescription;
    }
}
