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
 * 应用定义实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("app_info")
public class AppInfoDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 应用编码。 */
    @TableField("app_code")
    private String appCode;

    /** 应用名称。 */
    @TableField("app_name")
    private String appName;

    /** 应用状态。 */
    @TableField("status")
    private String status;

    /** 版本提示开关。 */
    @TableField("version_prompt_enabled")
    private Boolean versionPromptEnabled;

    /** 演示账号自动登录开关。 */
    @TableField("demo_auto_login_enabled")
    private Boolean demoAutoLoginEnabled;

    /** 登录本机注册校验开关。 */
    @TableField("login_device_binding_check_enabled")
    private Boolean loginDeviceBindingCheckEnabled;
    /** 演示模板登录号。 */
    @TableField("demo_template_login_id")
    private String demoTemplateLoginId;
    /** 演示联系人登录号。 */
    @TableField("demo_contact_login_id")
    private String demoContactLoginId;
    /** 演示注册默认密码。 */
    @TableField("demo_login_password")
    private String demoLoginPassword;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
