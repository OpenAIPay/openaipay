package cn.openaipay.infrastructure.security.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 安全限流状态持久化实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("security_attempt_guard")
public class SecurityAttemptGuardDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /** 限流作用域。 */
    @TableField("guard_scope")
    private String guardScope;
    /** 主体键。 */
    @TableField("principal_key")
    private String principalKey;
    /** 当前窗口计数。 */
    @TableField("attempt_count")
    private Integer attemptCount;
    /** 最近尝试时间。 */
    @TableField("last_attempt_at")
    private LocalDateTime lastAttemptAt;
    /** 锁定截止时间。 */
    @TableField("lock_until")
    private LocalDateTime lockUntil;
    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;
    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

