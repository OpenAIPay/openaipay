package cn.openaipay.infrastructure.loantrade.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

/**
 * 爱借交易单持久化实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("loan_trade_order")
public class LoanTradeOrderDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** XID */
    @TableField("xid")
    private String xid;

    /** 分支ID */
    @TableField("branch_id")
    private String branchId;

    /** 业务单号 */
    @TableField("business_no")
    private String businessNo;

    /** 业务单号 */
    @TableField("account_no")
    private String accountNo;

    /** 业务类型 */
    @TableField("operation_type")
    private String operationType;

    /** 当前状态编码 */
    @TableField("status")
    private String status;

    /** 请求金额 */
    @TableField("request_amount")
    private Money requestAmount;

    /** 业务金额 */
    @TableField("interest_amount")
    private Money interestAmount;

    /** 业务金额 */
    @TableField("principal_amount")
    private Money principalAmount;

    /** 业务金额 */
    @TableField("fine_amount")
    private Money fineAmount;

    /** 分支ID */
    @TableField("interest_branch_id")
    private String interestBranchId;

    /** 分支ID */
    @TableField("principal_branch_id")
    private String principalBranchId;

    /** 分支ID */
    @TableField("fine_branch_id")
    private String fineBranchId;

    /** 费率信息 */
    @TableField("annual_rate_percent")
    private BigDecimal annualRatePercent;

    /** remainingtermmonths信息 */
    @TableField("remaining_term_months")
    private Integer remainingTermMonths;

    /** monthlypayment信息 */
    @TableField("monthly_payment")
    private Money monthlyPayment;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
