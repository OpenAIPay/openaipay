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
 * App 访问记录实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("app_visit_record")
public class AppVisitRecordDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 设备ID。 */
    @TableField("device_id")
    private String deviceId;

    /** 应用编码。 */
    @TableField("app_code")
    private String appCode;

    /** 客户端ID。 */
    @TableField("client_id")
    private String clientId;

    /** 请求 IP 地址。 */
    @TableField("ip_address")
    private String ipAddress;

    /** 位置描述。 */
    @TableField("location_info")
    private String locationInfo;

    /** 租户编码。 */
    @TableField("tenant_code")
    private String tenantCode;

    /** 客户端类型。 */
    @TableField("client_type")
    private String clientType;

    /** 网络类型。 */
    @TableField("network_type")
    private String networkType;

    /** 应用版本ID。 */
    @TableField("app_version_id")
    private Long appVersionId;

    /** 设备品牌。 */
    @TableField("device_brand")
    private String deviceBrand;

    /** 系统版本号。 */
    @TableField("os_version")
    private String osVersion;

    /** 接口名称。 */
    @TableField("api_name")
    private String apiName;

    /** 请求参数摘要。 */
    @TableField("request_params_text")
    private String requestParamsText;

    /** 调用时间。 */
    @TableField("called_at")
    private LocalDateTime calledAt;

    /** 结果摘要。 */
    @TableField("result_summary")
    private String resultSummary;

    /** 耗时（毫秒）。 */
    @TableField("duration_ms")
    private Long durationMs;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

}
