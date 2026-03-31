package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建应用请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record CreateAppRequest(
        /** 应用编码 */
        @NotBlank(message = "不能为空") @Size(max = 64, message = "长度不能超过64") String appCode,
        /** 应用名称 */
        @NotBlank(message = "不能为空") @Size(max = 128, message = "长度不能超过128") String appName
) {
}
