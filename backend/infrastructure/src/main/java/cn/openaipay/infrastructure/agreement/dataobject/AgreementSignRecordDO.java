package cn.openaipay.infrastructure.agreement.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

/**
 * 协议签约记录持久化实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("agreement_sign_record")
public class AgreementSignRecordDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 签约单号。 */
    @TableField("sign_no")
    private String signNo;

    /** 用户ID。 */
    @TableField("user_id")
    private Long userId;

    /** 业务类型。 */
    @TableField("biz_type")
    private String bizType;

    /** 基金编码。 */
    @TableField("fund_code")
    private String fundCode;

    /** 币种。 */
    @TableField("currency_code")
    private String currencyCode;

    /** 幂等键。 */
    @TableField("idempotency_key")
    private String idempotencyKey;

    /** 签约状态。 */
    @TableField("sign_status")
    private String signStatus;

    /** 签约时间。 */
    @TableField("signed_at")
    private LocalDateTime signedAt;

    /** 开户完成时间。 */
    @TableField("opened_at")
    private LocalDateTime openedAt;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
