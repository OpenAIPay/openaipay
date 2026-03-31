package cn.openaipay.infrastructure.fundaccount.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import cn.openaipay.domain.shared.number.FundAmount;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 基金账户持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("fund_account")
public class FundAccountDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 基金产品编码 */
    @TableField("fund_code")
    private String fundCode;

    /** 币种编码 */
    @TableField("currency_code")
    private String currencyCode;

    /** 可用份额 */
    @TableField("available_share")
    private FundAmount availableShare;

    /** 冻结份额 */
    @TableField("frozen_share")
    private FundAmount frozenShare;

    /** 待处理申购金额 */
    @TableField("pending_subscribe_amount")
    private FundAmount pendingSubscribeAmount;

    /** 待处理赎回份额 */
    @TableField("pending_redeem_share")
    private FundAmount pendingRedeemShare;

    /** 累计收益 */
    @TableField("accumulated_income")
    private FundAmount accumulatedIncome;

    /** 昨日收益 */
    @TableField("yesterday_income")
    private FundAmount yesterdayIncome;

    /** 最新净值 */
    @TableField("latest_nav")
    private FundAmount latestNav;

    /** 账户状态 */
    @TableField("account_status")
    private String accountStatus;

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
