package cn.openaipay.domain.deliver.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 投放展位模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public class Position {

    /** 主键ID。 */
    private final Long id;
    /** 展位编码。 */
    private final String positionCode;
    /** 展位名称。 */
    private final String positionName;
    /** 展位类型。 */
    private final DeliverPositionType positionType;
    /** 展位预览图。 */
    private final String previewImage;
    /** 轮播间隔。 */
    private final Integer slideInterval;
    /** 最大展示数量。 */
    private final Integer maxDisplayCount;
    /** 排序类型。 */
    private final DeliverSortType sortType;
    /** 排序规则。 */
    private final String sortRule;
    /** 是否需要兜底创意。 */
    private final boolean needFallback;
    /** 发布状态。 */
    private final DeliverPublishStatus status;
    /** 备注。 */
    private final String memo;
    /** 发布时间。 */
    private final LocalDateTime publishedAt;
    /** 生效开始时间。 */
    private final LocalDateTime activeFrom;
    /** 生效结束时间。 */
    private final LocalDateTime activeTo;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private final LocalDateTime updatedAt;
    /** 关联创意列表。 */
    private List<DeliverCreative> deliverCreativeList;

    public Position(Long id,
                    String positionCode,
                    String positionName,
                    DeliverPositionType positionType,
                    String previewImage,
                    Integer slideInterval,
                    Integer maxDisplayCount,
                    DeliverSortType sortType,
                    String sortRule,
                    boolean needFallback,
                    DeliverPublishStatus status,
                    String memo,
                    LocalDateTime publishedAt,
                    LocalDateTime activeFrom,
                    LocalDateTime activeTo,
                    LocalDateTime createdAt,
                    LocalDateTime updatedAt,
                    List<DeliverCreative> deliverCreativeList) {
        this.id = id;
        this.positionCode = positionCode;
        this.positionName = positionName;
        this.positionType = positionType;
        this.previewImage = previewImage;
        this.slideInterval = slideInterval;
        this.maxDisplayCount = maxDisplayCount;
        this.sortType = sortType;
        this.sortRule = sortRule;
        this.needFallback = needFallback;
        this.status = status;
        this.memo = memo;
        this.publishedAt = publishedAt;
        this.activeFrom = activeFrom;
        this.activeTo = activeTo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        setDeliverCreativeList(deliverCreativeList);
    }

    /**
     * 处理用于投放信息。
     */
    public Position copyForDeliver() {
        return new Position(
                id,
                positionCode,
                positionName,
                positionType,
                previewImage,
                slideInterval,
                maxDisplayCount,
                sortType,
                sortRule,
                needFallback,
                status,
                memo,
                publishedAt,
                activeFrom,
                activeTo,
                createdAt,
                updatedAt,
                List.of()
        );
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
     * 处理创意数量信息。
     */
    public int creativeCount() {
        return deliverCreativeList == null ? 0 : deliverCreativeList.size();
    }

    /**
     * 处理SET投放创意列表。
     */
    public void setDeliverCreativeList(List<DeliverCreative> creatives) {
        deliverCreativeList = creatives == null ? new ArrayList<>() : new ArrayList<>(creatives);
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取广告位编码。
     */
    public String getPositionCode() {
        return positionCode;
    }

    /**
     * 获取广告位信息。
     */
    public String getPositionName() {
        return positionName;
    }

    /**
     * 获取广告位类型信息。
     */
    public DeliverPositionType getPositionType() {
        return positionType;
    }

    /**
     * 获取业务数据。
     */
    public String getPreviewImage() {
        return previewImage;
    }

    /**
     * 获取业务数据。
     */
    public Integer getSlideInterval() {
        return slideInterval;
    }

    /**
     * 获取MAX数量信息。
     */
    public Integer getMaxDisplayCount() {
        return maxDisplayCount;
    }

    /**
     * 获取业务数据。
     */
    public DeliverSortType getSortType() {
        return sortType;
    }

    /**
     * 获取规则。
     */
    public String getSortRule() {
        return sortRule;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isNeedFallback() {
        return needFallback;
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
    public String getMemo() {
        return memo;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getPublishedAt() {
        return publishedAt;
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
     * 获取投放创意列表。
     */
    public List<DeliverCreative> getDeliverCreativeList() {
        return deliverCreativeList;
    }
}
