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
 * 基金账户资金明细持久化对象。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("pay_fund_account_fund_detail")
public class PayFundAccountFundDetailDO {

    /** 汇总ID */
    @TableId(value = "summary_id", type = IdType.INPUT)
    private Long summaryId;

    /** 资金编码 */
    @TableField("fund_code")
    private String fundCode;

    /** 资金产品编码 */
    @TableField("fund_product_code")
    private String fundProductCode;

    /** 身份信息 */
    @TableField("account_identity")
    private String accountIdentity;

}
