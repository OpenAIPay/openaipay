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
 * App 行为埋点实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("app_behavior_event_log")
public class AppBehaviorEventDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 事件ID。 */
    @TableField("event_id")
    private String eventId;

    /** 会话ID。 */
    @TableField("session_id")
    private String sessionId;

    /** 应用编码。 */
    @TableField("app_code")
    private String appCode;

    /** 事件名称。 */
    @TableField("event_name")
    private String eventName;

    /** 事件类型。 */
    @TableField("event_type")
    private String eventType;

    /** 事件编码。 */
    @TableField("event_code")
    private String eventCode;

    /** 页面名称。 */
    @TableField("page_name")
    private String pageName;

    /** 动作名称。 */
    @TableField("action_name")
    private String actionName;

    /** 结果状态。 */
    @TableField("result_status")
    private String resultStatus;

    /** 链路追踪ID。 */
    @TableField("trace_id")
    private String traceId;

    /** 设备ID。 */
    @TableField("device_id")
    private String deviceId;

    /** 客户端ID。 */
    @TableField("client_id")
    private String clientId;

    /** 用户ID。 */
    @TableField("user_id")
    private Long userId;

    /** 爱付UID。 */
    @TableField("aipay_uid")
    private String aipayUid;

    /** 登录账号。 */
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

    /** 手机号。 */
    @TableField("mobile")
    private String mobile;

    /** IP地址。 */
    @TableField("ip_address")
    private String ipAddress;

    /** 位置信息。 */
    @TableField("location_info")
    private String locationInfo;

    /** 租户编码。 */
    @TableField("tenant_code")
    private String tenantCode;

    /** 网络类型。 */
    @TableField("network_type")
    private String networkType;

    /** 应用版本号。 */
    @TableField("app_version_no")
    private String appVersionNo;

    /** 应用构建号。 */
    @TableField("app_build_no")
    private String appBuildNo;

    /** 设备品牌。 */
    @TableField("device_brand")
    private String deviceBrand;

    /** 设备型号。 */
    @TableField("device_model")
    private String deviceModel;

    /** 设备名称。 */
    @TableField("device_name")
    private String deviceName;

    /** 设备类型。 */
    @TableField("device_type")
    private String deviceType;

    /** 系统名称。 */
    @TableField("os_name")
    private String osName;

    /** 系统版本。 */
    @TableField("os_version")
    private String osVersion;

    /** 区域信息。 */
    @TableField("locale")
    private String locale;

    /** 时区。 */
    @TableField("timezone")
    private String timezone;

    /** 语言。 */
    @TableField("language")
    private String language;

    /** 国家编码。 */
    @TableField("country_code")
    private String countryCode;

    /** 运营商。 */
    @TableField("carrier_name")
    private String carrierName;

    /** 屏幕宽度。 */
    @TableField("screen_width")
    private Integer screenWidth;

    /** 屏幕高度。 */
    @TableField("screen_height")
    private Integer screenHeight;

    /** 可视宽度。 */
    @TableField("viewport_width")
    private Integer viewportWidth;

    /** 可视高度。 */
    @TableField("viewport_height")
    private Integer viewportHeight;

    /** 业务耗时。 */
    @TableField("duration_ms")
    private Long durationMs;

    /** 登录时长。 */
    @TableField("login_duration_ms")
    private Long loginDurationMs;

    /** 事件发生时间。 */
    @TableField("event_occurred_at")
    private LocalDateTime eventOccurredAt;

    /** 扩展JSON。 */
    @TableField("payload_json")
    private String payloadJson;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
