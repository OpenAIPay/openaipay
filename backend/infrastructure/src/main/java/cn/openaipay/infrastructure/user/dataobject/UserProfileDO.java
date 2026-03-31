package cn.openaipay.infrastructure.user.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户资料持久化实体
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("user_profile")
public class UserProfileDO {

    /** 数据库主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Setter(AccessLevel.NONE)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 昵称 */
    @TableField("nickname")
    private String nickname;

    /** 头像地址 */
    @TableField("avatar_url")
    private String avatarUrl;

    /** 国家编码 */
    @TableField("country_code")
    private String countryCode;

    /** 手机号 */
    @TableField("mobile")
    private String mobile;

    /** 脱敏实名名称 */
    @TableField("masked_real_name")
    private String maskedRealName;

    /** 身份证号 */
    @TableField("id_card_no")
    private String idCardNo;

    /** 性别 */
    @TableField("gender")
    private String gender;

    /** 地区 */
    @TableField("region")
    private String region;

    /** 生日 */
    @TableField("birthday")
    private LocalDate birthday;

    /** 记录创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 记录更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
