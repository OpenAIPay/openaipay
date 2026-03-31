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
 * 后台管理RBAC模块持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("admin_rbac_module")
public class AdminRbacModuleDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 模块编码 */
    @TableField("module_code")
    private String moduleCode;

    /** 模块名称 */
    @TableField("module_name")
    private String moduleName;

    /** 模块描述 */
    @TableField("module_desc")
    private String moduleDesc;

    /** 启用开关 */
    @TableField("enabled")
    private Boolean enabled;

    /** 排序编号 */
    @TableField("sort_no")
    private Integer sortNo;

}
