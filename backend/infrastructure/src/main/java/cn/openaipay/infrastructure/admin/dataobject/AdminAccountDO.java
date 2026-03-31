package cn.openaipay.infrastructure.admin.dataobject;

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
 * 后台管理账户持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("admin_account")
public class AdminAccountDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 管理标识 */
    @TableField("admin_id")
    private Long adminId;

    /** 用户名 */
    @TableField("username")
    private String username;

    /** 显示名称 */
    @TableField("display_name")
    private String displayName;

    /** 密码哈希 */
    @TableField("password_sha256")
    private String passwordSha256;

    /** 账户状态 */
    @TableField("account_status")
    private String accountStatus;

    /** 最近登录时间 */
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
