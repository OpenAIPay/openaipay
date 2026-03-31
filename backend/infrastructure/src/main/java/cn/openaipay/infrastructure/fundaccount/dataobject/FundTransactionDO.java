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
 * 基金交易持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("fund_transaction")
public class FundTransactionDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 业务订单号 */
    @TableField("order_no")
    private String orderNo;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 基金产品编码 */
    @TableField("fund_code")
    private String fundCode;

    /** 交易类型 */
    @TableField("transaction_type")
    private String transactionType;

    /** 交易状态 */
    @TableField("transaction_status")
    private String transactionStatus;

    /** 请求金额 */
    @TableField("request_amount")
    private FundAmount requestAmount;

    /** 请求份额 */
    @TableField("request_share")
    private FundAmount requestShare;

    /** 确认金额 */
    @TableField("confirmed_amount")
    private FundAmount confirmedAmount;

    /** 确认份额 */
    @TableField("confirmed_share")
    private FundAmount confirmedShare;

    /** 业务编号 */
    @TableField("business_no")
    private String businessNo;

    /** 扩展信息 */
    @TableField("ext_info")
    private String extInfo;

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
