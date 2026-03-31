package cn.openaipay.application.app.command;

/**
 * 更新应用版本命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/29
 */
public record UpdateAppVersionCommand(
        /** 版本编码 */
        String versionCode,
        /** 更新类型 */
        String updateType,
        /** 更新提示频率 */
        String updatePromptFrequency,
        /** 用户提示信息 */
        String versionDescription,
        /** 发布者备注 */
        String publisherRemark,
        /** 最低支持版本号 */
        String minSupportedVersionNo,
        /** iOS 包编码 */
        String iosCode,
        /** 商店地址 */
        String appStoreUrl,
        /** 安装包大小字节数 */
        Long packageSizeBytes,
        /** 安装包 MD5 */
        String md5
) {
}
