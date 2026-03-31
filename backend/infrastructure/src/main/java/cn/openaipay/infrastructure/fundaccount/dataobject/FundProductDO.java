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
 * 基金产品持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("fund_product")
public class FundProductDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 基金产品编码 */
    @TableField("fund_code")
    private String fundCode;

    /** 产品名称 */
    @TableField("product_name")
    private String productName;

    /** 币种编码 */
    @TableField("currency_code")
    private String currencyCode;

    /** 产品状态 */
    @TableField("product_status")
    private String productStatus;

    /** 单笔申购最小金额 */
    @TableField("single_subscribe_min_amount")
    private FundAmount singleSubscribeMinAmount;

    /** 单笔申购最大金额 */
    @TableField("single_subscribe_max_amount")
    private FundAmount singleSubscribeMaxAmount;

    /** 单日申购最大金额 */
    @TableField("daily_subscribe_max_amount")
    private FundAmount dailySubscribeMaxAmount;

    /** 单笔赎回最小份额 */
    @TableField("single_redeem_min_share")
    private FundAmount singleRedeemMinShare;

    /** 单笔赎回最大份额 */
    @TableField("single_redeem_max_share")
    private FundAmount singleRedeemMaxShare;

    /** 单日赎回最大份额 */
    @TableField("daily_redeem_max_share")
    private FundAmount dailyRedeemMaxShare;

    /** 快速赎回单日额度 */
    @TableField("fast_redeem_daily_quota")
    private FundAmount fastRedeemDailyQuota;

    /** 快速赎回单用户单日额度 */
    @TableField("fast_redeem_per_user_daily_quota")
    private FundAmount fastRedeemPerUserDailyQuota;

    /** 切换启用开关 */
    @TableField("switch_enabled")
    private Boolean switchEnabled;

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
