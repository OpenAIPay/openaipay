package cn.openaipay.infrastructure.bankcard.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.joda.money.Money;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 银行卡持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("bank_card")
public class BankCardDO {

    /** 银行卡主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 银行卡号 */
    @TableField("card_no")
    private String cardNo;

    /** 绑卡所属用户ID */
    @TableField("user_id")
    private Long userId;

    /** 发卡行编码 */
    @TableField("bank_code")
    private String bankCode;

    /** 发卡行名称 */
    @TableField("bank_name")
    private String bankName;

    /** 银行卡类型 */
    @TableField("card_type")
    private String cardType;

    /** 持卡人姓名 */
    @TableField("card_holder_name")
    private String cardHolderName;

    /** 银行预留手机号 */
    @TableField("reserved_mobile")
    private String reservedMobile;

    /** 预留手机号后四位 */
    @TableField("phone_tail_no")
    private String phoneTailNo;

    /** 银行卡状态 */
    @TableField("card_status")
    private String cardStatus;

    /** 是否默认卡 */
    @TableField("is_default")
    private Boolean isDefault;

    /** 单笔支付限额 */
    @TableField("single_limit")
    private Money singleLimit;

    /** 单日支付限额 */
    @TableField("daily_limit")
    private Money dailyLimit;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
