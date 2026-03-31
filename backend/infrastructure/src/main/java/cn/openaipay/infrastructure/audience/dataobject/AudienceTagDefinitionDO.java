package cn.openaipay.infrastructure.audience.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 标签定义持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("audience_tag_definition")
public class AudienceTagDefinitionDO {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 标签编码 */
    @TableField("tag_code")
    private String tagCode;

    /** 标签名称 */
    @TableField("tag_name")
    private String tagName;

    /** 标签类型 */
    @TableField("tag_type")
    private String tagType;

    /** 标签值域 */
    @TableField("value_scope")
    private String valueScope;

    /** 标签描述 */
    @TableField("description")
    private String description;

    /** 是否启用 */
    @TableField("enabled")
    private Boolean enabled;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
