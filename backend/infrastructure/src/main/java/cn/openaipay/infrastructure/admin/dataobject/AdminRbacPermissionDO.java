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
 * 后台管理RBAC权限持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("admin_rbac_permission")
public class AdminRbacPermissionDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 权限编码 */
    @TableField("permission_code")
    private String permissionCode;

    /** 权限名称 */
    @TableField("permission_name")
    private String permissionName;

    /** 模块编码 */
    @TableField("module_code")
    private String moduleCode;

    /** 资源类型 */
    @TableField("resource_type")
    private String resourceType;

    /** HTTP方式 */
    @TableField("http_method")
    private String httpMethod;

    /** 路径模式 */
    @TableField("path_pattern")
    private String pathPattern;

    /** 权限描述 */
    @TableField("permission_desc")
    private String permissionDesc;

}
