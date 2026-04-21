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
 * 短视频统计持久化实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("short_video_stats")
public class ShortVideoStatsDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 视频标识。 */
    @TableField("video_id")
    private String videoId;

    /** 点赞数。 */
    @TableField("like_count")
    private Long likeCount;

    /** 收藏数。 */
    @TableField("favorite_count")
    private Long favoriteCount;

    /** 评论数。 */
    @TableField("comment_count")
    private Long commentCount;

    /** 播放数。 */
    @TableField("play_count")
    private Long playCount;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
