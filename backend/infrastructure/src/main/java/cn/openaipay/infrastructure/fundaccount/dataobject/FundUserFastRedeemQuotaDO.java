package cn.openaipay.infrastructure.fundaccount.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import cn.openaipay.domain.shared.number.FundAmount;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 基金用户Fast赎回额度持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("fund_user_fast_redeem_quota")
public class FundUserFastRedeemQuotaDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 基金产品编码 */
    @TableField("fund_code")
    private String fundCode;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 额度日期 */
    @TableField("quota_date")
    private LocalDate quotaDate;

    /** 额度上限 */
    @TableField("quota_limit")
    private FundAmount quotaLimit;

    /** 额度已用 */
    @TableField("quota_used")
    private FundAmount quotaUsed;

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
