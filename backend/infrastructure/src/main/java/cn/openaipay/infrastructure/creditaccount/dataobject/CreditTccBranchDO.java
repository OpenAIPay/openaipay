package cn.openaipay.infrastructure.creditaccount.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import org.joda.money.Money;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 信用TCC分支持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("credit_tcc_transaction")
public class CreditTccBranchDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 分布式事务ID */
    @TableField("xid")
    private String xid;

    /** 分支标识 */
    @TableField("branch_id")
    private String branchId;

    /** 账户编号 */
    @TableField("account_no")
    private String accountNo;

    /** 操作类型 */
    @TableField("operation_type")
    private String operationType;

    /** 资产类型 */
    @TableField("asset_category")
    private String assetCategory;

    /** 分支状态 */
    @TableField("branch_status")
    private String branchStatus;

    /** 金额 */
    @TableField("amount")
    private Money amount;

    /** 业务编号 */
    @TableField("business_no")
    private String businessNo;

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
