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
 * 短视频评论持久化实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("short_video_comment")
public class ShortVideoCommentDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 评论标识。 */
    @TableField("comment_id")
    private String commentId;

    /** 视频标识。 */
    @TableField("video_id")
    private String videoId;

    /** 父评论标识。 */
    @TableField("parent_comment_id")
    private String parentCommentId;

    /** 根评论标识。 */
    @TableField("root_comment_id")
    private String rootCommentId;

    /** 用户标识。 */
    @TableField("user_id")
    private Long userId;

    /** 评论内容。 */
    @TableField("content")
    private String content;

    /** 图片媒体标识。 */
    @TableField("image_media_id")
    private String imageMediaId;

    /** 点赞数。 */
    @TableField("like_count")
    private Long likeCount;

    /** 回复数。 */
    @TableField("reply_count")
    private Long replyCount;

    /** 评论状态。 */
    @TableField("status")
    private String status;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
