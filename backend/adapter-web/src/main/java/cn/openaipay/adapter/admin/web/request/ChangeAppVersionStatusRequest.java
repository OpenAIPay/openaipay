package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 变更应用版本状态请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record ChangeAppVersionStatusRequest(
        /** 状态编码 */
        @NotBlank(message = "不能为空") @Size(max = 32, message = "长度不能超过32") String status
) {
}
