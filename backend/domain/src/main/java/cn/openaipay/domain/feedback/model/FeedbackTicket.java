package cn.openaipay.domain.feedback.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户反馈单聚合。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
public class FeedbackTicket {

    /** 主键ID。 */
    private final Long id;
    /** 反馈单号。 */
    private final String feedbackNo;
    /** 用户ID。 */
    private final Long userId;
    /** 反馈类型。 */
    private final FeedbackType feedbackType;
    /** 来源渠道。 */
    private final String sourceChannel;
    /** 来源页面编码。 */
    private final String sourcePageCode;
    /** 标题。 */
    private final String title;
    /** 内容。 */
    private final String content;
    /** 联系方式手机号。 */
    private final String contactMobile;
    /** 附件地址列表。 */
    private final List<String> attachmentUrls;
    /** 当前状态。 */
    private FeedbackStatus status;
    /** 处理人。 */
    private String handledBy;
    /** 处理备注。 */
    private String handleNote;
    /** 处理时间。 */
    private LocalDateTime handledAt;
    /** 关闭时间。 */
    private LocalDateTime closedAt;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public FeedbackTicket(Long id,
                          String feedbackNo,
                          Long userId,
                          FeedbackType feedbackType,
                          String sourceChannel,
                          String sourcePageCode,
                          String title,
                          String content,
                          String contactMobile,
                          List<String> attachmentUrls,
                          FeedbackStatus status,
                          String handledBy,
                          String handleNote,
                          LocalDateTime handledAt,
                          LocalDateTime closedAt,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.id = id;
        this.feedbackNo = normalizeRequired(feedbackNo, "feedbackNo");
        this.userId = requirePositive(userId, "userId");
        this.feedbackType = feedbackType == null ? FeedbackType.PRODUCT_SUGGESTION : feedbackType;
        this.sourceChannel = defaultText(sourceChannel, "IOS_APP");
        this.sourcePageCode = normalizeOptional(sourcePageCode);
        this.title = normalizeOptional(title);
        this.content = normalizeContent(content);
        this.contactMobile = normalizeOptional(contactMobile);
        this.attachmentUrls = normalizeAttachments(attachmentUrls);
        this.status = status == null ? FeedbackStatus.SUBMITTED : status;
        this.handledBy = normalizeOptional(handledBy);
        this.handleNote = normalizeOptional(handleNote);
        this.handledAt = handledAt;
        this.closedAt = closedAt;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 创建业务数据。
     */
    public static FeedbackTicket create(String feedbackNo,
                                        Long userId,
                                        FeedbackType feedbackType,
                                        String sourceChannel,
                                        String sourcePageCode,
                                        String title,
                                        String content,
                                        String contactMobile,
                                        List<String> attachmentUrls,
                                        LocalDateTime now) {
        LocalDateTime createdAt = now == null ? LocalDateTime.now() : now;
        return new FeedbackTicket(
                null,
                feedbackNo,
                userId,
                feedbackType,
                sourceChannel,
                sourcePageCode,
                title,
                content,
                contactMobile,
                attachmentUrls,
                FeedbackStatus.SUBMITTED,
                null,
                null,
                null,
                null,
                createdAt,
                createdAt
        );
    }

    /**
     * 处理状态。
     */
    public void changeStatus(FeedbackStatus targetStatus, String handledBy, String handleNote, LocalDateTime now) {
        if (targetStatus == null || targetStatus == FeedbackStatus.SUBMITTED) {
            throw new IllegalArgumentException("targetStatus must not be SUBMITTED");
        }
        if (this.status == FeedbackStatus.CLOSED) {
            throw new IllegalStateException("closed feedback ticket can not be changed");
        }
        LocalDateTime handledTime = now == null ? LocalDateTime.now() : now;
        this.status = targetStatus;
        this.handledBy = normalizeRequired(handledBy, "handledBy");
        this.handleNote = normalizeOptional(handleNote);
        this.handledAt = handledTime;
        this.updatedAt = handledTime;
        if (targetStatus == FeedbackStatus.RESOLVED
                || targetStatus == FeedbackStatus.REJECTED
                || targetStatus == FeedbackStatus.CLOSED) {
            this.closedAt = handledTime;
        } else {
            this.closedAt = null;
        }
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取反馈NO信息。
     */
    public String getFeedbackNo() {
        return feedbackNo;
    }

    /**
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取反馈类型信息。
     */
    public FeedbackType getFeedbackType() {
        return feedbackType;
    }

    /**
     * 获取渠道信息。
     */
    public String getSourceChannel() {
        return sourceChannel;
    }

    /**
     * 获取页面编码。
     */
    public String getSourcePageCode() {
        return sourcePageCode;
    }

    /**
     * 获取业务数据。
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取内容信息。
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取联系人手机号信息。
     */
    public String getContactMobile() {
        return contactMobile;
    }

    /**
     * 获取业务数据。
     */
    public List<String> getAttachmentUrls() {
        return attachmentUrls;
    }

    /**
     * 获取状态。
     */
    public FeedbackStatus getStatus() {
        return status;
    }

    /**
     * 按条件获取记录。
     */
    public String getHandledBy() {
        return handledBy;
    }

    /**
     * 获取业务数据。
     */
    public String getHandleNote() {
        return handleNote;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getHandledAt() {
        return handledAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getClosedAt() {
        return closedAt;
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

    private static String normalizeContent(String raw) {
        String normalized = normalizeRequired(raw, "content");
        if (normalized.length() < 10) {
            throw new IllegalArgumentException("content must be at least 10 characters");
        }
        if (normalized.length() > 200) {
            throw new IllegalArgumentException("content length must be less than or equal to 200");
        }
        return normalized;
    }

    private static List<String> normalizeAttachments(List<String> attachmentUrls) {
        if (attachmentUrls == null || attachmentUrls.isEmpty()) {
            return List.of();
        }
        if (attachmentUrls.size() > 4) {
            throw new IllegalArgumentException("attachmentUrls size must be less than or equal to 4");
        }
        List<String> normalized = new ArrayList<>();
        for (String attachmentUrl : attachmentUrls) {
            String value = normalizeRequired(attachmentUrl, "attachmentUrl");
            normalized.add(value);
        }
        return List.copyOf(normalized);
    }

    private static String defaultText(String raw, String defaultValue) {
        String normalized = normalizeOptional(raw);
        return normalized == null ? defaultValue : normalized;
    }

    private static String normalizeRequired(String raw, String fieldName) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}
