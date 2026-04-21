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
 * 短视频互动持久化实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("short_video_engagement")
public class ShortVideoEngagementDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 互动标识。 */
    @TableField("engagement_id")
    private String engagementId;

    /** 视频标识。 */
    @TableField("video_id")
    private String videoId;

    /** 用户标识。 */
    @TableField("user_id")
    private Long userId;

    /** 是否已点赞。 */
    @TableField("liked")
    private Boolean liked;

    /** 是否已收藏。 */
    @TableField("favorited")
    private Boolean favorited;

    /** 点赞时间。 */
    @TableField("liked_at")
    private LocalDateTime likedAt;

    /** 收藏时间。 */
    @TableField("favorited_at")
    private LocalDateTime favoritedAt;

    /** 最近浏览时间。 */
    @TableField("last_viewed_at")
    private LocalDateTime lastViewedAt;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
