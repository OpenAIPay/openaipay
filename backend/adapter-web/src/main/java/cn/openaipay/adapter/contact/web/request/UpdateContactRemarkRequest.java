package cn.openaipay.adapter.contact.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 更新联系人备注请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record UpdateContactRemarkRequest(
        /** 所属用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long ownerUserId,
        /** 用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long friendUserId,
        /** 备注 */
        String remark
) {
}
