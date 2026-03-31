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
 * 钱包资金明细持久化实体
 *
 * 业务场景：记录支付资金明细中钱包出资的账户号信息，使用summary_id与摘要表一一关联。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("pay_wallet_fund_detail")
public class PayWalletFundDetailDO {

    /** 关联支付资金明细摘要ID */
    @TableId(value = "summary_id", type = IdType.INPUT)
    private Long summaryId;

    /** 钱包账户号 */
    @TableField("account_no")
    private String accountNo;

}
