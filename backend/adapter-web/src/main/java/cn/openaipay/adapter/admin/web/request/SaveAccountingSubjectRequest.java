package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 保存会计科目请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record SaveAccountingSubjectRequest(
        /** 科目名称 */
        @NotBlank(message = "不能为空") String subjectName,
        /** 科目类型 */
        @NotBlank(message = "不能为空") String subjectType,
        /** 余额方向 */
        @NotBlank(message = "不能为空") String balanceDirection,
        /** 科目编码 */
        String parentSubjectCode,
        /** 业务单号 */
        Integer levelNo,
        /** 启用标记 */
        @NotNull(message = "不能为空") Boolean enabled,
        /** 备注 */
        String remark
) {
}
