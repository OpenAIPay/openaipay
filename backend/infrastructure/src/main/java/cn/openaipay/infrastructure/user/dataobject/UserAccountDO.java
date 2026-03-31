package cn.openaipay.infrastructure.user.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户账户持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("user_account")
public class UserAccountDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 平台用户号 */
    @TableField("aipay_uid")
    private String aipayUid;

    /** 登录标识 */
    @TableField("login_id")
    private String loginId;

    /** 账户状态 */
    @TableField("account_status")
    private String accountStatus;

    /** KYC等级 */
    @TableField("kyc_level")
    private String kycLevel;

    /** 账号来源 */
    @TableField("account_source")
    private String accountSource;

    /** 登录密码已设置 */
    @TableField("login_password_set")
    private Boolean loginPasswordSet;

    /** 支付密码已设置 */
    @TableField("pay_password_set")
    private Boolean payPasswordSet;

    /** 登录密码哈希 */
    @TableField("login_password_sha256")
    private String loginPasswordSha256;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
