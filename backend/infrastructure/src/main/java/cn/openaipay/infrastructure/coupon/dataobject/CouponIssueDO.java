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
 * 优惠券发放持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("coupon_issue")
public class CouponIssueDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 券编号 */
    @TableField("coupon_no")
    private String couponNo;

    /** 模板标识 */
    @TableField("template_id")
    private Long templateId;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 券金额 */
    @TableField("coupon_amount")
    private Money couponAmount;

    /** 业务状态值 */
    @TableField("status")
    private String status;

    /** 领取渠道 */
    @TableField("claim_channel")
    private String claimChannel;

    /** 业务编号 */
    @TableField("business_no")
    private String businessNo;

    /** 业务订单号 */
    @TableField("order_no")
    private String orderNo;

    /** 全局业务单号 */
    @TableField("biz_order_no")
    private String bizOrderNo;

    /** 交易单号 */
    @TableField("trade_order_no")
    private String tradeOrderNo;

    /** 支付单号 */
    @TableField("pay_order_no")
    private String payOrderNo;

    /** 领取时间 */
    @TableField("claimed_at")
    private LocalDateTime claimedAt;

    /** 过期时间 */
    @TableField("expire_at")
    private LocalDateTime expireAt;

    /** 已用时间 */
    @TableField("used_at")
    private LocalDateTime usedAt;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
