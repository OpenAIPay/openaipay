package cn.openaipay.infrastructure.shortvideo.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 短视频评论点赞关系持久化实体。
 *
 * @author: tenggk.ai
 * @date: 2026/04/03
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("short_video_comment_like")
public class ShortVideoCommentLikeDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 评论标识。 */
    @TableField("comment_id")
    private String commentId;

    /** 用户标识。 */
    @TableField("user_id")
    private Long userId;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
