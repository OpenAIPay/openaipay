package cn.openaipay.adapter.admin.web.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 重放单条 outbox 死信请求参数。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record RequeueOutboxDeadLetterRequest(
        /** 下次重试时间。 */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime nextRetryAt
) {
}
