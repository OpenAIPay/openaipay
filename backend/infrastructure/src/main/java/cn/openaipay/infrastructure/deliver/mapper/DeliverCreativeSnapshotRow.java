package cn.openaipay.infrastructure.deliver.mapper;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * DeliverCreativeSnapshotRow 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Getter
@Setter
@NoArgsConstructor
public class DeliverCreativeSnapshotRow {

    /** 业务ID */
    private Long creativeId;
    /** 业务编码 */
    private String creativeCode;
    /** 业务名称 */
    private String creativeName;
    /** 单元编码 */
    private String unitCode;
    /** 业务编码 */
    private String materialCode;
    /** 业务地址 */
    private String landingUrl;
    /** 业务JSON */
    private String schemaJson;
    /** 优先级 */
    private Integer priority;
    /** 权重 */
    private Integer weight;
    /** 订单信息 */
    private Integer displayOrder;
    /** 兜底标记 */
    private Boolean fallback;
    /** 预览图地址 */
    private String previewImage;
    /** 当前状态编码 */
    private String status;
    /** 生效开始时间 */
    private LocalDateTime activeFrom;
    /** TO信息 */
    private LocalDateTime activeTo;
    /** 记录创建时间 */
    private LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

}
