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
 * 后台管理RBAC后台管理角色持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("admin_rbac_admin_role")
public class AdminRbacAdminRoleDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 管理标识 */
    @TableField("admin_id")
    private Long adminId;

    /** 角色编码 */
    @TableField("role_code")
    private String roleCode;

    /** 创建人 */
    @TableField("created_by")
    private String createdBy;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

}
