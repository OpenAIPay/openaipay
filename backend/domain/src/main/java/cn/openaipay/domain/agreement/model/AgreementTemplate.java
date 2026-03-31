package cn.openaipay.domain.agreement.model;

import java.time.LocalDateTime;

/**
 * 协议模板。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public class AgreementTemplate {

    /** 模板编码。 */
    private final String templateCode;
    /** 模板版本。 */
    private final String templateVersion;
    /** 业务类型。 */
    private final AgreementBizType bizType;
    /** 标题。 */
    private final String title;
    /** 内容地址。 */
    private final String contentUrl;
    /** 内容摘要。 */
    private final String contentHash;
    /** 是否必签。 */
    private final boolean required;
    /** 是否生效。 */
    private final boolean active;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private final LocalDateTime updatedAt;

    public AgreementTemplate(String templateCode,
                             String templateVersion,
                             AgreementBizType bizType,
                             String title,
                             String contentUrl,
                             String contentHash,
                             boolean required,
                             boolean active,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
        this.templateCode = templateCode;
        this.templateVersion = templateVersion;
        this.bizType = bizType;
        this.title = title;
        this.contentUrl = contentUrl;
        this.contentHash = contentHash;
        this.required = required;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 获取模板编码。
     */
    public String getTemplateCode() {
        return templateCode;
    }

    /**
     * 获取模板版本信息。
     */
    public String getTemplateVersion() {
        return templateVersion;
    }

    /**
     * 获取业务类型信息。
     */
    public AgreementBizType getBizType() {
        return bizType;
    }

    /**
     * 获取业务数据。
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取内容URL信息。
     */
    public String getContentUrl() {
        return contentUrl;
    }

    /**
     * 获取内容信息。
     */
    public String getContentHash() {
        return contentHash;
    }

    /**
     * 判断是否必需信息。
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isActive() {
        return active;
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
