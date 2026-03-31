package cn.openaipay.adapter.admin.web.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 批量重放 outbox 死信请求参数。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record RequeueOutboxDeadLettersRequest(
        /** 主题（可选，不传则重放所有主题）。 */
        @Size(max = 128, message = "长度不能超过128") String topic,
        /** 重放条数上限。 */
        @Min(value = 1, message = "必须大于0")
        @Max(value = 200, message = "不能超过200")
        Integer limit,
        /** 下次重试时间。 */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime nextRetryAt
) {
}
