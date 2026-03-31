package cn.openaipay.domain.user.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户资料模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class UserProfile {

    /** 用户ID */
    private final Long userId;
    /** 昵称 */
    private String nickname;
    /** 头像地址 */
    private String avatarUrl;
    /** 国家编码 */
    private String countryCode;
    /** 手机号 */
    private String mobile;
    /** 脱敏实名名称 */
    private String maskedRealName;
    /** 身份证号 */
    private String idCardNo;
    /** 性别 */
    private String gender;
    /** 地区 */
    private String region;
    /** 生日 */
    private LocalDate birthday;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public UserProfile(
            Long userId,
            String nickname,
            String avatarUrl,
            String countryCode,
            String mobile,
            String maskedRealName,
            String idCardNo,
            String gender,
            String region,
            LocalDate birthday,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.userId = userId;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.countryCode = countryCode;
        this.mobile = mobile;
        this.maskedRealName = maskedRealName;
        this.idCardNo = idCardNo;
        this.gender = gender;
        this.region = region;
        this.birthday = birthday;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 处理OF信息。
     */
    public static UserProfile defaultOf(Long userId, String nickname, String avatarUrl, String countryCode,
                                        String mobile, String maskedRealName, String idCardNo, LocalDateTime now) {
        return new UserProfile(
                userId,
                nickname,
                avatarUrl,
                countryCode,
                mobile,
                maskedRealName,
                idCardNo,
                "UNKNOWN",
                "CN",
                null,
                now,
                now
        );
    }

    /**
     * 更新基础资料。
     */
    public void updateBasicProfile(String nickname, String avatarUrl, String mobile,
                                   String gender, String region, LocalDate birthday) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl;
        }
        if (mobile != null) {
            this.mobile = mobile;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (region != null) {
            this.region = region;
        }
        if (birthday != null) {
            this.birthday = birthday;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新实名资料。
     */
    public void updateIdentityProfile(String maskedRealName, String idCardNo, String gender, LocalDate birthday) {
        if (maskedRealName != null) {
            this.maskedRealName = maskedRealName;
        }
        if (idCardNo != null) {
            this.idCardNo = idCardNo;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (birthday != null) {
            this.birthday = birthday;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取业务数据。
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 获取URL信息。
     */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * 获取编码。
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * 获取手机号信息。
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * 获取业务数据。
     */
    public String getMaskedRealName() {
        return maskedRealName;
    }

    /**
     * 获取ID卡NO信息。
     */
    public String getIdCardNo() {
        return idCardNo;
    }

    /**
     * 获取业务数据。
     */
    public String getGender() {
        return gender;
    }

    /**
     * 获取业务数据。
     */
    public String getRegion() {
        return region;
    }

    /**
     * 获取业务数据。
     */
    public LocalDate getBirthday() {
        return birthday;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
