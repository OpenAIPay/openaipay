package cn.openaipay.infrastructure.loanaccount.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 爱借账户档案持久化实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("loan_account_profile")
public class LoanAccountProfileDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 账户号。 */
    @TableField("account_no")
    private String accountNo;

    /** 用户ID。 */
    @TableField("user_id")
    private Long userId;

    /** 年化利率（百分数）。 */
    @TableField("annual_rate_percent")
    private BigDecimal annualRatePercent;

    /** 原始年化利率（百分数）。 */
    @TableField("original_annual_rate_percent")
    private BigDecimal originalAnnualRatePercent;

    /** 总期数（月）。 */
    @TableField("total_term_months")
    private Integer totalTermMonths;

    /** 放款日期。 */
    @TableField("draw_date")
    private LocalDate drawDate;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
