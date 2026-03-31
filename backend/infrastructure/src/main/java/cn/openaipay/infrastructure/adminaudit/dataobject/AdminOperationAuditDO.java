package cn.openaipay.infrastructure.adminaudit.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 后台操作审计记录持久化对象。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("admin_operation_audit")
public class AdminOperationAuditDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("trace_id")
    private String traceId;

    @TableField("admin_id")
    private Long adminId;

    @TableField("admin_username")
    private String adminUsername;

    @TableField("request_method")
    private String requestMethod;

    @TableField("request_path")
    private String requestPath;

    @TableField("request_query")
    private String requestQuery;

    @TableField("request_body")
    private String requestBody;

    @TableField("result_status")
    private String resultStatus;

    @TableField("error_message")
    private String errorMessage;

    @TableField("cost_ms")
    private Long costMs;

    @TableField("client_ip")
    private String clientIp;

    @TableField("user_agent")
    private String userAgent;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
