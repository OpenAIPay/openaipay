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
 * 好友关系持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("contact_friendship")
public class ContactFriendshipDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 所属用户ID。 */
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 好友用户ID。 */
    @TableField("friend_user_id")
    private Long friendUserId;

    /** 备注。 */
    @TableField("remark")
    private String remark;

    /** 来源请求号。 */
    @TableField("source_request_no")
    private String sourceRequestNo;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
