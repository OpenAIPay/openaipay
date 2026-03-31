package cn.openaipay.infrastructure.app.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * iOS 安装包实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("app_ios_package")
public class AppIosPackageDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** iOS 包编码。 */
    @TableField("ios_code")
    private String iosCode;

    /** 应用编码。 */
    @TableField("app_code")
    private String appCode;

    /** 版本编码。 */
    @TableField("version_code")
    private String versionCode;

    /** App Store 下载地址。 */
    @TableField("app_store_url")
    private String appStoreUrl;

    /** 安装包大小（字节）。 */
    @TableField("package_size_bytes")
    private Long packageSizeBytes;

    /** 安装包校验值。 */
    @TableField("md5")
    private String md5;

    /** 提审时间。 */
    @TableField("review_submitted_at")
    private LocalDateTime reviewSubmittedAt;

    /** 发布时间。 */
    @TableField("published_at")
    private LocalDateTime publishedAt;

    /** 安装包发布状态。 */
    @TableField("release_status")
    private String releaseStatus;

    /** 提审人。 */
    @TableField("review_submitted_by")
    private String reviewSubmittedBy;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
