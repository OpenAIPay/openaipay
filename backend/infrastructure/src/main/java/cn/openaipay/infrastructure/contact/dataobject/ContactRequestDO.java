package cn.openaipay.infrastructure.contact.dataobject;

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
 * 好友申请持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("contact_request")
public class ContactRequestDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 请求号。 */
    @TableField("request_no")
    private String requestNo;

    /** 申请发起方用户ID。 */
    @TableField("requester_user_id")
    private Long requesterUserId;

    /** 目标用户ID。 */
    @TableField("target_user_id")
    private Long targetUserId;

    /** 申请附言。 */
    @TableField("apply_message")
    private String applyMessage;

    /** 当前状态。 */
    @TableField("status")
    private String status;

    /** 处理人用户ID。 */
    @TableField("handled_by_user_id")
    private Long handledByUserId;

    /** 处理时间。 */
    @TableField("handled_at")
    private LocalDateTime handledAt;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
