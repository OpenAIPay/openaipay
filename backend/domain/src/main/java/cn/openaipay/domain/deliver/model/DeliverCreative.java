package cn.openaipay.domain.deliver.model;

import java.time.LocalDateTime;

/**
 * 投放创意模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public class DeliverCreative {

    /** 主键ID。 */
    private final Long id;
    /** 创意编码。 */
    private final String creativeCode;
    /** 创意名称。 */
    private final String creativeName;
    /** 所属投放单元编码。 */
    private final String unitCode;
    /** 关联素材编码。 */
    private final String materialCode;
    /** 落地页地址。 */
    private String landingUrl;
    /** 端内跳转 Schema 配置。 */
    private String schemaJson;
    /** 优先级。 */
    private final Integer priority;
    /** 权重。 */
    private final Integer weight;
    /** 展示顺序。 */
    private final Integer displayOrder;
    /** 是否兜底创意。 */
    private final boolean fallback;
    /** 预览图。 */
    private final String previewImage;
    /** 发布状态。 */
    private final DeliverPublishStatus status;
    /** 生效开始时间。 */
    private final LocalDateTime activeFrom;
    /** 生效结束时间。 */
    private final LocalDateTime activeTo;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private final LocalDateTime updatedAt;
    /** 素材类型。 */
    private String materialType;
    /** 素材标题。 */
    private String materialTitle;
    /** 素材图片地址。 */
    private String imageUrl;

    public DeliverCreative(Long id,
                           String creativeCode,
                           String creativeName,
                           String unitCode,
                           String materialCode,
                           String landingUrl,
                           String schemaJson,
                           Integer priority,
                           Integer weight,
                           Integer displayOrder,
                           boolean fallback,
                           String previewImage,
                           DeliverPublishStatus status,
                           LocalDateTime activeFrom,
                           LocalDateTime activeTo,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
        this.id = id;
        this.creativeCode = creativeCode;
        this.creativeName = creativeName;
        this.unitCode = unitCode;
        this.materialCode = materialCode;
        this.landingUrl = landingUrl;
        this.schemaJson = schemaJson;
        this.priority = priority;
        this.weight = weight;
        this.displayOrder = displayOrder;
        this.fallback = fallback;
        this.previewImage = previewImage;
        this.status = status;
        this.activeFrom = activeFrom;
        this.activeTo = activeTo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 判断是否AT信息。
     */
    public boolean isPublishedAt(LocalDateTime now) {
        if (status != DeliverPublishStatus.PUBLISHED) {
            return false;
        }
        LocalDateTime targetTime = now == null ? LocalDateTime.now() : now;
        if (activeFrom != null && activeFrom.isAfter(targetTime)) {
            return false;
        }
        return activeTo == null || !activeTo.isBefore(targetTime);
    }

    /**
     * 处理素材信息。
     */
    public void fillMaterial(DeliverMaterial material) {
        if (material == null) {
            return;
        }
        materialType = material.materialType();
        materialTitle = material.title();
        imageUrl = material.imageUrl();
        if (!hasText(landingUrl)) {
            landingUrl = material.landingUrl();
        }
        if (!hasText(schemaJson)) {
            schemaJson = material.schemaJson();
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取创意编码。
     */
    public String getCreativeCode() {
        return creativeCode;
    }

    /**
     * 获取创意信息。
     */
    public String getCreativeName() {
        return creativeName;
    }

    /**
     * 获取单元编码。
     */
    public String getUnitCode() {
        return unitCode;
    }

    /**
     * 获取素材编码。
     */
    public String getMaterialCode() {
        return materialCode;
    }

    /**
     * 获取URL信息。
     */
    public String getLandingUrl() {
        return landingUrl;
    }

    /**
     * 获取业务数据。
     */
    public String getSchemaJson() {
        return schemaJson;
    }

    /**
     * 获取业务数据。
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * 获取业务数据。
     */
    public Integer getWeight() {
        return weight;
    }

    /**
     * 获取订单信息。
     */
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isFallback() {
        return fallback;
    }

    /**
     * 获取业务数据。
     */
    public String getPreviewImage() {
        return previewImage;
    }

    /**
     * 获取状态。
     */
    public DeliverPublishStatus getStatus() {
        return status;
    }

    /**
     * 获取业务数据。
     */
    public LocalDateTime getActiveFrom() {
        return activeFrom;
    }

    /**
     * 获取TO信息。
     */
    public LocalDateTime getActiveTo() {
        return activeTo;
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

    /**
     * 获取素材类型信息。
     */
    public String getMaterialType() {
        return materialType;
    }

    /**
     * 获取素材信息。
     */
    public String getMaterialTitle() {
        return materialTitle;
    }

    /**
     * 获取URL信息。
     */
    public String getImageUrl() {
        return imageUrl;
    }
}
