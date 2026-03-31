package cn.openaipay.infrastructure.pay.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 红包资金明细持久化实体
 *
 * 业务场景：记录支付资金明细中红包出资明细，通过summary_id挂接支付资金摘要。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("pay_red_packet_fund_detail")
public class PayRedPacketFundDetailDO {

    /** 关联支付资金明细摘要ID */
    @TableId(value = "summary_id", type = IdType.INPUT)
    private Long summaryId;

    /** 红包ID */
    @TableField("red_packet_id")
    private String redPacketId;

}
