package cn.openaipay.infrastructure.coupon.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.joda.money.Money;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 优惠券模板持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("coupon_template")
public class CouponTemplateDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 模板编码 */
    @TableField("template_code")
    private String templateCode;

    /** 模板名称 */
    @TableField("template_name")
    private String templateName;

    /** 场景类型 */
    @TableField("scene_type")
    private String sceneType;

    /** 值类型 */
    @TableField("value_type")
    private String valueType;

    /** 金额 */
    @TableField("amount")
    private Money amount;

    /** 最小金额 */
    @TableField("min_amount")
    private Money minAmount;

    /** 最大金额 */
    @TableField("max_amount")
    private Money maxAmount;

    /** 门槛金额 */
    @TableField("threshold_amount")
    private Money thresholdAmount;

    /** 总预算 */
    @TableField("total_budget")
    private Money totalBudget;

    /** 总库存 */
    @TableField("total_stock")
    private Integer totalStock;

    /** 已领数量 */
    @TableField("claimed_count")
    private Integer claimedCount;

    /** 单用户上限 */
    @TableField("per_user_limit")
    private Integer perUserLimit;

    /** 领取开始时间 */
    @TableField("claim_start_time")
    private LocalDateTime claimStartTime;

    /** 领取结束时间 */
    @TableField("claim_end_time")
    private LocalDateTime claimEndTime;

    /** 使用开始时间 */
    @TableField("use_start_time")
    private LocalDateTime useStartTime;

    /** 使用结束时间 */
    @TableField("use_end_time")
    private LocalDateTime useEndTime;

    /** 资金来源 */
    @TableField("funding_source")
    private String fundingSource;

    /** 规则载荷 */
    @TableField("rule_payload")
    private String rulePayload;

    /** 业务状态值 */
    @TableField("status")
    private String status;

    /** 创建人 */
    @TableField("created_by")
    private String createdBy;

    /** 更新人 */
    @TableField("updated_by")
    private String updatedBy;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
