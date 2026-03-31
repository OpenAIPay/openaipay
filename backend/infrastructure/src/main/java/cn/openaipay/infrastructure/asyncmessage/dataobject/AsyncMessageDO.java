package cn.openaipay.infrastructure.asyncmessage.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 系统内可靠异步消息持久化对象。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("async_message")
public class AsyncMessageDO {
    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 消息主题 */
    @TableField("topic")
    private String topic;

    /** 消息唯一键 */
    @TableField("message_key")
    private String messageKey;

    /** 消息载荷内容 */
    @TableField("payload")
    private String payload;

    /** 当前状态编码 */
    @TableField("status")
    private String status;

    /** 当前重试次数 */
    @TableField("retry_count")
    private Integer retryCount;

    /** 最大重试次数 */
    @TableField("max_retry_count")
    private Integer maxRetryCount;

    /** 下次重试时间 */
    @TableField("next_retry_at")
    private LocalDateTime nextRetryAt;

    /** 处理开始时间 */
    @TableField("processing_started_at")
    private LocalDateTime processingStartedAt;

    /** 最近一次失败原因 */
    @TableField("last_error")
    private String lastError;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
