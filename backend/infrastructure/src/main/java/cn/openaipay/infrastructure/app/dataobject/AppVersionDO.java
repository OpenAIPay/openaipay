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
 * 应用版本实体。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("app_version")
public class AppVersionDO {

    /** 主键ID。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 版本编码。 */
    @TableField("version_code")
    private String versionCode;

    /** 应用编码。 */
    @TableField("app_code")
    private String appCode;

    /** 客户端类型。 */
    @TableField("client_type")
    private String clientType;

    /** 应用版本号。 */
    @TableField("app_version_no")
    private String appVersionNo;

    /** 更新类型。 */
    @TableField("update_type")
    private String updateType;

    /** 更新提示频率。 */
    @TableField("update_prompt_frequency")
    private String updatePromptFrequency;

    /** 用户提示信息。 */
    @TableField("version_description")
    private String versionDescription;
    /** 发布者备注。 */
    @TableField("publisher_remark")
    private String publisherRemark;

    /** 正式发布区域列表文本。 */
    @TableField("release_region_list_text")
    private String releaseRegionListText;

    /** 灰度定向区域列表文本。 */
    @TableField("target_region_list_text")
    private String targetRegionListText;

    /** 最低支持版本号。 */
    @TableField("min_supported_version_no")
    private String minSupportedVersionNo;

    /** 是否最新已发布版本。 */
    @TableField("latest_published_version")
    private Boolean latestPublishedVersion;

    /** 版本状态。 */
    @TableField("status")
    private String status;

    /** 创建时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
