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
 * 后台管理菜单持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("admin_menu")
public class AdminMenuDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 菜单编码 */
    @TableField("menu_code")
    private String menuCode;

    /** 父级编码 */
    @TableField("parent_code")
    private String parentCode;

    /** 菜单名称 */
    @TableField("menu_name")
    private String menuName;

    /** 路径 */
    @TableField("path")
    private String path;

    /** 图标 */
    @TableField("icon")
    private String icon;

    /** 排序编号 */
    @TableField("sort_no")
    private Integer sortNo;

    /** 可见 */
    @TableField("visible")
    private Boolean visible;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
