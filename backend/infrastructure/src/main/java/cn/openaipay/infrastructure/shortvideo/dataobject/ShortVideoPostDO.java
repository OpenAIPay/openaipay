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
 * 短视频内容持久化实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("short_video_post")
public class ShortVideoPostDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 视频标识。 */
    @TableField("video_id")
    private String videoId;

    /** 作者用户ID。 */
    @TableField("creator_user_id")
    private Long creatorUserId;

    /** 视频文案。 */
    @TableField("caption")
    private String caption;

    /** 封面媒资ID。 */
    @TableField("cover_media_id")
    private String coverMediaId;

    /** 播放媒资ID。 */
    @TableField("playback_media_id")
    private String playbackMediaId;

    /** 视频时长毫秒。 */
    @TableField("duration_ms")
    private Long durationMs;

    /** 画面比例。 */
    @TableField("aspect_ratio")
    private String aspectRatio;

    /** 发布状态。 */
    @TableField("publish_status")
    private String publishStatus;

    /** 可见性状态。 */
    @TableField("visibility_status")
    private String visibilityStatus;

    /** 信息流权重。 */
    @TableField("feed_priority")
    private Integer feedPriority;

    /** 发布时间。 */
    @TableField("published_at")
    private LocalDateTime publishedAt;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
