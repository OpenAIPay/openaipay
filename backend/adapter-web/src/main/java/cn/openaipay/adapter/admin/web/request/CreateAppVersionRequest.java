package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 创建应用版本请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record CreateAppVersionRequest(
        /** 版本编码 */
        @NotBlank(message = "不能为空") @Size(max = 64, message = "长度不能超过64") String versionCode,
        /** 应用版本单号 */
        @NotBlank(message = "不能为空") @Size(max = 32, message = "长度不能超过32")
                                        @Pattern(regexp = "^\\d{2}\\.\\d{3,4}\\.\\d+$", message = "版本号格式必须为YY.MMDD.SEQUENCE，例如26.315.1") String appVersionNo,
        /** 业务类型 */
        @Size(max = 32, message = "长度不能超过32") String updateType,
        /** 更新提示频率 */
        @Size(max = 32, message = "长度不能超过32") String updatePromptFrequency,
        /** 用户提示信息 */
        @Size(max = 1000, message = "长度不能超过1000") String versionDescription,
        /** 发布者备注 */
        @Size(max = 1000, message = "长度不能超过1000") String publisherRemark,
        /** 发布地区列表 */
        @Size(max = 50, message = "长度不能超过50") List<@Size(max = 32, message = "长度不能超过32") String> releaseRegions,
        /** 定向地区列表 */
        @Size(max = 50, message = "长度不能超过50") List<@Size(max = 32, message = "长度不能超过32") String> targetedRegions,
        /** MIN版本单号 */
        @Size(max = 32, message = "长度不能超过32")
                                        @Pattern(regexp = "^$|^\\d{2}\\.\\d{3,4}\\.\\d+$", message = "最低支持版本格式必须为YY.MMDD.SEQUENCE，例如26.315.1") String minSupportedVersionNo,
        /** IOS编码 */
        @Size(max = 64, message = "长度不能超过64") String iosCode,
        /** 应用地址 */
        @Size(max = 512, message = "长度不能超过512") String appStoreUrl,
        /** 安装包大小字节数 */
        @Min(value = 0, message = "必须大于等于0") Long packageSizeBytes,
        /** MD5信息 */
        @Size(max = 64, message = "长度不能超过64") String md5
) {
}
