package cn.openaipay.application.app.dto;

import java.time.LocalDateTime;

/**
 * iOS 包 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record AppIosPackageDTO(
        /** IOS编码 */
        String iosCode,
        /** 应用编码 */
        String appCode,
        /** 版本编码 */
        String versionCode,
        /** 应用地址 */
        String appStoreUrl,
        /** 安装包大小字节数 */
        Long packageSizeBytes,
        /** MD5信息 */
        String md5,
        /** 业务时间 */
        LocalDateTime reviewSubmittedAt,
        /** 发布时间 */
        LocalDateTime publishedAt,
        /** 业务状态 */
        String releaseStatus,
        /** reviewsubmittedBY信息 */
        String reviewSubmittedBy,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
