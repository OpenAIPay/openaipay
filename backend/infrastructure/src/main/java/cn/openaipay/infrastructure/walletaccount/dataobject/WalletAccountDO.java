package cn.openaipay.infrastructure.walletaccount.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 钱包账户持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("wallet_account")
public class WalletAccountDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 币种编码 */
    @TableField("currency_code")
    private String currencyCode;

    /** 可用余额 */
    @TableField("available_balance")
    private BigDecimal availableBalance;

    /** 冻结余额 */
    @TableField("reserved_balance")
    private BigDecimal reservedBalance;

    /** 账户状态 */
    @TableField("account_status")
    private String accountStatus;

    /** 锁版本 */
    @TableField("lock_version")
    private Long lockVersion;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
