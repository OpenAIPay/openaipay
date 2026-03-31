package cn.openaipay.adapter.feedback.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 提交反馈单请求参数。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
public record SubmitFeedbackRequest(
        /** 用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long userId,
        /** 反馈类型 */
        @Size(max = 32, message = "长度不能超过32") String feedbackType,
        /** 来源渠道 */
        @Size(max = 32, message = "长度不能超过32") String sourceChannel,
        /** 来源页面编码 */
        @Size(max = 64, message = "长度不能超过64") String sourcePageCode,
        /** 标题 */
        @Size(max = 128, message = "长度不能超过128") String title,
        /** 内容 */
        @NotBlank(message = "不能为空") @Size(min = 10, max = 200, message = "长度必须在10到200之间") String content,
        /** 联系人手机号 */
        @Size(max = 32, message = "长度不能超过32") String contactMobile,
        /** 附件地址列表 */
        @Size(max = 4, message = "最多支持4张") List<@Size(max = 512, message = "附件地址长度不能超过512") String> attachmentUrls
) {
}
