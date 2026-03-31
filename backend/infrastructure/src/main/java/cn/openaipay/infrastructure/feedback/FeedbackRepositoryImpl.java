package cn.openaipay.infrastructure.feedback;

import cn.openaipay.domain.feedback.model.FeedbackStatus;
import cn.openaipay.domain.feedback.model.FeedbackTicket;
import cn.openaipay.domain.feedback.model.FeedbackType;
import cn.openaipay.domain.feedback.repository.FeedbackRepository;
import cn.openaipay.infrastructure.feedback.dataobject.FeedbackTicketDO;
import cn.openaipay.infrastructure.feedback.mapper.FeedbackTicketMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 反馈单仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
@Repository
public class FeedbackRepositoryImpl implements FeedbackRepository {

    /** 工单信息 */
    private final FeedbackTicketMapper feedbackTicketMapper;

    public FeedbackRepositoryImpl(FeedbackTicketMapper feedbackTicketMapper) {
        this.feedbackTicketMapper = feedbackTicketMapper;
    }

    /**
     * 保存业务数据。
     */
    @Override
    public FeedbackTicket save(FeedbackTicket ticket) {
        FeedbackTicketDO entity = resolveDO(ticket);
        entity.setFeedbackNo(ticket.getFeedbackNo());
        entity.setUserId(ticket.getUserId());
        entity.setFeedbackType(ticket.getFeedbackType().name());
        entity.setSourceChannel(ticket.getSourceChannel());
        entity.setSourcePageCode(ticket.getSourcePageCode());
        entity.setTitle(ticket.getTitle());
        entity.setContent(ticket.getContent());
        entity.setContactMobile(ticket.getContactMobile());
        entity.setAttachmentUrlsText(writeAttachmentUrls(ticket.getAttachmentUrls()));
        entity.setStatus(ticket.getStatus().name());
        entity.setHandledBy(ticket.getHandledBy());
        entity.setHandleNote(ticket.getHandleNote());
        entity.setHandledAt(ticket.getHandledAt());
        entity.setClosedAt(ticket.getClosedAt());
        entity.setCreatedAt(ticket.getCreatedAt());
        entity.setUpdatedAt(ticket.getUpdatedAt());
        FeedbackTicketDO saved = feedbackTicketMapper.save(entity);
        return toDomain(saved);
    }

    /**
     * 按反馈单号查找记录。
     */
    @Override
    public Optional<FeedbackTicket> findByFeedbackNo(String feedbackNo) {
        return feedbackTicketMapper.findByFeedbackNo(feedbackNo).map(this::toDomain);
    }

    /**
     * 按用户ID查询记录列表。
     */
    @Override
    public List<FeedbackTicket> listByUserId(Long userId, int limit) {
        return feedbackTicketMapper.findByUserId(userId, limit)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * 按条件查询记录列表。
     */
    @Override
    public List<FeedbackTicket> listByFilters(String feedbackNo, FeedbackStatus status, FeedbackType feedbackType, Long userId, int limit) {
        return feedbackTicketMapper.findByFilters(
                        feedbackNo,
                        status == null ? null : status.name(),
                        feedbackType == null ? null : feedbackType.name(),
                        userId,
                        limit
                )
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private FeedbackTicketDO resolveDO(FeedbackTicket ticket) {
        if (ticket.getId() != null) {
            return feedbackTicketMapper.findById(ticket.getId()).orElse(new FeedbackTicketDO());
        }
        return feedbackTicketMapper.findByFeedbackNo(ticket.getFeedbackNo()).orElse(new FeedbackTicketDO());
    }

    private FeedbackTicket toDomain(FeedbackTicketDO entity) {
        return new FeedbackTicket(
                entity.getId(),
                entity.getFeedbackNo(),
                entity.getUserId(),
                FeedbackType.fromCode(entity.getFeedbackType()),
                entity.getSourceChannel(),
                entity.getSourcePageCode(),
                entity.getTitle(),
                entity.getContent(),
                entity.getContactMobile(),
                readAttachmentUrls(entity.getAttachmentUrlsText()),
                FeedbackStatus.fromCode(entity.getStatus()),
                entity.getHandledBy(),
                entity.getHandleNote(),
                entity.getHandledAt(),
                entity.getClosedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String writeAttachmentUrls(List<String> attachmentUrls) {
        if (attachmentUrls == null || attachmentUrls.isEmpty()) {
            return null;
        }
        return String.join("\n", attachmentUrls);
    }

    private List<String> readAttachmentUrls(String attachmentUrlsJson) {
        if (attachmentUrlsJson == null || attachmentUrlsJson.isBlank()) {
            return List.of();
        }
        return Arrays.stream(attachmentUrlsJson.split("\\n"))
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .toList();
    }
}
