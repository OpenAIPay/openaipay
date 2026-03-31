package cn.openaipay.application.app.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用版本 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record AppVersionDTO(
        /** 版本编码 */
        String versionCode,
        /** 应用编码 */
        String appCode,
        /** 业务类型 */
        String clientType,
        /** 应用版本单号 */
        String appVersionNo,
        /** 业务类型 */
        String updateType,
        /** 更新提示频率 */
        String updatePromptFrequency,
        /** 用户提示信息 */
        String versionDescription,
        /** 发布者备注 */
        String publisherRemark,
        /** 发布地区列表 */
        List<String> releaseRegions,
        /** 定向地区列表 */
        List<String> targetedRegions,
        /** MIN版本单号 */
        String minSupportedVersionNo,
        /** 业务版本号 */
        boolean latestPublishedVersion,
        /** 状态编码 */
        String status,
        /** IOSpackage信息 */
        AppIosPackageDTO iosPackage,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
