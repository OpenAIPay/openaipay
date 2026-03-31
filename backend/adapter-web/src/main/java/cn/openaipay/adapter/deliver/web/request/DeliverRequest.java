package cn.openaipay.adapter.deliver.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 投放请求对象。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliverRequest {

    /** 位置编码列表 */
    @NotEmpty
    private List<String> positionCodeList;

    /** 业务ID */
    @NotBlank
    private String clientId;

    /** 用户ID */
    private Long userId;

    /** 场景编码 */
    private String sceneCode;

    /** 渠道信息 */
    private String channel;

    /** 用户信息 */
    @Default
    private Set<String> userTags = new LinkedHashSet<>();

    /** 请求时间 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime requestTime;

}
