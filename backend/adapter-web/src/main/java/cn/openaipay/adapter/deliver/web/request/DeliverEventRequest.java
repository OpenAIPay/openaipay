package cn.openaipay.adapter.deliver.web.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 投放事件回传请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliverEventRequest {

    /** 业务ID */
    @NotBlank
    private String clientId;

    /** 用户ID */
    private Long userId;

    /** 场景编码 */
    private String sceneCode;

    /** 渠道信息 */
    private String channel;

    /** 位置编码 */
    @NotBlank
    private String positionCode;

    /** 单元编码 */
    private String unitCode;

    /** 业务编码 */
    private String creativeCode;

    /** 事件类型 */
    @NotBlank
    private String eventType;

    /** 事件时间 */
    private LocalDateTime eventTime;

}
