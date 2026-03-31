package cn.openaipay.adapter.app.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 上报 App 设备请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record UpsertAppDeviceRequest(
        /** 设备ID */
        @NotBlank(message = "不能为空") @Size(max = 64, message = "长度不能超过64") String deviceId,
        /** 应用编码 */
        @NotBlank(message = "不能为空") @Size(max = 64, message = "长度不能超过64") String appCode,
        /** 客户端ID列表 */
        @Size(max = 20, message = "长度不能超过20") List<@Size(max = 64, message = "长度不能超过64") String> clientIds,
        /** 设备品牌 */
        @Size(max = 64, message = "长度不能超过64") String deviceBrand,
        /** 系统版本号 */
        @Size(max = 64, message = "长度不能超过64") String osVersion,
        /** 当前版本编码 */
        @Size(max = 64, message = "长度不能超过64") String currentVersionCode,
        /** 当前版本号 */
        @Size(max = 64, message = "长度不能超过64") String currentVersionNo,
        /** 是否已启动标记 */
        Boolean started
) {
}
