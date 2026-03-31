package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更新应用版本请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/29
 */
public record UpdateAppVersionRequest(
        /** 更新类型 */
        @Size(max = 32, message = "长度不能超过32") String updateType,
        /** 更新提示频率 */
        @Size(max = 32, message = "长度不能超过32") String updatePromptFrequency,
        /** 用户提示信息 */
        @Size(max = 1000, message = "长度不能超过1000") String versionDescription,
        /** 发布者备注 */
        @Size(max = 1000, message = "长度不能超过1000") String publisherRemark,
        /** 最低支持版本号 */
        @Size(max = 32, message = "长度不能超过32")
        @Pattern(regexp = "^$|^\\d{2}\\.\\d{3,4}\\.\\d+$", message = "最低支持版本格式必须为YY.MMDD.SEQUENCE，例如26.315.1") String minSupportedVersionNo,
        /** iOS 包编码 */
        @Size(max = 64, message = "长度不能超过64") String iosCode,
        /** 商店地址 */
        @Size(max = 512, message = "长度不能超过512") String appStoreUrl,
        /** 安装包大小字节数 */
        @Min(value = 0, message = "必须大于等于0") Long packageSizeBytes,
        /** MD5信息 */
        @Size(max = 64, message = "长度不能超过64") String md5
) {
}
