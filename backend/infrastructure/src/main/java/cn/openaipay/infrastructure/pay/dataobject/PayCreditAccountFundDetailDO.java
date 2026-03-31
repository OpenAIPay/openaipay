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
 * 信用账户资金明细持久化对象。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("pay_credit_account_fund_detail")
public class PayCreditAccountFundDetailDO {

    /** 汇总ID */
    @TableId(value = "summary_id", type = IdType.INPUT)
    private Long summaryId;

    /** 业务单号 */
    @TableField("account_no")
    private String accountNo;

    /** 信用类型 */
    @TableField("credit_account_type")
    private String creditAccountType;

    /** 信用产品编码 */
    @TableField("credit_product_code")
    private String creditProductCode;

}
