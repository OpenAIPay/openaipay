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
 * 用户安全Setting持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("user_security_setting")
public class UserSecuritySettingDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 生物识别启用开关 */
    @TableField("biometric_enabled")
    private Boolean biometricEnabled;

    /** 双因子模式 */
    @TableField("two_factor_mode")
    private String twoFactorMode;

    /** 风险等级 */
    @TableField("risk_level")
    private String riskLevel;

    /** 设备锁启用开关 */
    @TableField("device_lock_enabled")
    private Boolean deviceLockEnabled;

    /** 隐私模式启用开关 */
    @TableField("privacy_mode_enabled")
    private Boolean privacyModeEnabled;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
