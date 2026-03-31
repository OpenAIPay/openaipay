package cn.openaipay.infrastructure.pay.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 支付参与方分支持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("pay_participant_branch")
public class PayParticipantBranchDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 支付单号 */
    @TableField("pay_order_no")
    private String payOrderNo;

    /** 参与方类型 */
    @TableField("participant_type")
    private String participantType;

    /** 分支标识 */
    @TableField("branch_id")
    private String branchId;

    /** 参与方资源标识 */
    @TableField("participant_resource_id")
    private String participantResourceId;

    /** 请求载荷 */
    @TableField("request_payload")
    private String requestPayload;

    /** 业务状态值 */
    @TableField("status")
    private String status;

    /** 响应消息 */
    @TableField("response_message")
    private String responseMessage;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
