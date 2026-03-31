package cn.openaipay.infrastructure.creditaccount.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import org.joda.money.Money;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 信用账户持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("credit_account")
public class CreditAccountDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 账户编号 */
    @TableField("account_no")
    private String accountNo;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 总上限 */
    @TableField("total_limit")
    private Money totalLimit;

    /** 本金余额 */
    @TableField("principal_balance")
    private Money principalBalance;

    /** 未入账本金金额 */
    @TableField("principal_unreach_amount")
    private Money principalUnreachAmount;

    /** 逾期本金余额 */
    @TableField("overdue_principal_balance")
    private Money overduePrincipalBalance;

    /** 逾期未入账本金金额 */
    @TableField("overdue_principal_unreach_amount")
    private Money overduePrincipalUnreachAmount;

    /** 利息余额 */
    @TableField("interest_balance")
    private Money interestBalance;

    /** 罚息余额 */
    @TableField("fine_balance")
    private Money fineBalance;

    /** 账户状态 */
    @TableField("account_status")
    private String accountStatus;

    /** 支付状态 */
    @TableField("pay_status")
    private String payStatus;

    /** 每月还款日 */
    @TableField("repay_day_of_month")
    private Integer repayDayOfMonth;

    /** 锁版本 */
    @Version
    @TableField("lock_version")
    private Long lockVersion;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
