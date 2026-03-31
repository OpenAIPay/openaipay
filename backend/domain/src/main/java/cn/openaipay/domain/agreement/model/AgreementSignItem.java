package cn.openaipay.domain.agreement.model;

import java.time.LocalDateTime;

/**
 * 协议签约明细。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public class AgreementSignItem {

    /** 签约单号。 */
    private final String signNo;
    /** 模板编码。 */
    private final String templateCode;
    /** 模板版本。 */
    private final String templateVersion;
    /** 协议标题。 */
    private final String title;
    /** 协议地址。 */
    private final String contentUrl;
    /** 协议摘要。 */
    private final String contentHash;
    /** 是否同意。 */
    private final boolean accepted;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private final LocalDateTime updatedAt;

    public AgreementSignItem(String signNo,
                             String templateCode,
                             String templateVersion,
                             String title,
                             String contentUrl,
                             String contentHash,
                             boolean accepted,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
        this.signNo = signNo;
        this.templateCode = templateCode;
        this.templateVersion = templateVersion;
        this.title = title;
        this.contentUrl = contentUrl;
        this.contentHash = contentHash;
        this.accepted = accepted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 处理业务数据。
     */
    public static AgreementSignItem accepted(String signNo,
                                             AgreementTemplate template,
                                             LocalDateTime now) {
        return new AgreementSignItem(
                signNo,
                template.getTemplateCode(),
                template.getTemplateVersion(),
                template.getTitle(),
                template.getContentUrl(),
                template.getContentHash(),
                true,
                now,
                now
        );
    }

    /**
     * 获取NO信息。
     */
    public String getSignNo() {
        return signNo;
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
     * 判断是否业务数据。
     */
    public boolean isAccepted() {
        return accepted;
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
