package cn.openaipay.infrastructure.trade.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 交易流程步骤持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("trade_flow_step")
public class TradeFlowStepDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 统一交易主单号。 */
    @TableField("trade_order_no")
    private String tradeOrderNo;

    /** 流程步骤编码。 */
    @TableField("step_code")
    private String stepCode;

    /** 流程步骤状态。 */
    @TableField("step_status")
    private String stepStatus;

    /** 步骤请求报文。 */
    @TableField("request_payload")
    private String requestPayload;

    /** 步骤响应报文。 */
    @TableField("response_payload")
    private String responsePayload;

    /** 步骤错误信息。 */
    @TableField("error_message")
    private String errorMessage;

    /** 步骤开始时间。 */
    @TableField("started_at")
    private LocalDateTime startedAt;

    /** 步骤结束时间。 */
    @TableField("finished_at")
    private LocalDateTime finishedAt;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
