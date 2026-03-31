package cn.openaipay.infrastructure.media.dataobject;

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
 * 媒体资源持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("media_asset")
public class MediaAssetDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 媒体资源ID。 */
    @TableField("media_id")
    private String mediaId;

    /** 所属用户ID。 */
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 媒体类型。 */
    @TableField("media_type")
    private String mediaType;

    /** 原始文件名。 */
    @TableField("original_name")
    private String originalName;

    /** 文件 MIME 类型。 */
    @TableField("mime_type")
    private String mimeType;

    /** 文件大小（字节）。 */
    @TableField("size_bytes")
    private Long sizeBytes;

    /** 压缩后文件大小（字节）。 */
    @TableField("compressed_size_bytes")
    private Long compressedSizeBytes;

    /** 宽度。 */
    @TableField("width")
    private Integer width;

    /** 高度。 */
    @TableField("height")
    private Integer height;

    /** 存储路径。 */
    @TableField("storage_path")
    private String storagePath;

    /** 缩略图路径。 */
    @TableField("thumbnail_path")
    private String thumbnailPath;

    /** 文件 SHA-256 摘要。 */
    @TableField("sha256")
    private String sha256;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
