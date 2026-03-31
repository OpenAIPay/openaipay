package cn.openaipay.application.app.command;

import java.util.List;

/**
 * 创建应用版本命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record CreateAppVersionCommand(
        /** 应用编码 */
        String appCode,
        /** 版本编码 */
        String versionCode,
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
        /** IOS编码 */
        String iosCode,
        /** 应用地址 */
        String appStoreUrl,
        /** 安装包大小字节数 */
        Long packageSizeBytes,
        /** MD5信息 */
        String md5
) {
}
