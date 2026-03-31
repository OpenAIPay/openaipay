package cn.openaipay.infrastructure.user.dataobject;

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
 * 用户隐私Setting持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("user_privacy_setting")
public class UserPrivacySettingDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 手机号可搜索 */
    @TableField("allow_search_by_mobile")
    private Boolean allowSearchByMobile;

    /** 用户号可搜索 */
    @TableField("allow_search_by_aipay_uid")
    private Boolean allowSearchByAipayUid;

    /** 隐藏实名名称 */
    @TableField("hide_real_name")
    private Boolean hideRealName;

    /** 个性化推荐启用开关 */
    @TableField("personalized_recommendation_enabled")
    private Boolean personalizedRecommendationEnabled;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
