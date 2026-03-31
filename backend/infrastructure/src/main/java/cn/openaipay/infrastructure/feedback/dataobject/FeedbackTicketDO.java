package cn.openaipay.infrastructure.feedback.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 反馈单持久化实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("feedback_ticket")
public class FeedbackTicketDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 反馈单号。 */
    @TableField("feedback_no")
    private String feedbackNo;

    /** 用户ID。 */
    @TableField("user_id")
    private Long userId;

    /** 反馈类型。 */
    @TableField("feedback_type")
    private String feedbackType;

    /** 来源渠道。 */
    @TableField("source_channel")
    private String sourceChannel;

    /** 来源页面编码。 */
    @TableField("source_page_code")
    private String sourcePageCode;

    /** 标题。 */
    @TableField("title")
    private String title;

    /** 内容。 */
    @TableField("content")
    private String content;

    /** 联系方式手机号。 */
    @TableField("contact_mobile")
    private String contactMobile;

    /** 附件地址文本，按换行分隔。 */
    @TableField("attachment_urls_text")
    private String attachmentUrlsText;

    /** 当前状态。 */
    @TableField("status")
    private String status;

    /** 处理人。 */
    @TableField("handled_by")
    private String handledBy;

    /** 处理备注。 */
    @TableField("handle_note")
    private String handleNote;

    /** 处理时间。 */
    @TableField("handled_at")
    private LocalDateTime handledAt;

    /** 关闭时间。 */
    @TableField("closed_at")
    private LocalDateTime closedAt;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
