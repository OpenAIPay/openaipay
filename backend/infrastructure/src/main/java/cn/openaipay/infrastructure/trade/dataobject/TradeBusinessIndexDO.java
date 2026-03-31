package cn.openaipay.infrastructure.trade.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.joda.money.Money;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 业务交易查询索引持久化实体。
 *
 * 业务场景：后台与账单中心按业务域、业务单号查询交易时直接命中本表，避免扫描统一交易主单再拼业务字段。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("trade_bill_index")
public class TradeBusinessIndexDO {

    /** 查询索引主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 统一交易主单号。 */
    @TableField("trade_order_no")
    private String tradeOrderNo;

    /** 业务域编码。 */
    @TableField("business_domain_code")
    private String businessDomainCode;

    /** 业务交易单号。 */
    @TableField("biz_order_no")
    private String bizOrderNo;

    /** 产品类型。 */
    @TableField("product_type")
    private String productType;

    /** 业务类型。 */
    @TableField("business_type")
    private String businessType;

    /** 业务所属用户ID。 */
    @TableField("user_id")
    private Long userId;

    /** 交易对手用户ID。 */
    @TableField("counterparty_user_id")
    private Long counterpartyUserId;

    /** 业务账户号。 */
    @TableField("account_no")
    private String accountNo;

    /** 账单号。 */
    @TableField("bill_no")
    private String billNo;

    /** 账单月份。 */
    @TableField("bill_month")
    private String billMonth;

    /** 展示标题。 */
    @TableField("display_title")
    private String displayTitle;

    /** 展示副标题。 */
    @TableField("display_subtitle")
    private String displaySubtitle;

    /** 展示金额。 */
    @TableField("amount")
    private Money amount;

    /** 统一交易状态。 */
    @TableField("status")
    private String status;

    /** 业务排序时间。 */
    @TableField("trade_time")
    private LocalDateTime tradeTime;

    /** 记录创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
