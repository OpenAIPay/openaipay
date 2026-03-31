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
 * 入金付款方实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("inbound_payer")
public class InboundPayerDO {
    @TableId(value = "id", type = IdType.AUTO)
    /** 主键ID。 */
    private Long id;

    @TableField("inbound_id")
    /** 入金订单号。 */
    private String inboundId;

    @TableField("payer_account_no")
    /** 付款账号。 */
    private String payerAccountNo;

    @TableField("gmt_create")
    /** 创建时间。 */
    private LocalDateTime createdAt;

    @TableField("gmt_modified")
    /** 更新时间。 */
    private LocalDateTime updatedAt;

}
