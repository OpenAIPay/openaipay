package cn.openaipay.infrastructure.inbound.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 入金机构侧实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("inbound_inst")
public class InboundInstDO {
    @TableId(value = "id", type = IdType.AUTO)
    /** 主键ID。 */
    private Long id;

    @TableField("inbound_id")
    /** 入金订单号。 */
    private String inboundId;

    @TableField("inst_id")
    /** 机构ID。 */
    private String instId;

    @TableField("inst_channel_code")
    /** 财务处理编码。 */
    private String instChannelCode;

    @TableField("inbound_order_no")
    /** 入金订单号。 */
    private String inboundOrderNo;

    @TableField("result_code")
    /** 结果码。 */
    private String resultCode;

    @TableField("result_description")
    /** 结果说明。 */
    private String resultDescription;

    @TableField("inbound_status")
    /** 处理状态。 */
    private String inboundStatus;

    @TableField("gmt_submit")
    /** 提交时间。 */
    private LocalDateTime gmtSubmit;

    @TableField("gmt_resp")
    /** 响应时间。 */
    private LocalDateTime gmtResp;

    @TableField("gmt_settle")
    /** 结算时间。 */
    private LocalDateTime gmtSettle;

    @TableField("gmt_create")
    /** 创建时间。 */
    private LocalDateTime createdAt;

    @TableField("gmt_modified")
    /** 更新时间。 */
    private LocalDateTime updatedAt;

}
