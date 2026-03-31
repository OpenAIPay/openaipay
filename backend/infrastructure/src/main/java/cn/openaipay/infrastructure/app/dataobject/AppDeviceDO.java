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
 * App 设备实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("app_device")
public class AppDeviceDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 设备ID。 */
    @TableField("device_id")
    private String deviceId;

    /** 应用编码。 */
    @TableField("app_code")
    private String appCode;

    /** 客户端ID列表文本。 */
    @TableField("client_id_list_text")
    private String clientIdListText;

    /** 设备状态。 */
    @TableField("status")
    private String status;

    /** 安装时间。 */
    @TableField("installed_at")
    private LocalDateTime installedAt;

    /** 最近启动时间。 */
    @TableField("started_at")
    private LocalDateTime startedAt;

    /** 最近打开时间。 */
    @TableField("last_opened_at")
    private LocalDateTime lastOpenedAt;

    /** 当前版本ID。 */
    @TableField("current_app_version_id")
    private Long currentAppVersionId;

    /** 当前 iOS 包ID。 */
    @TableField("current_ios_package_id")
    private Long currentIosPackageId;

    /** 应用更新时间。 */
    @TableField("app_updated_at")
    private LocalDateTime appUpdatedAt;

    /** 设备品牌。 */
    @TableField("device_brand")
    private String deviceBrand;

    /** 系统版本号。 */
    @TableField("os_version")
    private String osVersion;

    /** 当前登录用户ID。 */
    @TableField("user_id")
    private Long userId;

    /** 当前登录爱付号。 */
    @TableField("aipay_uid")
    private String aipayUid;

    /** 当前登录账号。 */
    @TableField("login_id")
    private String loginId;

    /** 账户状态。 */
    @TableField("account_status")
    private String accountStatus;

    /** 实名等级。 */
    @TableField("kyc_level")
    private String kycLevel;

    /** 昵称。 */
    @TableField("nickname")
    private String nickname;

    /** 头像地址。 */
    @TableField("avatar_url")
    private String avatarUrl;

    /** 手机号。 */
    @TableField("mobile")
    private String mobile;

    /** 脱敏姓名。 */
    @TableField("masked_real_name")
    private String maskedRealName;

    /** 脱敏证件号。 */
    @TableField("id_card_no_masked")
    private String idCardNoMasked;

    /** 国家编码。 */
    @TableField("country_code")
    private String countryCode;

    /** 性别。 */
    @TableField("gender")
    private String gender;

    /** 地区。 */
    @TableField("region")
    private String region;

    /** 最近登录时间。 */
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
