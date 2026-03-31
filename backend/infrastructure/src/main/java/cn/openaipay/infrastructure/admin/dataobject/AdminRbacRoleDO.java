package cn.openaipay.infrastructure.admin.dataobject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 后台管理RBAC角色持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("admin_rbac_role")
public class AdminRbacRoleDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 角色编码 */
    @TableField("role_code")
    private String roleCode;

    /** 角色名称 */
    @TableField("role_name")
    private String roleName;

    /** 角色范围 */
    @TableField("role_scope")
    private String roleScope;

    /** 角色状态 */
    @TableField("role_status")
    private String roleStatus;

    /** 是否内置角色 */
    @TableField("is_builtin")
    private Boolean builtin;

    /** 角色描述 */
    @TableField("role_desc")
    private String roleDesc;

}
