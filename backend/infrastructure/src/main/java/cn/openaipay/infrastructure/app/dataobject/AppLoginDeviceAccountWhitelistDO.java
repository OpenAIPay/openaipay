package cn.openaipay.infrastructure.app.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 登录设备白名单账号实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/21
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("app_login_device_account_whitelist")
public class AppLoginDeviceAccountWhitelistDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 应用编码。 */
    @TableField("app_code")
    private String appCode;

    /** 设备ID。 */
    @TableField("device_id")
    private String deviceId;

    /** 登录账号。 */
    @TableField("login_id")
    private String loginId;

    /** 昵称。 */
    @TableField("nickname")
    private String nickname;

    /** 是否启用。 */
    @TableField("enabled")
    private Boolean enabled;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
