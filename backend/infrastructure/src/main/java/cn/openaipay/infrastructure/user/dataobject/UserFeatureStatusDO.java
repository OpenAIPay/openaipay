package cn.openaipay.infrastructure.user.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户能力开通状态持久化实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("user_feature_status")
public class UserFeatureStatusDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID。 */
    @TableField("user_id")
    private Long userId;

    /** 能力编码。 */
    @TableField("feature_code")
    private String featureCode;

    /** 是否已开通。 */
    @TableField("enabled")
    private Boolean enabled;

    /** 开通时间。 */
    @TableField("opened_at")
    private LocalDateTime openedAt;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
